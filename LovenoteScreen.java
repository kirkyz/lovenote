package com.lovenote.gui;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.module.impl.InterfaceModule;
import com.lovenote.module.impl.SpotifyHud;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.setting.Setting;
import com.lovenote.spotify.SpotifyManager;
import com.lovenote.util.Colors;
import com.lovenote.util.Config;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Compact, scalable Lovenote menu. Drag the bottom-right grip to resize. */
public class LovenoteScreen extends Screen {
	private static final int BG = 0xF0101015, CARD = 0xFF17171E, CARD_HOV = 0xFF1D1D26;
	private static final int TXT = 0xFFE8E8F0, DIM = 0xFF8A8A9A, FAINT = 0xFF5E5E70, TRACK = 0xFF2A2A36;

	private static final int K_HEADER = 0, K_SWITCH = 1, K_BOOL = 2, K_SLIDER = 3, K_MODE = 4;
	private static final int K_CONNECT = 10, K_DISCONNECT = 11, K_PREV = 12, K_PLAY = 13, K_NEXT = 14, K_SEEK = 15, K_VOL = 16;

	private static final class Hit {
		int x, y, w, h, kind;
		Module module;
		Setting setting;
	}

	private static final class TabRect {
		int x, y, w, h;
	}

	private final Category[] tabs = Category.values();
	private final List<TabRect> tabRects = new ArrayList<>();
	private final List<Hit> hits = new ArrayList<>();
	private final Set<Module> expanded = new HashSet<>();
	private final Map<String, Float> anim = new HashMap<>();

	private int tab, scroll, maxScroll;
	private float scale = 1f;
	private int vw, vh, wx, wy, panelW, panelH;
	private TextFieldWidget searchField, idField;
	private Module listening;
	private Hit dragging;
	private boolean resizing;
	private int rsX, rsY, rsW, rsH;
	private String tooltip;
	private int localVol = -1;
	private long openTime;
	private float[] px, py, pvx, pvy;

	public LovenoteScreen() {
		super(Text.literal("Lovenote"));
	}

	// ------------------------------------------------------------------ setup

	private InterfaceModule ui() {
		return LovenoteClient.modules == null ? null : LovenoteClient.modules.get(InterfaceModule.class);
	}

	private int corner() { InterfaceModule u = ui(); return u == null ? 5 : u.corner.getI(); }
	private int rowH() { InterfaceModule u = ui(); return u == null ? 20 : u.rowHeight.getI(); }
	private boolean animated() { InterfaceModule u = ui(); return u == null || u.animations.get(); }
	private boolean tooltips() { InterfaceModule u = ui(); return u == null || u.tooltips.get(); }

	@Override
	protected void init() {
		openTime = System.currentTimeMillis();
		Random r = new Random();
		int n = 46;
		px = new float[n]; py = new float[n]; pvx = new float[n]; pvy = new float[n];
		for (int i = 0; i < n; i++) {
			px[i] = r.nextFloat() * width;
			py[i] = r.nextFloat() * height;
			pvx[i] = (r.nextFloat() - 0.5f) * 0.3f;
			pvy[i] = -0.1f - r.nextFloat() * 0.4f;
		}
		searchField = new TextFieldWidget(textRenderer, 0, 0, 10, 14, Text.literal("Search"));
		searchField.setMaxLength(80);
		searchField.setSuggestion("Search modules...");
		searchField.setChangedListener(v -> scroll = 0);
		idField = new TextFieldWidget(textRenderer, 0, 0, 10, 14, Text.literal("Client ID"));
		idField.setMaxLength(64);
		idField.setText(SpotifyManager.I.clientId);
		idField.setChangedListener(s -> SpotifyManager.I.setClientId(s.trim()));
		layout();
	}

	private void layout() {
		InterfaceModule u = ui();
		scale = MathHelper.clamp((u == null ? 100 : u.uiScale.getI()) / 100f, 0.5f, 2.0f);
		vw = Math.max(200, (int) (width / scale));
		vh = Math.max(140, (int) (height / scale));
		panelW = MathHelper.clamp(u == null ? 460 : u.panelWidth.getI(), 280, Math.max(300, vw - 10));
		panelH = MathHelper.clamp(u == null ? 300 : u.panelHeight.getI(), 170, Math.max(180, vh - 10));
		wx = (vw - panelW) / 2;
		wy = (vh - panelH) / 2;
		positionFields();
	}

