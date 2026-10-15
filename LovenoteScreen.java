package com.lovenote.gui;
import net.minecraft.client.MinecraftClient;
import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.module.impl.InterfaceModule;
import com.lovenote.module.impl.Notifications;
import com.lovenote.module.impl.Panic;
import com.lovenote.module.impl.SpotifyHud;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.setting.Setting;
import com.lovenote.spotify.SpotifyManager;
import com.lovenote.util.Colors;
import com.lovenote.util.Config;
import com.lovenote.util.DrainLogo;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Sleek, draggable menu. Drag the top bar to move, drag the grip to resize. */
public class LovenoteScreen extends Screen {
	private static final int PANEL = 0xF2101016, CARD = 0xFF16161E, CARD_HOV = 0xFF1D1D27;
	private static final int EDGE = 0x18FFFFFF, EDGE_STRONG = 0x2EFFFFFF;
	private static final int LINE = 0xFF22222C, TXT = 0xFFEDEDF5, DIM = 0xFF8E8E9E, FAINT = 0xFF5C5C6E;
	private static final int TRACK = 0xFF272732;

	private static final int TOP = 26, SIDE = 66, STATUS = 14, CARD_H = 24;

	private static final int K_HEADER = 0, K_SWITCH = 1, K_BOOL = 2, K_SLIDER = 3, K_MODE = 4;
	private static final int K_CONNECT = 10, K_DISCONNECT = 11, K_PREV = 12, K_PLAY = 13, K_NEXT = 14, K_SEEK = 15, K_VOL = 16;
	private static final int K_TAB = 20, K_FAV = 21, K_PANIC = 22, K_SAVE = 23, K_STAR = 24, K_COLLAPSE = 25, K_RESET = 26;

	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

	private static final class Hit {
		int x, y, w, h, kind, dir;
		Module module;
		Setting setting;
		Category cat;
	}

	private final Category[] tabs = Category.values();
	private final List<Hit> hits = new ArrayList<>();
	private final Set<Module> expanded = new HashSet<>();
	private final Map<String, Float> anim = new HashMap<>();

	private int tab, scroll, maxScroll;
	private float scale = 1f, ts = 0.88f;
	private int vw, vh, wx, wy, panelW, panelH;
	private TextFieldWidget searchField, idField;
	private Module listening;
	private Hit dragging;
	private boolean resizing, moving, favOnly;
	private int rsX, rsY, rsW, rsH, mvX, mvY, mvWX, mvWY;
	private String tooltip;
	private int localVol = -1;
	private long openTime;
	private float[] px, py, pvx, pvy;

	public LovenoteScreen() {
		super(Text.literal("Lovenote"));
	}

	// ------------------------------------------------------------------ helpers

	private InterfaceModule ui() {
		return LovenoteClient.modules == null ? null : LovenoteClient.modules.get(InterfaceModule.class);
	}

	private int corner() { InterfaceModule u = ui(); return u == null ? 8 : u.corner.getI(); }
	private int rowH() { InterfaceModule u = ui(); return u == null ? 16 : u.rowHeight.getI(); }
	private boolean animated() { InterfaceModule u = ui(); return u == null || u.animations.get(); }
	private boolean tooltips() { InterfaceModule u = ui(); return u == null || u.tooltips.get(); }
	private boolean descOn() { InterfaceModule u = ui(); return u == null || u.descriptions.get(); }
	private boolean logoOn() { InterfaceModule u = ui(); return u == null || u.logo.get(); }
	private boolean hairline() { InterfaceModule u = ui(); return u == null || u.hairline.get(); }
	private boolean stripe() { InterfaceModule u = ui(); return u == null || u.accentStripe.get(); }

