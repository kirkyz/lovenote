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
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Compact, rounded Lovenote menu: pill tabs on top, expandable module cards. */
public class LovenoteScreen extends Screen {
	private static final int W = 388, H = 262;
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

	private int tab, scroll, maxScroll, wx, wy;
	private Module listening;
	private Hit dragging;
	private int localVol = -1;
	private long openTime;
	private float[] px, py, pvx, pvy;
	private TextFieldWidget idField;

	public LovenoteScreen() {
		super(Text.literal("Lovenote"));
	}

	// ------------------------------------------------------------------ setup

	@Override
	protected void init() {
		openTime = System.currentTimeMillis();
		Random r = new Random();
		px = new float[60];
		py = new float[60];
		pvx = new float[60];
		pvy = new float[60];
		for (int i = 0; i < px.length; i++) {
			px[i] = r.nextFloat() * width;
			py[i] = r.nextFloat() * height;
			pvx[i] = (r.nextFloat() - 0.5f) * 0.3f;
			pvy[i] = -0.1f - r.nextFloat() * 0.4f;
		}
		wx = (width - W) / 2;
		wy = (height - H) / 2;
		int cx = wx + 12, cy = wy + 44, cw = W - 24;
		idField = new TextFieldWidget(textRenderer, cx + 9, cy + 18, cw - 18, 14, Text.literal("Client ID"));
		idField.setMaxLength(64);
		idField.setText(SpotifyManager.I.clientId);
		idField.setChangedListener(s -> SpotifyManager.I.setClientId(s.trim()));
		idField.setFocused(false);
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

	private boolean spotifyTab() {
		return tabs[tab] == Category.SPOTIFY;
	}

	private float anim(String key, float target) {
		float v = anim.getOrDefault(key, target);
		v += (target - v) * 0.28f;
		if (Math.abs(target - v) < 0.01f) v = target;
		anim.put(key, v);
		return v;
	}

	// ------------------------------------------------------------------ render

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		hits.clear();
		tabRects.clear();
		InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);
		if (im != null) im.apply();

		wx = (width - W) / 2;
		wy = (height - H) / 2;
		float open = Math.min(1f, (System.currentTimeMillis() - openTime) / 200f);

		ctx.fill(0, 0, width, height, Colors.alpha(0x000000, (int) (110 * open)));

		// floating background particles
		for (int i = 0; i < px.length; i++) {
			px[i] += pvx[i];
			py[i] += pvy[i];
			if (py[i] < 0) { py[i] = height; px[i] = (float) (Math.random() * width); }
			if (px[i] < 0) px[i] = width;
			if (px[i] > width) px[i] = 0;
			int s = 1 + (i % 2);
			ctx.fill((int) px[i], (int) py[i], (int) px[i] + s, (int) py[i] + s,
					Colors.alpha(Colors.skeet(i / (float) px.length), 140));
		}

		// soft shadow + border + body
		for (int i = 5; i >= 1; i--) RenderUtil.rrect(ctx, wx - i, wy - i, W + i * 2, H + i * 2, Colors.alpha(0x000000, 13));
		RenderUtil.rrect(ctx, wx - 1, wy - 1, W + 2, H + 2, Colors.alpha(Colors.C2, 110));
		RenderUtil.rrect(ctx, wx, wy, W, H, BG);
		RenderUtil.skeetBar(ctx, wx + 4, wy + 1, W - 8, 1);

		renderHeader(ctx, mx, my);

		int cx = wx + 12, cy = wy + 44, cw = W - 24, bottom = wy + H - 10;
		if (spotifyTab()) {
			renderSpotify(ctx, mx, my, delta, cx, cy, cw);
		} else {
			ctx.enableScissor(cx - 2, cy - 2, cx + cw + 2, bottom);
			renderModules(ctx, cx, cy, cw, bottom, mx, my);
			ctx.disableScissor();
		}