	private void positionFields() {
		int cx = wx + 10, cw = panelW - 20;
		searchField.setX(cx + 4);
		searchField.setY(wy + 42);
		searchField.setWidth(cw - 8);
		idField.setX(cx + 6);
		idField.setY(wy + 110);
		idField.setWidth(cw - 12);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		Config.save();
		super.close();
	}

	private float anim(String key, float target) {
		if (!animated()) return target;
		float v = anim.getOrDefault(key, target);
		v += (target - v) * 0.28f;
		if (Math.abs(target - v) < 0.01f) v = target;
		anim.put(key, v);
		return v;
	}

	private String fit(String value, int maxWidth) {
		if (value == null || maxWidth <= 0) return "";
		if (textRenderer.getWidth(value) <= maxWidth) return value;
		String suffix = "\u2026";
		if (textRenderer.getWidth(suffix) > maxWidth) return "";
		int end = value.length();
		while (end > 0 && textRenderer.getWidth(value.substring(0, end) + suffix) > maxWidth) end--;
		return value.substring(0, end) + suffix;
	}

	// ------------------------------------------------------------------ render

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		hits.clear();
		tabRects.clear();
		tooltip = null;

		InterfaceModule u = ui();
		if (u != null) u.apply();
		layout();

		int smx = (int) (mx / scale), smy = (int) (my / scale);

		ctx.getMatrices().push();
		ctx.getMatrices().scale(scale, scale, 1f);

		float open = animated() ? Math.min(1f, (System.currentTimeMillis() - openTime) / 180f) : 1f;
		ctx.fill(0, 0, vw, vh, Colors.alpha(0x000000, (int) ((u == null ? 110 : u.dim.getI()) * open)));

		for (int i = 0; i < px.length; i++) {
			px[i] += pvx[i];
			py[i] += pvy[i];
			if (py[i] < 0) { py[i] = vh; px[i] = (float) (Math.random() * vw); }
			if (px[i] < 0) px[i] = vw;
			if (px[i] > vw) px[i] = 0;
			int s = 1 + (i % 2);
			ctx.fill((int) px[i], (int) py[i], (int) px[i] + s, (int) py[i] + s,
					Colors.alpha(Colors.skeet(i / (float) px.length), 140));
		}

		int cr = corner();
		for (int i = 5; i >= 1; i--)
			RenderUtil.rrect(ctx, wx - i, wy - i, panelW + i * 2, panelH + i * 2, cr + i, Colors.alpha(0x000000, 12));
		RenderUtil.rrect(ctx, wx - 1, wy - 1, panelW + 2, panelH + 2, cr + 1, Colors.alpha(Colors.C2, 110));
		RenderUtil.rrect(ctx, wx, wy, panelW, panelH, cr, BG);
		RenderUtil.skeetBar(ctx, wx + 4, wy + 1, panelW - 8, 1);

		renderHeader(ctx, smx, smy);

		int cx = wx + 10, cy = wy + 62, cw = panelW - 20, bottom = wy + panelH - 10;

		if (tabs[tab] == Category.SPOTIFY) {
			ctx.enableScissor(cx - 2, cy - 2, cx + cw + 2, bottom);
			renderSpotify(ctx, smx, smy, delta, cx, cy, cw);
			ctx.disableScissor();
		} else {
			searchField.render(ctx, smx, smy, delta);
			ctx.enableScissor(cx - 2, cy - 2, cx + cw + 2, bottom);
			renderModules(ctx, cx, cy, cw, bottom, smx, smy);
			ctx.disableScissor();
		}

		drawGrip(ctx, wx + panelW - 13, wy + panelH - 13);

		if (tooltip != null && tooltips()) {
			int tw = textRenderer.getWidth(tooltip) + 12;
			int tx = MathHelper.clamp(smx + 10, 0, Math.max(0, vw - tw));
			int ty = MathHelper.clamp(smy + 10, 0, Math.max(0, vh - 17));
			RenderUtil.rrect(ctx, tx, ty, tw, 16, 4, 0xF522222C);
			ctx.drawTextWithShadow(textRenderer, tooltip, tx + 6, ty + 4, TXT);
		}