	@Override
	protected void init() {
		openTime = System.currentTimeMillis();
		Random r = new Random();
		int n = 34;
		px = new float[n]; py = new float[n]; pvx = new float[n]; pvy = new float[n];
		for (int i = 0; i < n; i++) {
			px[i] = r.nextFloat() * width;
			py[i] = r.nextFloat() * height;
			pvx[i] = (r.nextFloat() - 0.5f) * 0.25f;
			pvy[i] = -0.08f - r.nextFloat() * 0.3f;
		}
		searchField = new TextFieldWidget(textRenderer, 0, 0, 10, 14, Text.literal("Search"));
		searchField.setMaxLength(80);
		searchField.setSuggestion("search modules...");
		searchField.setDrawsBackground(false);
		searchField.setChangedListener(v -> scroll = 0);
		idField = new TextFieldWidget(textRenderer, 0, 0, 10, 14, Text.literal("Client ID"));
		idField.setMaxLength(64);
		idField.setDrawsBackground(false);
		idField.setText(SpotifyManager.I.clientId);
		idField.setChangedListener(s -> SpotifyManager.I.setClientId(s.trim()));
		layout();
	}

	private void layout() {
		InterfaceModule u = ui();
		scale = MathHelper.clamp((u == null ? 100 : u.uiScale.getI()) / 100f, 0.5f, 2.0f);
		ts = MathHelper.clamp((u == null ? 88 : u.textScale.getI()) / 100f, 0.70f, 1.10f);
		vw = Math.max(240, (int) (width / scale));
		vh = Math.max(170, (int) (height / scale));
		panelW = MathHelper.clamp(u == null ? 430 : u.panelWidth.getI(), 340, Math.max(360, vw - 10));
		panelH = MathHelper.clamp(u == null ? 275 : u.panelHeight.getI(), 210, Math.max(220, vh - 10));
		int px0 = u == null ? -1 : u.panelX.getI();
		int py0 = u == null ? -1 : u.panelY.getI();
		wx = px0 < 0 ? (vw - panelW) / 2 : MathHelper.clamp(px0, 2, Math.max(2, vw - panelW - 2));
		wy = py0 < 0 ? (vh - panelH) / 2 : MathHelper.clamp(py0, 2, Math.max(2, vh - panelH - 2));
		positionFields();
	}