		if (listening != null) {
			ctx.drawCenteredTextWithShadow(textRenderer, "press a key to bind " + listening.name + "  (esc = clear)",
					wx + W / 2, wy + H + 8, 0xFFFFD27A);
		}
	}

	private void renderHeader(DrawContext ctx, int mx, int my) {
		// brand
		var ms = ctx.getMatrices();
		ms.push();
		ms.translate(wx + 14, wy + 12, 0);
		ms.scale(1.35f, 1.35f, 1f);
		int bx = 0;
		String brand = "lovenote";
		for (int i = 0; i < brand.length(); i++) {
			String ch = String.valueOf(brand.charAt(i));
			ctx.drawTextWithShadow(textRenderer, ch, bx, 0, Colors.wave(i * 0.07f));
			bx += textRenderer.getWidth(ch);
		}
		ctx.drawTextWithShadow(textRenderer, "\u2665", bx + 3, 0, Colors.C2);
		ms.pop();

		// pill tabs, right aligned
		int px = wx + W - 12, py = wy + 12, ph = 15;
		TabRect[] rects = new TabRect[tabs.length];
		for (int i = tabs.length - 1; i >= 0; i--) {
			int pw = textRenderer.getWidth(tabs[i].label) + 14;
			px -= pw;
			TabRect r = new TabRect();
			r.x = px; r.y = py; r.w = pw; r.h = ph;
			rects[i] = r;
			px -= 4;
		}
		for (int i = 0; i < tabs.length; i++) {
			TabRect r = rects[i];
			tabRects.add(r);
			boolean sel = i == tab;
			boolean hov = mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h;
			if (sel) {
				RenderUtil.rrect(ctx, r.x, r.y, r.w, r.h, Colors.C2);
				RenderUtil.gradientH(ctx, r.x + 2, r.y + 1, r.x + r.w - 2, r.y + r.h - 1, Colors.C1, Colors.C3);
			} else {
				RenderUtil.rrect(ctx, r.x, r.y, r.w, r.h, hov ? 0xFF262632 : 0xFF1A1A22);
			}
			ctx.drawCenteredTextWithShadow(textRenderer, tabs[i].label, r.x + r.w / 2, r.y + 4,
					sel ? 0xFF15151B : hov ? TXT : DIM);
		}
		ctx.fill(wx + 12, wy + 36, wx + W - 12, wy + 37, 0xFF20202A);
	}

	// ---------- module cards

	private void renderModules(DrawContext ctx, int cx, int cy, int cw, int bottom, int mx, int my) {
		List<Module> mods = LovenoteClient.modules.in(tabs[tab]);
		int colW = (cw - 8) / 2;
		int[] colY = {cy - scroll, cy - scroll};
		for (Module m : mods) {
			int col = colY[0] <= colY[1] ? 0 : 1;
			int gx = cx + col * (colW + 8);
			int h = drawCard(ctx, m, gx, colY[col], colW, mx, my);
			colY[col] += h + 6;
		}
		int total = Math.max(colY[0], colY[1]) + scroll - cy;
		maxScroll = Math.max(0, total - (bottom - cy));
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
	}

	private int rowH(Setting s) {
		return s instanceof NumberSetting ? 26 : s instanceof ModeSetting ? 20 : 17;
	}

	private int settingsH(Module m) {
		int h = 0;
		for (Setting s : m.settings) h += rowH(s);
		return h > 0 ? h + 5 : 0;
	}

	private void addHit(int x, int y, int w, int h, int kind, Module m, Setting s) {
		Hit hit = new Hit();
		hit.x = x; hit.y = y; hit.w = w; hit.h = h; hit.kind = kind; hit.module = m; hit.setting = s;
		hits.add(hit);
	}

	private void drawSwitch(DrawContext ctx, int x, int y, float t) {
		RenderUtil.rrect(ctx, x, y, 22, 10, Colors.lerp(TRACK, Colors.wave(0f), t));
		RenderUtil.rrect(ctx, x + 1 + (int) (t * 12), y + 1, 8, 8, 0xFFFFFFFF);
	}

	private String keyName(int key) {
		if (key == -1) return "none";
		String n = InputUtil.fromKeyCode(key, -1).getLocalizedText().getString().toUpperCase();
		return n.length() > 6 ? n.substring(0, 6) : n;
	}

	private int drawCard(DrawContext ctx, Module m, int gx, int gy, int w, int mx, int my) {
		boolean open = expanded.contains(m);
		float ex = anim(m.name + ":ex", open ? 1f : 0f);
		float on = anim(m.name + ":sw", m.enabled ? 1f : 0f);
		int sh = (int) (settingsH(m) * ex);
		int h = 22 + sh;

		boolean hov = inContent(mx, my) && mx >= gx && mx < gx + w && my >= gy && my < gy + 22;
		RenderUtil.rrect(ctx, gx, gy, w, h, hov ? CARD_HOV : CARD);
		if (on > 0.02f) ctx.fill(gx, gy + 6, gx + 2, gy + 16, Colors.alpha(Colors.wave(gy * 0.004f), (int) (255 * on)));

		ctx.drawText(textRenderer, m.name, gx + 9, gy + 7, Colors.lerp(DIM, TXT, on), false);
		if (m.key != -1 || listening == m) {
			String b = "[" + (listening == m ? "..." : keyName(m.key)) + "]";
			ctx.drawText(textRenderer, b, gx + 13 + textRenderer.getWidth(m.name), gy + 7, FAINT, false);
		}
		int sx = gx + w - 30, sy = gy + 6;
		drawSwitch(ctx, sx, sy, on);
		if (!m.settings.isEmpty()) ctx.drawText(textRenderer, open ? "v" : ">", sx - 11, gy + 7, FAINT, false);
		addHit(gx, gy, w, 22, K_HEADER, m, null);
		addHit(sx - 3, gy + 2, 28, 18, K_SWITCH, m, null);

		if (ex > 0.02f) {
			ctx.enableScissor(gx, gy + 22, gx + w, gy + 22 + sh);
			int ry = gy + 24;
			boolean active = ex > 0.95f;
			for (Setting s : m.settings) {
				drawSetting(ctx, m, s, gx, ry, w, active);
				ry += rowH(s);
			}
			ctx.disableScissor();
		}
		return h;
	}

	private void drawSetting(DrawContext ctx, Module m, Setting s, int gx, int ry, int w, boolean active) {
		int lx = gx + 9, rw = w - 18;
		if (s instanceof BoolSetting b) {
			float t = anim(m.name + ":" + b.name, b.get() ? 1f : 0f);
			ctx.drawText(textRenderer, b.name, lx, ry + 3, b.get() ? TXT : DIM, false);
			drawSwitch(ctx, gx + w - 9 - 22, ry + 2, t);
			if (active) addHit(gx + 4, ry, w - 8, 15, K_BOOL, m, b);
		} else if (s instanceof NumberSetting n) {
			ctx.drawText(textRenderer, n.name, lx, ry + 2, DIM, false);
			String val = n.step >= 1 ? String.valueOf(n.getI()) : String.format("%.2f", n.get());
			ctx.drawText(textRenderer, val, lx + rw - textRenderer.getWidth(val), ry + 2, FAINT, false);
			int sy = ry + 15;
			float t = (float) ((n.get() - n.min) / (n.max - n.min));
			ctx.fill(lx, sy, lx + rw, sy + 3, TRACK);
			int fx = lx + (int) (rw * t);
			RenderUtil.gradientH(ctx, lx, sy, Math.max(lx + 1, fx), sy + 3, Colors.C1, Colors.skeet(t));
			RenderUtil.rrect(ctx, MathHelper.clamp(fx - 2, lx, lx + rw - 5), sy - 2, 5, 7, 0xFFFFFFFF);
			if (active) addHit(lx, sy - 4, rw, 12, K_SLIDER, m, n);
		} else if (s instanceof ModeSetting md) {
			ctx.drawText(textRenderer, md.name, lx, ry + 4, DIM, false);
			int pw = textRenderer.getWidth(md.get()) + 12;
			int pxx = lx + rw - pw;
			RenderUtil.rrect(ctx, pxx, ry + 1, pw, 14, 0xFF24242E);
			ctx.drawText(textRenderer, md.get(), pxx + 6, ry + 4, TXT, false);
			if (active) addHit(gx + 4, ry, w - 8, 16, K_MODE, m, md);
		}
	}

	// ---------- spotify tab

	private void button(DrawContext ctx, int x, int y, int w, int h, String label, int mx, int my, int kind) {
		boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
		RenderUtil.rrect(ctx, x, y, w, h, hov ? 0xFF2A2A36 : 0xFF22222C);
		ctx.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + (h - 8) / 2 + 1, hov ? TXT : DIM);
		addHit(x, y, w, h, kind, null, null);
	}

	private void bar(DrawContext ctx, int x, int y, int w, float f) {
		ctx.fill(x, y, x + w, y + 4, TRACK);
		int fx = x + (int) (w * f);
		RenderUtil.gradientH(ctx, x, y, Math.max(x + 1, fx), y + 4, Colors.C1, Colors.skeet(f));
		RenderUtil.rrect(ctx, MathHelper.clamp(fx - 2, x, x + w - 5), y - 2, 5, 8, 0xFFFFFFFF);
	}

	private void renderSpotify(DrawContext ctx, int mx, int my, float delta, int cx, int cy, int cw) {
		SpotifyManager s = SpotifyManager.I;

		// connection card
		RenderUtil.rrect(ctx, cx, cy, cw, 80, CARD);
		ctx.drawText(textRenderer, "spotify", cx + 9, cy + 6, Colors.wave(0f), false);
		idField.render(ctx, mx, my, delta);
		button(ctx, cx + 9, cy + 38, 62, 14, "connect", mx, my, K_CONNECT);
		button(ctx, cx + 75, cy + 38, 72, 14, "disconnect", mx, my, K_DISCONNECT);
		boolean ok = s.status.startsWith("Connected");
		ctx.drawText(textRenderer, s.status, cx + 9, cy + 58, ok ? 0xFF8EE0A8 : 0xFFE8B86A, false);
		if (!s.connected) ctx.drawText(textRenderer, "redirect uri: http://127.0.0.1:8888/callback", cx + 9, cy + 68, FAINT, false);

		// now playing card
		int gy = cy + 88;
		RenderUtil.rrect(ctx, cx, gy, cw, 108, CARD);
		int ax = cx + 9, ay = gy + 10;
		if (s.connected && s.artTexture != null) {
			ctx.drawTexture(s.artTexture, ax, ay, 56, 56, 0, 0, 64, 64, 64, 64);
		} else {
			RenderUtil.rrect(ctx, ax, ay, 56, 56, 0xFF22222C);
			ctx.drawCenteredTextWithShadow(textRenderer, "\u266A", ax + 28, ay + 24, Colors.wave(0));
		}

		int tx = cx + 75, tw = cw - 84;
		if (s.connected && s.hasTrack) {
			SpotifyHud.marquee(ctx, textRenderer, s.title, tx, gy + 10, tw, 0xFFFFFFFF);
			SpotifyHud.marquee(ctx, textRenderer, s.artist, tx, gy + 22, tw, 0xFFA0A0B2);
			SpotifyHud.marquee(ctx, textRenderer, s.album, tx, gy + 34, tw, FAINT);
		} else {
			ctx.drawText(textRenderer, s.connected ? "nothing playing" : "not connected", tx, gy + 10, DIM, false);
		}

		long prog = s.progress();
		float f = s.durationMs <= 0 ? 0f : Math.min(1f, prog / (float) s.durationMs);
		int by = gy + 54;
		bar(ctx, tx, by, tw, f);
		addHit(tx, by - 4, tw, 12, K_SEEK, null, null);
		ctx.drawText(textRenderer, SpotifyHud.fmt(prog), tx, by + 8, FAINT, false);
		String dur = SpotifyHud.fmt(s.durationMs);
		ctx.drawText(textRenderer, dur, tx + tw - textRenderer.getWidth(dur), by + 8, FAINT, false);

		int bty = gy + 76;
		button(ctx, tx, bty, 30, 16, "<<", mx, my, K_PREV);
		button(ctx, tx + 34, bty, 30, 16, s.playing ? "||" : ">", mx, my, K_PLAY);
		button(ctx, tx + 68, bty, 30, 16, ">>", mx, my, K_NEXT);

		int vol = localVol >= 0 ? localVol : Math.max(0, s.volume);
		String vl = "vol " + vol;
		ctx.drawText(textRenderer, vl, tx + 108, bty + 4, FAINT, false);
		int vx = tx + 144, vw = tw - 144;
		bar(ctx, vx, bty + 6, vw, vol / 100f);
		addHit(vx, bty + 2, vw, 12, K_VOL, null, null);

		ctx.drawText(textRenderer, "skip / pause / seek / volume need spotify premium", cx + 9, gy + 98 - 1, 0xFF4E4E5E, false);
	}

	// ------------------------------------------------------------------ input

	private boolean inContent(double mx, double my) {
		return mx >= wx + 10 && mx <= wx + W - 10 && my >= wy + 40 && my <= wy + H - 8;
	}

	private void setSlider(Hit h, double mx) {
		NumberSetting n = (NumberSetting) h.setting;
		double t = MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0);
		n.set(n.min + t * (n.max - n.min));
	}

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		for (int i = 0; i < tabRects.size(); i++) {
			TabRect r = tabRects.get(i);
			if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
				tab = i;
				scroll = 0;
				listening = null;
				idField.setFocused(false);
				return true;
			}
		}

		if (spotifyTab()) {
			boolean inField = idField.mouseClicked(mx, my, btn);
			idField.setFocused(inField);
			if (inField) return true;
		}

		if (inContent(mx, my)) {
			for (int i = hits.size() - 1; i >= 0; i--) {
				Hit h = hits.get(i);
				if (mx < h.x || mx >= h.x + h.w || my < h.y || my >= h.y + h.h) continue;
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
							setSlider(h, mx);
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
							double t = MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0);
							sp.seek((long) (t * sp.durationMs));
						}
					}
					case K_VOL -> {
						dragging = h;
						localVol = (int) (MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0) * 100);
					}
				}
				return true;
			}
		}
		return super.mouseClicked(mx, my, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		if (dragging != null) {
			if (dragging.kind == K_SLIDER) setSlider(dragging, mx);
			else if (dragging.kind == K_VOL) localVol = (int) (MathHelper.clamp((mx - dragging.x) / (double) dragging.w, 0.0, 1.0) * 100);
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int btn) {
		if (dragging != null && dragging.kind == K_VOL && localVol >= 0) SpotifyManager.I.setVolume(localVol);
		dragging = null;
		localVol = -1;
		return super.mouseReleased(mx, my, btn);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double amount) {
		if (!spotifyTab()) scroll = MathHelper.clamp(scroll - (int) (amount * 16), 0, maxScroll);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (listening != null) {
			listening.key = keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode;
			listening = null;
			return true;
		}
		if (spotifyTab() && idField.isFocused()) {
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
		if (spotifyTab() && idField.isFocused()) return idField.charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}
}