		if (listening != null) {
			ctx.drawCenteredTextWithShadow(textRenderer, "press a key to bind " + listening.name + "  (esc = clear)",
					wx + panelW / 2, wy + panelH + 6, 0xFFFFD27A);
		}

		ctx.getMatrices().pop();
	}

	private void drawGrip(DrawContext ctx, int x, int y) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j <= i; j++)
				ctx.fill(x + i * 3, y + j * 3, x + i * 3 + 2, y + j * 3 + 2, FAINT);
	}

	private void renderHeader(DrawContext ctx, int mx, int my) {
		var ms = ctx.getMatrices();
		ms.push();
		ms.translate(wx + 12, wy + 10, 0);
		ms.scale(1.22f, 1.22f, 1f);
		int bx = 0;
		String brand = "lovenote";
		for (int i = 0; i < brand.length(); i++) {
			String ch = String.valueOf(brand.charAt(i));
			ctx.drawTextWithShadow(textRenderer, ch, bx, 0, Colors.wave(i * 0.07f));
			bx += textRenderer.getWidth(ch);
		}
		ctx.drawTextWithShadow(textRenderer, "\u2665", bx + 3, 0, Colors.C2);
		ms.pop();

		int px = wx + 10, py = wy + 26, ph = 14;
		int available = panelW - 20;
		for (int i = 0; i < tabs.length; i++) {
			int pw = Math.max(26, textRenderer.getWidth(tabs[i].label) + 12);
			if (px + pw > wx + 10 + available) break;
			TabRect r = new TabRect();
			r.x = px; r.y = py; r.w = pw; r.h = ph;
			tabRects.add(r);
			boolean sel = i == tab;
			boolean hov = mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h;
			RenderUtil.rrect(ctx, r.x, r.y, r.w, r.h, 4, sel ? Colors.C2 : hov ? 0xFF292934 : 0xFF1C1C25);
			ctx.drawCenteredTextWithShadow(textRenderer, fit(tabs[i].label, r.w - 6), r.x + r.w / 2, r.y + 3,
					sel ? 0xFF101018 : hov ? TXT : DIM);
			px += pw + 4;
		}
		ctx.fill(wx + 10, wy + 60, wx + panelW - 10, wy + 61, 0xFF252530);
	}

	// ------------------------------------------------------------------ module cards

	private int rowHeight(Setting s) {
		int base = rowH();
		return s instanceof NumberSetting ? base + 9 : base;
	}

	private int settingsH(Module m) {
		int h = 0;
		for (Setting s : m.settings) h += rowHeight(s);
		return h > 0 ? h + 4 : 0;
	}

	private void addHit(int x, int y, int w, int h, int kind, Module m, Setting s) {
		Hit hit = new Hit();
		hit.x = x; hit.y = y; hit.w = w; hit.h = h; hit.kind = kind; hit.module = m; hit.setting = s;
		hits.add(hit);
	}

	private void drawSwitch(DrawContext ctx, int x, int y, float t) {
		RenderUtil.rrect(ctx, x, y, 20, 9, 4, Colors.lerp(TRACK, Colors.wave(0f), t));
		RenderUtil.rrect(ctx, x + 1 + (int) (t * 10), y + 1, 8, 7, 3, 0xFFFFFFFF);
	}

	private String keyName(int key) {
		if (key == -1) return "none";
		String n = InputUtil.fromKeyCode(key, -1).getLocalizedText().getString().toUpperCase();
		return n.length() > 6 ? n.substring(0, 6) : n;
	}

	private void renderModules(DrawContext ctx, int cx, int cy, int cw, int bottom, int mx, int my) {
		List<Module> mods = LovenoteClient.modules.in(tabs[tab]);
		String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
		if (!query.isEmpty()) mods.removeIf(m -> !m.name.toLowerCase(Locale.ROOT).contains(query));
		if (mods.isEmpty()) ctx.drawCenteredTextWithShadow(textRenderer, "no matching modules", cx + cw / 2, cy + 20, DIM);

		InterfaceModule u = ui();
		boolean twoCol = u == null || u.columns.get();
		int columns = (twoCol && cw >= 280) ? 2 : 1;
		int colW = columns == 2 ? (cw - 6) / 2 : cw;
		int[] colY = {cy - scroll, cy - scroll};
		for (Module m : mods) {
			int col = columns == 1 ? 0 : (colY[0] <= colY[1] ? 0 : 1);
			int gx = cx + col * (colW + 6);
			colY[col] += drawCard(ctx, m, gx, colY[col], colW, mx, my) + 5;
		}
		int total = (columns == 1 ? colY[0] : Math.max(colY[0], colY[1])) + scroll - cy;
		maxScroll = Math.max(0, total - (bottom - cy));
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
	}

	private int drawCard(DrawContext ctx, Module m, int gx, int gy, int w, int mx, int my) {
		boolean open = expanded.contains(m);
		float ex = anim(m.name + ":ex", open ? 1f : 0f);
		float on = anim(m.name + ":sw", m.enabled ? 1f : 0f);
		int sh = (int) (settingsH(m) * ex);
		int hh = 20 + sh;
		int cr = Math.max(2, corner() - 1);

		boolean hov = inContent(mx, my) && mx >= gx && mx < gx + w && my >= gy && my < gy + 20;
		RenderUtil.rrect(ctx, gx, gy, w, hh, cr, hov ? CARD_HOV : CARD);
		if (on > 0.02f) ctx.fill(gx, gy + 5, gx + 2, gy + 15, Colors.alpha(Colors.wave(gy * 0.004f), (int) (255 * on)));

		int reserved = 42;
		String key = (m.key != -1 || listening == m) ? "[" + (listening == m ? "..." : keyName(m.key)) + "]" : "";
		int keyW = key.isEmpty() ? 0 : textRenderer.getWidth(key) + 3;
		String title = fit(m.name, Math.max(0, w - reserved - keyW - 8));
		if (hov && !title.equals(m.name)) tooltip = m.name;
		ctx.drawText(textRenderer, title, gx + 8, gy + 6, Colors.lerp(DIM, TXT, on), false);
		if (!key.isEmpty()) ctx.drawText(textRenderer, key, gx + 12 + textRenderer.getWidth(title), gy + 6, FAINT, false);

		int sx = gx + w - 28, sy = gy + 5;
		drawSwitch(ctx, sx, sy, on);
		if (!m.settings.isEmpty()) ctx.drawText(textRenderer, open ? "v" : ">", sx - 10, gy + 6, FAINT, false);
		addHit(gx, gy, w, 20, K_HEADER, m, null);
		addHit(sx - 3, gy + 1, 26, 18, K_SWITCH, m, null);

		if (ex > 0.02f) {
			ctx.enableScissor(gx, gy + 20, gx + w, gy + 20 + sh);
			int ry = gy + 21;
			boolean active = ex > 0.95f;
			for (Setting s : m.settings) {
				drawSetting(ctx, m, s, gx, ry, w, active);
				ry += rowHeight(s);
			}
			ctx.disableScissor();
		}
		return hh;
	}

	private void drawSetting(DrawContext ctx, Module m, Setting s, int gx, int ry, int w, boolean active) {
		int lx = gx + 8, rw = w - 16;
		if (s instanceof BoolSetting b) {
			float t = anim(m.name + ":" + b.name, b.get() ? 1f : 0f);
			ctx.drawText(textRenderer, fit(b.name, rw - 30), lx, ry + 3, b.get() ? TXT : DIM, false);
			drawSwitch(ctx, gx + w - 8 - 20, ry + 2, t);
			if (active) addHit(gx + 3, ry, w - 6, rowH() - 2, K_BOOL, m, b);
		} else if (s instanceof NumberSetting n) {
			boolean hue = n.name.equalsIgnoreCase("Hue");
			ctx.drawText(textRenderer, fit(n.name, Math.max(0, rw - 46)), lx, ry + 2, DIM, false);
			String val = n.step >= 1 ? String.valueOf(n.getI()) : String.format(Locale.ROOT, "%.2f", n.get());
			ctx.drawText(textRenderer, val, lx + rw - textRenderer.getWidth(val), ry + 2, FAINT, false);
			int sy = ry + 14;
			float t = n.max <= n.min ? 0f : (float) ((n.get() - n.min) / (n.max - n.min));
			if (hue) {
				int segs = 24;
				for (int i = 0; i < segs; i++) {
					int x0 = lx + rw * i / segs, x1 = lx + rw * (i + 1) / segs;
					ctx.fill(x0, sy, x1, sy + 3, Colors.hsv(i / (float) segs, 0.85f, 1f));
				}
			} else {
				ctx.fill(lx, sy, lx + rw, sy + 3, TRACK);
				int fx = lx + (int) (rw * t);
				RenderUtil.gradientH(ctx, lx, sy, Math.max(lx + 1, fx), sy + 3, Colors.C1, Colors.skeet(t));
			}
			int fx = lx + (int) (rw * t);
			RenderUtil.rrect(ctx, MathHelper.clamp(fx - 2, lx, lx + rw - 5), sy - 2, 5, 7, 1, 0xFFFFFFFF);
			if (active) addHit(lx, sy - 4, rw, 11, K_SLIDER, m, n);
		} else if (s instanceof ModeSetting md) {
			ctx.drawText(textRenderer, fit(md.name, rw / 2), lx, ry + 4, DIM, false);
			int pw = Math.min(rw / 2, textRenderer.getWidth(md.get()) + 12);
			int pxx = lx + rw - pw;
			RenderUtil.rrect(ctx, pxx, ry + 1, pw, 14, 3, 0xFF24242E);
			ctx.drawText(textRenderer, fit(md.get(), pw - 10), pxx + 5, ry + 4, TXT, false);
			if (active) addHit(gx + 3, ry, w - 6, 16, K_MODE, m, md);
		}
	}

	// ------------------------------------------------------------------ spotify tab

	private void button(DrawContext ctx, int x, int y, int w, int h, String label, int mx, int my, int kind) {
		boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
		RenderUtil.rrect(ctx, x, y, w, h, 3, hov ? 0xFF2A2A36 : 0xFF22222C);
		ctx.drawCenteredTextWithShadow(textRenderer, fit(label, w - 6), x + w / 2, y + (h - 8) / 2 + 1, hov ? TXT : DIM);
		addHit(x, y, w, h, kind, null, null);
	}

	private void bar(DrawContext ctx, int x, int y, int w, float f) {
		if (w <= 1) return;
		ctx.fill(x, y, x + w, y + 4, TRACK);
		int fx = x + (int) (w * f);
		RenderUtil.gradientH(ctx, x, y, Math.max(x + 1, fx), y + 4, Colors.C1, Colors.skeet(f));
		RenderUtil.rrect(ctx, MathHelper.clamp(fx - 2, x, x + w - 5), y - 2, 5, 8, 1, 0xFFFFFFFF);
	}

	private void renderSpotify(DrawContext ctx, int mx, int my, float delta, int cx, int cy, int cw) {
		SpotifyManager s = SpotifyManager.I;
		int cr = Math.max(2, corner() - 1);

		// connection card
		int card1 = 74;
		RenderUtil.rrect(ctx, cx, cy, cw, card1, cr, CARD);
		ctx.drawText(textRenderer, "spotify", cx + 8, cy + 5, Colors.wave(0f), false);
		idField.render(ctx, mx, my, delta);
		button(ctx, cx + 8, cy + 34, 58, 13, "connect", mx, my, K_CONNECT);
		button(ctx, cx + 70, cy + 34, 66, 13, "disconnect", mx, my, K_DISCONNECT);
		boolean ok = s.status.startsWith("Connected");
		ctx.drawText(textRenderer, fit(s.status, cw - 16), cx + 8, cy + 51, ok ? 0xFF8EE0A8 : 0xFFE8B86A, false);
		if (!s.connected)
			ctx.drawText(textRenderer, fit("bridge exe goes in .minecraft/config", cw - 16), cx + 8, cy + 61, FAINT, false);

		// now playing card
		int gy = cy + card1 + 6;
		int card2 = 104;
		RenderUtil.rrect(ctx, cx, gy, cw, card2, cr, CARD);

		int ax = cx + 8, ay = gy + 8, as = 50;
		if (s.connected && s.artTexture != null) {
			ctx.drawTexture(s.artTexture, ax, ay, as, as, 0, 0, 64, 64, 64, 64);
		} else {
			RenderUtil.rrect(ctx, ax, ay, as, as, 3, 0xFF22222C);
			ctx.drawCenteredTextWithShadow(textRenderer, "\u266A", ax + as / 2, ay + 19, Colors.wave(0));
		}

		int tx = ax + as + 8;
		int tw = Math.max(40, cx + cw - 8 - tx);

		if (s.connected && s.hasTrack) {
			SpotifyHud.marquee(ctx, textRenderer, s.title, tx, gy + 9, tw, 0xFFFFFFFF);
			SpotifyHud.marquee(ctx, textRenderer, s.artist, tx, gy + 20, tw, 0xFFA0A0B2);
			SpotifyHud.marquee(ctx, textRenderer, s.album, tx, gy + 31, tw, FAINT);
		} else {
			ctx.drawText(textRenderer, s.connected ? "nothing playing" : "not connected", tx, gy + 9, DIM, false);
		}

		// progress bar, with the two time stamps on their own tidy row underneath
		long prog = s.progress();
		float f = s.durationMs <= 0 ? 0f : Math.min(1f, prog / (float) s.durationMs);
		int by = gy + 48;
		bar(ctx, tx, by, tw, f);
		addHit(tx, by - 4, tw, 11, K_SEEK, null, null);

		String el = SpotifyHud.fmt(prog);
		String du = SpotifyHud.fmt(s.durationMs);
		ctx.drawText(textRenderer, el, tx, by + 7, FAINT, false);
		ctx.drawText(textRenderer, du, tx + tw - textRenderer.getWidth(du), by + 7, FAINT, false);
		if (s.playing && tw > 150) {
			String mid = "playing";
			ctx.drawText(textRenderer, mid, tx + (tw - textRenderer.getWidth(mid)) / 2, by + 7, 0xFF4E4E5E, false);
		}

		// controls
		int bty = gy + 68;
		button(ctx, tx, bty, 28, 15, "<<", mx, my, K_PREV);
		button(ctx, tx + 32, bty, 28, 15, s.playing ? "||" : ">", mx, my, K_PLAY);
		button(ctx, tx + 64, bty, 28, 15, ">>", mx, my, K_NEXT);

		int vol = localVol >= 0 ? localVol : Math.max(0, s.volume);
		ctx.drawText(textRenderer, "vol", tx + 100, bty + 4, FAINT, false);
		int vx = tx + 122, vw2 = Math.max(10, tw - 122);
		bar(ctx, vx, bty + 5, vw2, vol / 100f);
		addHit(vx, bty + 1, vw2, 11, K_VOL, null, null);

		ctx.drawText(textRenderer, fit("controls need the media bridge running", cw - 16), cx + 8, gy + card2 - 11, 0xFF4E4E5E, false);
	}

	// ------------------------------------------------------------------ input

	private boolean inContent(double mx, double my) {
		return mx >= wx + 8 && mx <= wx + panelW - 8 && my >= wy + 60 && my <= wy + panelH - 8;
	}

	private void setSlider(Hit h, double mx) {
		NumberSetting n = (NumberSetting) h.setting;
		double t = MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0);
		n.set(n.min + t * (n.max - n.min));
	}

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		double vmx = mx / scale, vmy = my / scale;

		if (vmx >= wx + panelW - 15 && vmx < wx + panelW && vmy >= wy + panelH - 15 && vmy < wy + panelH && ui() != null) {
			resizing = true;
			rsX = (int) vmx; rsY = (int) vmy; rsW = panelW; rsH = panelH;
			return true;
		}

		for (int i = 0; i < tabRects.size(); i++) {
			TabRect r = tabRects.get(i);
			if (vmx >= r.x && vmx < r.x + r.w && vmy >= r.y && vmy < r.y + r.h) {
				tab = i;
				scroll = 0;
				listening = null;
				idField.setFocused(false);
				searchField.setFocused(false);
				return true;
			}
		}

		if (tabs[tab] != Category.SPOTIFY) {
			boolean inSearch = searchField.mouseClicked(vmx, vmy, btn);
			searchField.setFocused(inSearch);
			if (inSearch) return true;
		} else {
			boolean inField = idField.mouseClicked(vmx, vmy, btn);
			idField.setFocused(inField);
			if (inField) return true;
		}

		if (inContent(vmx, vmy)) {
			for (int i = hits.size() - 1; i >= 0; i--) {
				Hit h = hits.get(i);
				if (vmx < h.x || vmx >= h.x + h.w || vmy < h.y || vmy >= h.y + h.h) continue;
				switch (h.kind) {
					case K_HEADER -> {
						if (btn == 1) listening = h.module;
						else if (btn == 0) {
							if (h.module.settings.isEmpty()) h.module.toggle();
							else if (!expanded.remove(h.module)) expanded.add(h.module);
						}
					}
					case K_SWITCH -> { if (btn == 0) h.module.toggle(); }
					case K_BOOL -> { if (btn == 0) ((BoolSetting) h.setting).toggle(); }
					case K_SLIDER -> {
						if (btn == 0) {
							dragging = h;
							setSlider(h, vmx);
						}
					}
					case K_MODE -> ((ModeSetting) h.setting).cycle(btn == 1 ? -1 : 1);
					case K_CONNECT -> SpotifyManager.I.connect();
					case K_DISCONNECT -> SpotifyManager.I.disconnect();
					case K_PREV -> SpotifyManager.I.previous();
					case K_PLAY -> SpotifyManager.I.togglePlay();
					case K_NEXT -> SpotifyManager.I.next();
					case K_SEEK -> {
						SpotifyManager sp = SpotifyManager.I;
						if (sp.hasTrack) {
							double t = MathHelper.clamp((vmx - h.x) / (double) h.w, 0.0, 1.0);
							sp.seek((long) (t * sp.durationMs));
						}
					}
					case K_VOL -> {
						dragging = h;
						localVol = (int) (MathHelper.clamp((vmx - h.x) / (double) h.w, 0.0, 1.0) * 100);
					}
				}
				return true;
			}
		}
		return super.mouseClicked(vmx, vmy, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		double vmx = mx / scale, vmy = my / scale;
		if (resizing) {
			InterfaceModule u = ui();
			if (u != null) {
				u.panelWidth.set(MathHelper.clamp(rsW + (vmx - rsX), u.panelWidth.min, u.panelWidth.max));
				u.panelHeight.set(MathHelper.clamp(rsH + (vmy - rsY), u.panelHeight.min, u.panelHeight.max));
				layout();
			}
			return true;
		}
		if (dragging != null) {
			if (dragging.kind == K_SLIDER) setSlider(dragging, vmx);
			else if (dragging.kind == K_VOL)
				localVol = (int) (MathHelper.clamp((vmx - dragging.x) / (double) dragging.w, 0.0, 1.0) * 100);
			return true;
		}
		return super.mouseDragged(vmx, vmy, btn, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int btn) {
		if (dragging != null && dragging.kind == K_VOL && localVol >= 0) SpotifyManager.I.setVolume(localVol);
		dragging = null;
		resizing = false;
		localVol = -1;
		return super.mouseReleased(mx / scale, my / scale, btn);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double amount) {
		if (tabs[tab] != Category.SPOTIFY) scroll = MathHelper.clamp(scroll - (int) (amount * 14), 0, maxScroll);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (listening != null) {
			listening.key = keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode;
			listening = null;
			return true;
		}
		if (tabs[tab] != Category.SPOTIFY && searchField.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) searchField.setFocused(false);
			else searchField.keyPressed(keyCode, scanCode, modifiers);
			return true;
		}
		if (tabs[tab] == Category.SPOTIFY && idField.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) idField.setFocused(false);
			else idField.keyPressed(keyCode, scanCode, modifiers);
			return true;
		}
		if (LovenoteClient.openGui.matchesKey(keyCode, scanCode)) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (tabs[tab] != Category.SPOTIFY && searchField.isFocused()) return searchField.charTyped(chr, modifiers);
		if (tabs[tab] == Category.SPOTIFY && idField.isFocused()) return idField.charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}
}