	private void positionFields() {
		int cx = wx + SIDE + 8, cw = panelW - SIDE - 16;
		searchField.setX(cx + 8);
		searchField.setY(wy + TOP + 8);
		searchField.setWidth(cw - 30);
		idField.setX(cx + 8);
		idField.setY(wy + TOP + 8);
		idField.setWidth(cw - 30);
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

	// ------------------------------------------------------------------ primitives

	private int tw(String s) { return (int) (textRenderer.getWidth(s) * ts); }
	private int sc(int v) { return (int) (v * scale); }

	private void txt(DrawContext ctx, String s, int x, int y, int color, boolean shadow) {
		var ms = ctx.getMatrices();
		ms.push();
		ms.translate(x, y, 0);
		ms.scale(ts, ts, 1f);
		if (shadow) ctx.drawTextWithShadow(textRenderer, s, 0, 0, color);
		else ctx.drawText(textRenderer, s, 0, 0, color, false);
		ms.pop();
	}

	/** Rounded shape with a 1px lighter edge - the "sleek" look without heavy borders. */
	private void hair(DrawContext ctx, int x, int y, int w, int h, int r, int fill, int edge) {
		RenderUtil.rrect(ctx, x, y, w, h, r, edge);
		RenderUtil.rrect(ctx, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
	}

	private String fit(String v, int maxW) {
		if (v == null || maxW <= 0) return "";
		if (tw(v) <= maxW) return v;
		String suffix = "\u2026";
		if (tw(suffix) > maxW) return "";
		int end = v.length();
		while (end > 0 && tw(v.substring(0, end) + suffix) > maxW) end--;
		return v.substring(0, end) + suffix;
	}

	private float anim(String key, float target) {
		if (!animated()) return target;
		float v = anim.getOrDefault(key, target);
		v += (target - v) * 0.3f;
		if (Math.abs(target - v) < 0.01f) v = target;
		anim.put(key, v);
		return v;
	}

	private Hit addHit(int x, int y, int w, int h, int kind, Module m, Setting s) {
		Hit hit = new Hit();
		hit.x = x; hit.y = y; hit.w = w; hit.h = h; hit.kind = kind;
		hit.module = m; hit.setting = s;
		hits.add(hit);
		return hit;
	}

	/** Re-runs enable so modules that cache their state apply a new value instantly. */
	private void refresh(Module m) {
		InterfaceModule u = ui();
		if (u != null && !u.liveApply.get()) return;
		if (m == null || !m.enabled) return;
		m.onDisable();
		m.onEnable();
	}

	// ------------------------------------------------------------------ render

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		hits.clear();
		tooltip = null;

		InterfaceModule u = ui();
		if (u != null) u.apply();
		layout();

		int smx = (int) (mx / scale), smy = (int) (my / scale);

		ctx.getMatrices().push();
		ctx.getMatrices().scale(scale, scale, 1f);

		float open = animated() ? Math.min(1f, (System.currentTimeMillis() - openTime) / 170f) : 1f;
		ctx.fill(0, 0, vw, vh, Colors.alpha(0x000000, (int) ((u == null ? 120 : u.dim.getI()) * open)));

		if (u == null || u.particles.get()) {
			for (int i = 0; i < px.length; i++) {
				px[i] += pvx[i];
				py[i] += pvy[i];
				if (py[i] < 0) { py[i] = vh; px[i] = (float) (Math.random() * vw); }
				if (px[i] < 0) px[i] = vw;
				if (px[i] > vw) px[i] = 0;
				ctx.fill((int) px[i], (int) py[i], (int) px[i] + 1, (int) py[i] + 1,
						Colors.alpha(Colors.skeet(i / (float) px.length), 110));
			}
		}

		int cr = corner();
		if (u == null || u.shadow.get())
			for (int i = 6; i >= 1; i--)
				RenderUtil.rrect(ctx, wx - i, wy - i, panelW + i * 2, panelH + i * 2, cr + i, Colors.alpha(0x000000, 8));
		if (u != null && u.glow.get())
			for (int i = 3; i >= 1; i--)
				RenderUtil.rrect(ctx, wx - i, wy - i, panelW + i * 2, panelH + i * 2, cr + i, Colors.alpha(Colors.C2, 22));

		hair(ctx, wx, wy, panelW, panelH, cr, PANEL, EDGE);
		if (u == null || u.glass.get()) {
			RenderUtil.rrect(ctx, wx + cr, wy + 1, panelW - cr * 2, 1, 0, 0x1EFFFFFF);
			RenderUtil.rrect(ctx, wx + 1, wy + cr, 1, panelH - cr * 2, 0, 0x10FFFFFF);
		}
		RenderUtil.skeetBar(ctx, wx + cr, wy + 1, panelW - cr * 2, 1);

		renderTop(ctx, smx, smy);
		renderSidebar(ctx, smx, smy);

		if (tabs[tab] == Category.SPOTIFY) {
			int cx = wx + SIDE + 8, cy = wy + TOP + 4, cw = panelW - SIDE - 16;
			ctx.enableScissor(sc(cx - 2), sc(cy - 2), sc(cx + cw + 2), sc(wy + panelH - STATUS - 4));
			renderSpotify(ctx, smx, smy, delta, cx, cy, cw);
			ctx.disableScissor();
		} else {
			int cx = wx + SIDE + 8, cw = panelW - SIDE - 16;
			int sy = wy + TOP + 4;
			hair(ctx, cx, sy, cw, 16, 5, 0xFF14141B, EDGE);
			txt(ctx, "\u2315", cx + 6, sy + 5, FAINT, false);
			searchField.render(ctx, smx, smy, delta);

			int cy = wy + TOP + 26, bottom = wy + panelH - STATUS - 4;
			ctx.enableScissor(sc(cx - 2), sc(cy - 2), sc(cx + cw + 2), sc(bottom));
			renderModules(ctx, cx, cy, cw, bottom, smx, smy);
			ctx.disableScissor();
			renderStatus(ctx);
		}

		drawGrip(ctx, wx + panelW - 13, wy + panelH - 13);

		if (tooltip != null && tooltips()) {
			int twd = textRenderer.getWidth(tooltip) + 10;
			int tx = MathHelper.clamp(smx + 10, 0, Math.max(0, vw - twd));
			int ty = MathHelper.clamp(smy + 10, 0, Math.max(0, vh - 15));
			hair(ctx, tx, ty, twd, 14, 4, 0xF51A1A22, EDGE_STRONG);
			ctx.drawTextWithShadow(textRenderer, tooltip, tx + 5, ty + 3, TXT);
		}

		if (listening != null)
			ctx.drawCenteredTextWithShadow(textRenderer,
					"press a key to bind " + listening.name + "   (esc clears)",
					wx + panelW / 2, wy + panelH + 5, 0xFFFFD27A);

		ctx.getMatrices().pop();
	}

	private void renderTop(DrawContext ctx, int mx, int my) {
		int cx = wx + 10, cy = wy + 7;
		if (logoOn()) {
			DrainLogo.draw(ctx, cx, cy, 12, Colors.wave(0f));
			cx += 16;
		}
		var ms = ctx.getMatrices();
		ms.push();
		ms.translate(cx, cy, 0);
		ms.scale(1.06f, 1.06f, 1f);
		int bx = 0;
		String brand = "lovenote";
		for (int i = 0; i < brand.length(); i++) {
			String ch = String.valueOf(brand.charAt(i));
			ctx.drawTextWithShadow(textRenderer, ch, bx, 0, Colors.wave(i * 0.07f));
			bx += textRenderer.getWidth(ch);
		}
		ms.pop();

		String clock = LocalTime.now().format(CLOCK);
		MinecraftClient mc = MinecraftClient.getInstance();
		String fps = mc.getCurrentFps() + " fps";
		String user = mc.getSession().getUsername();
		int rx = wx + panelW - 10;
		txt(ctx, clock, rx - tw(clock), wy + 9, FAINT, false);
		rx -= tw(clock) + 9;
		txt(ctx, fps, rx - tw(fps), wy + 9, FAINT, false);
		rx -= tw(fps) + 9;
		int cw2 = tw(user) + 14;
		hair(ctx, rx - cw2, wy + 6, cw2, 14, 7, 0xFF191921, EDGE);
		txt(ctx, user, rx - cw2 + 7, wy + 9, DIM, false);

		ctx.fill(wx + 10, wy + TOP - 1, wx + panelW - 10, wy + TOP, LINE);
	}

	private void renderSidebar(DrawContext ctx, int mx, int my) {
		int x = wx + 8, y = wy + TOP + 5, w = SIDE - 16, h = 17;
		for (int i = 0; i < tabs.length; i++) {
			boolean sel = i == tab;
			boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
			if (sel) {
				hair(ctx, x, y, w, h, 6, Colors.alpha(Colors.C2, 34), Colors.alpha(Colors.C2, 90));
				ctx.fill(x + 2, y + 5, x + 3, y + h - 5, Colors.C2);
			} else if (hov) {
				hair(ctx, x, y, w, h, 6, 0xFF1A1A23, EDGE);
			}
			if (ui() == null || ui().sidebarIcons.get()) txt(ctx, tabs[i].icon, x + 7, y + 5, sel ? TXT : DIM, false);
			txt(ctx, fit(tabs[i].label, w - 24), x + 19, y + 5, sel ? TXT : DIM, false);
			addHit(x, y, w, h, K_TAB, null, null).cat = tabs[i];
			y += h + 2;
		}

		y += 5;
		ctx.fill(x + 3, y - 3, x + w - 3, y - 2, LINE);

		String[] labels = {"favourites", "collapse", "panic", "save", "reset"};
		int[] kinds = {K_FAV, K_COLLAPSE, K_PANIC, K_SAVE, K_RESET};
		for (int i = 0; i < labels.length; i++) {
			boolean hov = mx >= x && mx < x + w && my >= y && my < y + 15;
			boolean on = kinds[i] == K_FAV && favOnly;
			hair(ctx, x, y, w, 15, 5, on ? Colors.alpha(Colors.C2, 34) : hov ? 0xFF1A1A23 : 0x00000000, on ? Colors.alpha(Colors.C2, 90) : EDGE);
			txt(ctx, labels[i], x + 7, y + 4, on ? TXT : hov ? TXT : FAINT, false);
			addHit(x, y, w, 15, kinds[i], null, null);
			y += 17;
		}
	}

	private void renderStatus(DrawContext ctx) {
		int on = 0;
		for (Module m : LovenoteClient.modules.all()) if (m.enabled && m.category != Category.HUD) on++;
		int total = LovenoteClient.modules.all().size();
		String left = on + " on  \u00B7  " + total + " modules";
		String right = "lovenote \u00B7 drag to move";
		txt(ctx, left, wx + SIDE + 10, wy + panelH - 11, FAINT, false);
		txt(ctx, right, wx + panelW - 10 - tw(right), wy + panelH - 11, FAINT, false);
		if (logoOn()) DrainLogo.draw(ctx, wx + 10, wy + panelH - 14, 10, Colors.wave(0f));
	}

	// ------------------------------------------------------------------ cards

	private int rowHeight(Setting s) {
		return s instanceof NumberSetting ? rowH() + 9 : rowH() + 1;
	}

	private int settingsH(Module m) {
		int h = 0;
		for (Setting s : m.settings) h += rowHeight(s);
		return h > 0 ? h + 3 : 0;
	}

	private void drawSwitch(DrawContext ctx, int x, int y, float t) {
		RenderUtil.rrect(ctx, x, y, 20, 10, 5, Colors.lerp(TRACK, Colors.wave(0f), t));
		RenderUtil.rrect(ctx, x + 1 + (int) (t * 10), y + 1, 8, 8, 4, 0xFFFFFFFF);
	}

	private String keyName(int key) {
		if (key == -1) return "none";
		String n = InputUtil.fromKeyCode(key, -1).getLocalizedText().getString().toUpperCase(Locale.ROOT);
		return n.length() > 6 ? n.substring(0, 6) : n;
	}

	private void renderModules(DrawContext ctx, int cx, int cy, int cw, int bottom, int mx, int my) {
		List<Module> mods = new ArrayList<>(LovenoteClient.modules.in(tabs[tab]));
		String q = searchField.getText().trim().toLowerCase(Locale.ROOT);
		if (favOnly) mods.removeIf(m -> !m.favorite);
		if (!q.isEmpty()) mods.removeIf(m -> !m.matches(q));
		if (mods.isEmpty())
			ctx.drawCenteredTextWithShadow(textRenderer, "no matching modules", cx + cw / 2, cy + 16, DIM);

		boolean two = (ui() == null || ui().columns.get()) && cw >= 260;
		int cols = two ? 2 : 1;
		int colW = cols == 2 ? (cw - 6) / 2 : cw;
		int[] colY = {cy - scroll, cy - scroll};
		for (Module m : mods) {
			int col = cols == 1 ? 0 : (colY[0] <= colY[1] ? 0 : 1);
			int gx = cx + col * (colW + 6);
			colY[col] += drawCard(ctx, m, gx, colY[col], colW, mx, my) + 4;
		}
		int total = (cols == 1 ? colY[0] : Math.max(colY[0], colY[1])) + scroll - cy;
		maxScroll = Math.max(0, total - (bottom - cy));
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
	}

	private int drawCard(DrawContext ctx, Module m, int gx, int gy, int w, int mx, int my) {
		boolean open = expanded.contains(m);
		float ex = anim(m.name + ":ex", open ? 1f : 0f);
		float on = anim(m.name + ":sw", m.enabled ? 1f : 0f);
		int sh = (int) (settingsH(m) * ex);
		int hh = CARD_H + sh;
		int cr = Math.max(3, corner() - 4);

		boolean hov = mx >= gx && mx < gx + w && my >= gy && my < gy + CARD_H;
		hair(ctx, gx, gy, w, hh, cr, hov ? CARD_HOV : CARD, hov ? EDGE_STRONG : EDGE);
		if (stripe() && on > 0.02f)
			ctx.fill(gx + 1, gy + 6, gx + 3, gy + CARD_H - 6, Colors.alpha(Colors.wave(gy * 0.004f), (int) (255 * on)));

		boolean hints = ui() == null || ui().keybindHints.get();
		String key = (m.key != -1 || listening == m) && hints
				? "[" + (listening == m ? "..." : keyName(m.key)) + "]" : "";
		int keyW = key.isEmpty() ? 0 : tw(key) + 3;
		String title = fit(m.name, Math.max(0, w - 60 - keyW));
		if (hov && !title.equals(m.name)) tooltip = m.name;
		txt(ctx, title, gx + 9, gy + 5, Colors.lerp(DIM, TXT, on), false);
		if (!key.isEmpty()) txt(ctx, key, gx + 11 + tw(title), gy + 5, FAINT, false);
		if (descOn() && !m.description.isEmpty()) txt(ctx, fit(m.description, w - 18), gx + 9, gy + 14, FAINT, false);

		if (!m.settings.isEmpty()) txt(ctx, open ? "-" : "+", gx + w - 56, gy + 5, FAINT, false);
		if (m.favorite) txt(ctx, "\u2605", gx + w - 44, gy + 5, Colors.wave(0.1f), false);
		drawSwitch(ctx, gx + w - 28, gy + 7, on);

		addHit(gx, gy, w, CARD_H, K_HEADER, m, null);
		addHit(gx + w - 30, gy + 3, 24, 18, K_SWITCH, m, null);
		addHit(gx + w - 46, gy + 3, 12, 18, K_STAR, m, null);

		if (ex > 0.02f) {
			ctx.enableScissor(sc(gx), sc(gy + CARD_H), sc(gx + w), sc(gy + CARD_H + sh));
			int ry = gy + CARD_H + 2;
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
		int lx = gx + 9, rw = w - 18;
		if (s instanceof BoolSetting b) {
			float t = anim(m.name + ":" + b.name, b.get() ? 1f : 0f);
			txt(ctx, fit(b.name, rw - 32), lx, ry + 3, b.get() ? TXT : DIM, false);
			drawSwitch(ctx, gx + w - 9 - 18, ry + 3, t);
			if (active) addHit(gx + 4, ry, w - 8, rowH(), K_BOOL, m, b);
		} else if (s instanceof NumberSetting n) {
			boolean hue = n.name.equalsIgnoreCase("Hue");
			txt(ctx, fit(n.name, Math.max(0, rw - 44)), lx, ry + 2, DIM, false);
			String val = n.step >= 1 ? String.valueOf(n.getI()) : String.format(Locale.ROOT, "%.2f", n.get());
			txt(ctx, val, lx + rw - tw(val), ry + 2, FAINT, false);
			int sy = ry + 13;
			float t = n.max <= n.min ? 0f : (float) ((n.get() - n.min) / (n.max - n.min));
			if (hue) {
				int segs = 24;
				for (int i = 0; i < segs; i++) {
					int x0 = lx + rw * i / segs, x1 = lx + rw * (i + 1) / segs;
					ctx.fill(x0, sy, x1, sy + 3, Colors.hsv(i / (float) segs, 0.85f, 1f));
				}
			} else {
				RenderUtil.rrect(ctx, lx, sy, rw, 3, 2, TRACK);
				int fx = lx + (int) (rw * t);
				RenderUtil.gradientH(ctx, lx, sy, Math.max(lx + 1, fx), sy + 3, Colors.C1, Colors.skeet(t));
			}
			int fx = lx + (int) (rw * t);
			RenderUtil.rrect(ctx, MathHelper.clamp(fx - 3, lx, lx + rw - 6), sy - 3, 6, 8, 3, 0xFFFFFFFF);
			if (active) addHit(lx, sy - 5, rw, 12, K_SLIDER, m, n);
		} else if (s instanceof ModeSetting md) {
			txt(ctx, fit(md.name, rw - 76), lx, ry + 3, DIM, false);
			int pw = Math.max(62, Math.min(rw / 2 + 20, tw(md.get()) + 30));
			int pxx = lx + rw - pw;
			hair(ctx, pxx, ry + 1, pw, 15, 5, 0xFF191921, EDGE);
			txt(ctx, "<", pxx + 5, ry + 4, FAINT, false);
			txt(ctx, fit(md.get(), pw - 22), pxx + 13, ry + 4, TXT, false);
			txt(ctx, ">", pxx + pw - 9, ry + 4, FAINT, false);
			if (active) {
				addHit(pxx, ry, pw / 3, 16, K_MODE, m, md).dir = -1;
				addHit(pxx + pw / 3, ry, pw - pw / 3, 16, K_MODE, m, md).dir = 1;
			}
		}
	}

	private void drawGrip(DrawContext ctx, int x, int y) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j <= i; j++)
				ctx.fill(x + i * 3, y + j * 3, x + i * 3 + 2, y + j * 3 + 2, FAINT);
	}

	// ------------------------------------------------------------------ spotify

	private void button(DrawContext ctx, int x, int y, int w, int h, String label, int mx, int my, int kind) {
		boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
		hair(ctx, x, y, w, h, 4, hov ? 0xFF1D1D27 : 0xFF16161E, hov ? EDGE_STRONG : EDGE);
		ctx.drawCenteredTextWithShadow(textRenderer, fit(label, w - 6), x + w / 2, y + (h - 8) / 2 + 1, hov ? TXT : DIM);
		addHit(x, y, w, h, kind, null, null);
	}

	private void bar(DrawContext ctx, int x, int y, int w, float f) {
		if (w <= 1) return;
		RenderUtil.rrect(ctx, x, y, w, 4, 2, TRACK);
		int fx = x + (int) (w * f);
		RenderUtil.gradientH(ctx, x, y, Math.max(x + 1, fx), y + 4, Colors.C1, Colors.skeet(f));
		RenderUtil.rrect(ctx, MathHelper.clamp(fx - 3, x, x + w - 6), y - 3, 6, 9, 3, 0xFFFFFFFF);
	}

	private void renderSpotify(DrawContext ctx, int mx, int my, float delta, int cx, int cy, int cw) {
		SpotifyManager s = SpotifyManager.I;
		int cr = Math.max(3, corner() - 4);

		int card1 = 76;
		hair(ctx, cx, cy, cw, card1, cr, CARD, EDGE);
		txt(ctx, "spotify", cx + 9, cy + 6, Colors.wave(0f), false);
		idField.render(ctx, mx, my, delta);
		button(ctx, cx + 9, cy + 36, 58, 14, "connect", mx, my, K_CONNECT);
		button(ctx, cx + 71, cy + 36, 66, 14, "disconnect", mx, my, K_DISCONNECT);
		boolean ok = s.status.startsWith("Connected");
		ctx.drawText(textRenderer, fit(s.status, cw - 18), cx + 9, cy + 53, ok ? 0xFF8EE0A8 : 0xFFE8B86A, false);
		if (!s.connected)
			ctx.drawText(textRenderer, fit("bridge exe goes in .minecraft/config", cw - 18), cx + 9, cy + 63, FAINT, false);

		int gy = cy + card1 + 6;
		int card2 = 106;
		hair(ctx, cx, gy, cw, card2, cr, CARD, EDGE);

		int ax = cx + 9, ay = gy + 9, as = 50;
		if (s.connected && s.artTexture != null) {
			ctx.drawTexture(s.artTexture, ax, ay, as, as, 0, 0, 64, 64, 64, 64);
		} else {
			RenderUtil.rrect(ctx, ax, ay, as, as, 4, 0xFF191921);
			ctx.drawCenteredTextWithShadow(textRenderer, "\u266A", ax + as / 2, ay + 19, Colors.wave(0));
		}

		int tx = ax + as + 9;
		int tw = Math.max(40, cx + cw - 9 - tx);

		if (s.connected && s.hasTrack) {
			SpotifyHud.marquee(ctx, textRenderer, s.title, tx, gy + 10, tw, 0xFFFFFFFF);
			SpotifyHud.marquee(ctx, textRenderer, s.artist, tx, gy + 21, tw, 0xFFA0A0B2);
			SpotifyHud.marquee(ctx, textRenderer, s.album, tx, gy + 32, tw, FAINT);
		} else {
			ctx.drawText(textRenderer, s.connected ? "nothing playing" : "not connected", tx, gy + 10, DIM, false);
		}

		long prog = s.progress();
		float f = s.durationMs <= 0 ? 0f : Math.min(1f, prog / (float) s.durationMs);
		int by = gy + 50;
		bar(ctx, tx, by, tw, f);
		addHit(tx, by - 5, tw, 12, K_SEEK, null, null);

		String el = SpotifyHud.fmt(prog);
		String du = SpotifyHud.fmt(s.durationMs);
		ctx.drawText(textRenderer, el, tx, by + 8, FAINT, false);
		ctx.drawText(textRenderer, du, tx + tw - textRenderer.getWidth(du), by + 8, FAINT, false);

		int bty = gy + 70;
		button(ctx, tx, bty, 28, 15, "<<", mx, my, K_PREV);
		button(ctx, tx + 32, bty, 28, 15, s.playing ? "||" : ">", mx, my, K_PLAY);
		button(ctx, tx + 64, bty, 28, 15, ">>", mx, my, K_NEXT);

		int vol = localVol >= 0 ? localVol : Math.max(0, s.volume);
		ctx.drawText(textRenderer, "vol", tx + 100, bty + 4, FAINT, false);
		int vx = tx + 122, vw2 = Math.max(10, tw - 122);
		bar(ctx, vx, bty + 6, vw2, vol / 100f);
		addHit(vx, bty + 1, vw2, 12, K_VOL, null, null);
	}

	// ------------------------------------------------------------------ input

	private boolean inContent(double mx, double my) {
		return mx >= wx + 6 && mx <= wx + panelW - 6 && my >= wy + TOP && my <= wy + panelH - 6;
	}

	private void setSlider(Hit h, double mx) {
		NumberSetting n = (NumberSetting) h.setting;
		double t = MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0);
		n.set(n.min + t * (n.max - n.min));
	}

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		double vmx = mx / scale, vmy = my / scale;

		// grip -> resize
		if (vmx >= wx + panelW - 15 && vmx < wx + panelW && vmy >= wy + panelH - 15 && vmy < wy + panelH && ui() != null) {
			resizing = true;
			rsX = (int) vmx; rsY = (int) vmy; rsW = panelW; rsH = panelH;
			return true;
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
					case K_TAB -> { tab = h.cat.ordinal(); scroll = 0; listening = null; }
					case K_FAV -> favOnly = !favOnly;
					case K_COLLAPSE -> expanded.clear();
					case K_RESET -> {
						InterfaceModule u = ui();
						if (u != null) { u.panelX.set(-1); u.panelY.set(-1); }
						layout();
					}
					case K_PANIC -> {
						Panic p = LovenoteClient.modules.get(Panic.class);
						if (p != null) p.setEnabled(true);
					}
					case K_SAVE -> { Config.save(); Notifications.push("config saved"); }
					case K_HEADER -> {
						if (btn == 2) listening = h.module;
						else if (btn == 1) { if (!expanded.remove(h.module)) expanded.add(h.module); }
						else if (btn == 0) h.module.toggle();
					}
					case K_STAR -> { if (btn == 0) h.module.favorite = !h.module.favorite; }
					case K_SWITCH -> { if (btn == 0) h.module.toggle(); }
					case K_BOOL -> { if (btn == 0) { ((BoolSetting) h.setting).toggle(); refresh(h.module); } }
					case K_SLIDER -> { if (btn == 0) { dragging = h; setSlider(h, vmx); } }
					case K_MODE -> {
						((ModeSetting) h.setting).cycle(h.dir == -1 ? -1 : 1);
						refresh(h.module);
					}
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
					default -> { }
				}
				return true;
			}
		}

		// top bar -> move the whole panel
		InterfaceModule u = ui();
		if (u != null && u.dragToMove.get() && vmx >= wx && vmx < wx + panelW && vmy >= wy && vmy < wy + TOP) {
			moving = true;
			mvX = (int) vmx; mvY = (int) vmy;
			mvWX = wx; mvWY = wy;
			return true;
		}

		return super.mouseClicked(vmx, vmy, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		double vmx = mx / scale, vmy = my / scale;
		if (moving) {
			InterfaceModule u = ui();
			if (u != null) {
				u.panelX.set(mvWX + (vmx - mvX));
				u.panelY.set(mvWY + (vmy - mvY));
				layout();
			}
			return true;
		}
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
		if (dragging != null && dragging.kind == K_SLIDER) refresh(dragging.module);
		if (moving || resizing) Config.save();
		dragging = null;
		resizing = false;
		moving = false;
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
