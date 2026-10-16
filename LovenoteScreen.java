package com.lovenote.gui;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.module.impl.InterfaceModule;
import com.lovenote.module.impl.Lyrics;
import com.lovenote.module.impl.SpotifyHud;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.MultiSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.setting.Setting;
import com.lovenote.spotify.LyricsManager;
import com.lovenote.spotify.SpotifyManager;
import com.lovenote.util.Colors;
import com.lovenote.util.Config;
import com.lovenote.util.DrainLogo;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.IntPredicate;

/** Lovenote menu: flat modern window, expanding rail, cards and a draggable settings panel. */
public class LovenoteScreen extends Screen {
	private static final int PW = 208, PH = 320;
	private static final int ACC_COLS = 11;
	private static final int RAIL_MIN = 46;

	private static int mainX = Integer.MIN_VALUE, mainY, panX = Integer.MIN_VALUE, panY, page;
	private static long sessionStart;

	private static final int K_NONE = -1, K_CARD = 0, K_SWITCH = 1, K_BOOL = 2, K_SLIDER = 3, K_MODE = 4, K_MULTI = 5,
			K_PTOGGLE = 6, K_PCLOSE = 7, K_BIND = 8, K_ACCENT = 9, K_EDITOR = 10, K_CLOSE = 12,
			K_LYRICS = 13,
			SP_CONNECT = 20, SP_DISCONNECT = 21, SP_PREV = 22, SP_PLAY = 23, SP_NEXT = 24, SP_SEEK = 25, SP_VOL = 26;

	private static final class Hit {
		int x, y, w, h, kind, idx;
		Module module;
		Setting setting;
		int[] clip;
	}

	private static final class Ripple {
		int x, y;
		long t;
	}

	private final Category[] pages = Category.values();
	private final ItemStack[] icons = {
			new ItemStack(Items.NETHERITE_SWORD), new ItemStack(Items.FEATHER), new ItemStack(Items.LEATHER_BOOTS),
			new ItemStack(Items.ENDER_EYE), new ItemStack(Items.SPYGLASS), new ItemStack(Items.PAPER),
			new ItemStack(Items.REDSTONE), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLDEN_APPLE),
			new ItemStack(Items.JUKEBOX), new ItemStack(Items.PAINTING)};
	private final ItemStack editorIcon = new ItemStack(Items.ITEM_FRAME);

	private final List<Hit> hits = new ArrayList<>();
	private final List<int[]> pageRects = new ArrayList<>();
	private final List<Ripple> ripples = new ArrayList<>();
	private final Map<String, Float> anim = new HashMap<>();

	private int wx, wy, winW = 600, winH = 372, railW = 118;
	private boolean railRight;
	private float gs = 1f;
	private int vw, vh;
	private float dt, ga = 1f, la = 1f, openMs, closeMs, tabMs, scrollTarget, scrollShown, panelScrollTarget, panelScrollShown;
	private float pillY = -1f;
	private int maxScroll, panelMax;
	private boolean closing, finishClose, animating;
	private long lastNano;
	private Module selected, panelModule, listening;
	private int dragWin, grabX, grabY, localVol = -1;
	private Hit dragging;
	private int[] clip = {0, 0, 99999, 99999};
	private boolean settingFields;
	private String tooltip;
	private float sbAlpha = 0.45f;

	private float[] px, py, pvx, pvy;
	private int particleCount = 45;
	private TextFieldWidget search, idField, primField, secField;

	public LovenoteScreen() {
		super(Text.literal("Lovenote"));
	}

	// ------------------------------------------------------------------ setup

	private TextFieldWidget mkField(String hint, int max, String initial) {
		TextFieldWidget f = new TextFieldWidget(textRenderer, 0, 0, 100, 12, Text.literal(hint));
		f.setDrawsBackground(false);
		f.setMaxLength(max);
		f.setText(initial);
		f.setFocused(false);
		return f;
	}

	private static boolean validHex(String s) {
		if (s.length() != 6) return false;
		try { Integer.parseInt(s, 16); return true; } catch (Exception e) { return false; }
	}

	@Override
	protected void init() {
		if (sessionStart == 0L) sessionStart = System.currentTimeMillis();
		InterfaceModule im0 = LovenoteClient.modules.get(InterfaceModule.class);
		gs = im0 != null ? MathHelper.clamp(im0.guiScale.getI() / 100f, 0.4f, 1.1f) : 1f;
		readLayout(im0);
		vw = Math.max(160, Math.round(width / gs));
		vh = Math.max(120, Math.round(height / gs));

		if (mainX == Integer.MIN_VALUE) {
			mainX = (vw - winW) / 2;
			mainY = (vh - winH) / 2;
		}
		if (panX == Integer.MIN_VALUE) {
			panX = mainX + winW + 8 + PW <= vw - 4 ? mainX + winW + 8 : mainX + winW - PW - 10;
			panY = mainY + 10;
		}
		clampWindows();
		openMs = 0;
		closing = false;
		finishClose = false;
		lastNano = System.nanoTime();

		Random r = new Random();
		px = new float[140]; py = new float[140]; pvx = new float[140]; pvy = new float[140];
		for (int i = 0; i < px.length; i++) {
			px[i] = r.nextFloat() * width;
			py[i] = r.nextFloat() * height;
			pvx[i] = (r.nextFloat() - 0.5f) * 0.25f;
			pvy[i] = -0.08f - r.nextFloat() * 0.35f;
		}

		search = mkField("Search...", 32, "");
		search.setChangedListener(s -> { scrollTarget = 0; scrollShown = 0; tabMs = 0; });
		idField = mkField("Bridge", 64, "");
		primField = mkField("Primary", 6, "");
		secField = mkField("Secondary", 6, "");
		primField.setChangedListener(s -> onHexTyped(s, true));
		secField.setChangedListener(s -> onHexTyped(s, false));
	}

	private void readLayout(InterfaceModule im) {
		if (im == null) {
			winW = 600; winH = 372; railW = 118; railRight = false; particleCount = 45;
			return;
		}
		winW = MathHelper.clamp(im.windowWidth.getI(), 460, 900);
		winH = MathHelper.clamp(im.windowHeight.getI(), 280, 560);
		railW = MathHelper.clamp(im.railWidth.getI(), 92, 168);
		railRight = im.railSide.is("Right");
		particleCount = MathHelper.clamp(im.particles.getI(), 0, 140);
	}

	private int contentX() { return railRight ? wx + 14 : wx + railW + 14; }
	private int contentW() { return winW - railW - 28; }
	private int railX() { return railRight ? wx + winW - railW : wx; }

	private void clampWindows() {
		mainX = MathHelper.clamp(mainX, Math.min(0, vw - winW), Math.max(0, vw - winW));
		mainY = MathHelper.clamp(mainY, Math.min(0, vh - winH), Math.max(0, vh - winH));
		panX = MathHelper.clamp(panX, 0, Math.max(0, vw - PW));
		panY = MathHelper.clamp(panY, 0, Math.max(0, vh - PH));
	}

	private void onHexTyped(String s, boolean primary) {
		if (settingFields) return;
		String v = s.replace("#", "").trim();
		if (!validHex(v)) return;
		InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);
		if (im == null) return;
		if (!im.accent.is("Custom")) {
			im.customPrimary = String.format("%06X", Colors.C1 & 0xFFFFFF);
			im.customSecondary = String.format("%06X", Colors.C3 & 0xFFFFFF);
			im.accent.set("Custom");
		}
		if (primary) im.customPrimary = v.toUpperCase(); else im.customSecondary = v.toUpperCase();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		if (finishClose) {
			Config.save();
			super.close();
			return;
		}
		if (!closing) { closing = true; closeMs = 0; }
	}

	// ------------------------------------------------------------------ helpers

	private int a(int c) {
		int al = (int) (((c >>> 24) * ga) * la);
		return (al << 24) | (c & 0xFFFFFF);
	}

	private void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int c) { ctx.fill(x1, y1, x2, y2, a(c)); }

	private void rr(DrawContext ctx, int x, int y, int w, int h, int c) {
		RenderUtil.rrect(ctx, x, y, w, h, a(c));
	}

	private void rr(DrawContext ctx, int x, int y, int w, int h, int r, int c) {
		rounded(ctx, x, y, w, h, r, a(c));
	}

	private void rounded(DrawContext ctx, int x, int y, int w, int h, int r, int c) {
		if (w <= 0 || h <= 0) return;
		r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
		if (r == 0) { ctx.fill(x, y, x + w, y + h, c); return; }
		for (int i = 0; i < r; i++) {
			double dy = r - i - 0.5;
			int inset = (int) Math.round(r - Math.sqrt(Math.max(0.0, r * r - dy * dy)));
			int x1 = Math.min(x + inset, x + w);
			int x2 = Math.max(x + w - inset, x1);
			ctx.fill(x1, y + i, x2, y + i + 1, c);
			ctx.fill(x1, y + h - 1 - i, x2, y + h - i, c);
		}
		ctx.fill(x, y + r, x + w, y + h - r, c);
	}

	private void grad(DrawContext ctx, int x1, int y1, int x2, int y2, int c1, int c2) {
		RenderUtil.gradientH(ctx, x1, y1, x2, y2, a(c1), a(c2));
	}

	private void txt(DrawContext ctx, String s, int x, int y, int c) {
		int cc = a(c);
		if ((cc >>> 24) < 12) return;
		ctx.drawText(textRenderer, s, x, y, cc, false);
	}

	private void txtS(DrawContext ctx, String s, int x, int y, int c) {
		int cc = a(c);
		if ((cc >>> 24) < 12) return;
		ctx.drawTextWithShadow(textRenderer, s, x, y, cc);
	}

	private void txtScaled(DrawContext ctx, String s, int x, int y, int c, float sc) {
		var ms = ctx.getMatrices();
		ms.pushMatrix();
		ms.translate(x, y);
		ms.scale(sc, sc);
		txt(ctx, s, 0, 0, c);
		ms.popMatrix();
	}

	private void circle(DrawContext ctx, int cx, int cy, int r, int c) {
		for (int dy = -r; dy <= r; dy++) {
			int hw = (int) Math.round(Math.sqrt(r * r - dy * dy));
			ctx.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, a(c));
		}
	}

	private float ease(String key, float target, float durMs) {
		float v = anim.getOrDefault(key, target);
		v += (target - v) * (1f - (float) Math.exp(-dt / Math.max(8f, durMs * 0.25f)));
		if (Math.abs(target - v) < 0.004f) v = target;
		anim.put(key, v);
		return v;
	}

	private void addHit(int x, int y, int w, int h, int kind, Module m, Setting s, int idx) {
		Hit hit = new Hit();
		hit.x = x; hit.y = y; hit.w = w; hit.h = h; hit.kind = kind; hit.module = m; hit.setting = s; hit.idx = idx;
		hit.clip = clip.clone();
		hits.add(hit);
	}

	private void setClip(DrawContext ctx, int x1, int y1, int x2, int y2) {
		clip = new int[]{x1, y1, x2, y2};
		if (animating) return;
		ctx.enableScissor(Math.round(x1 * gs), Math.round(y1 * gs), Math.round(x2 * gs), Math.round(y2 * gs));
	}

	private void endClip(DrawContext ctx) {
		clip = new int[]{0, 0, 99999, 99999};
		if (!animating) ctx.disableScissor();
	}

	private String keyName(int key) {
		if (key == -1) return "none";
		String n = InputUtil.fromKeyCode(new KeyInput(key, 0, 0)).getLocalizedText().getString().toUpperCase();
		return n.length() > 7 ? n.substring(0, 7) : n;
	}

	private void scrollbar(DrawContext ctx, int x, int top, int bottom, float shown, int max) {
		if (max <= 0) return;
		int trackH = bottom - top;
		int barH = Math.max(18, (int) (trackH * (trackH / (float) (trackH + max))));
		int barY = top + (int) ((trackH - barH) * (shown / max));
		rr(ctx, x, top, 3, trackH, 2, Colors.alpha(Colors.track(), (int) (120 * sbAlpha)));
		rr(ctx, x, barY, 3, barH, 2, Colors.alpha(Colors.wave(0.1f), (int) (255 * sbAlpha)));
	}

	private int cols(InterfaceModule im) {
		if (im == null) return 2;
		try {
			return MathHelper.clamp(Integer.parseInt(im.columns.get()), 1, 3);
		} catch (Exception e) {
			return 2;
		}
	}

	private float stagger(int index, InterfaceModule im) {
		if (im == null || !im.stagger.get()) return 1f;
		return MathHelper.clamp((tabMs - index * im.staggerSpeed.getF()) / 170f, 0f, 1f);
	}

	private static String groupName(Setting s) {
		if (s instanceof BoolSetting) return "Toggles";
		if (s instanceof NumberSetting) return "Values";
		if (s instanceof ModeSetting) return "Modes";
		if (s instanceof MultiSetting) return "Lists";
		return "Other";
	}

	// ------------------------------------------------------------------ settings widgets

	private void drawSwitch(DrawContext ctx, int x, int y, float t) {
		rr(ctx, x, y, 22, 10, 5, Colors.lerp(Colors.track(), Colors.C2, t));
		if (t > 0.02f) rr(ctx, x + 2, y + 1, 18, 8, 4, Colors.alpha(Colors.C2, (int) (255 * t)));
		rr(ctx, x + 1 + (int) (t * 12), y + 1, 8, 8, 4, 0xFFFFFFFF);
	}

	private int chipsHeight(String[] names, int w) {
		int cx = 0, rows = 1;
		for (String n : names) {
			int cw = textRenderer.getWidth(n) + 12;
			if (cx + cw > w && cx > 0) { rows++; cx = 0; }
			cx += cw + 4;
		}
		return rows * 15 + (rows - 1) * 3;
	}

	private void chips(DrawContext ctx, String[] names, IntPredicate on, int x, int y, int w, int kind, Module owner, Setting set, boolean live) {
		int cx = 0, cy = 0;
		for (int i = 0; i < names.length; i++) {
			int cw = textRenderer.getWidth(names[i]) + 12;
			if (cx + cw > w && cx > 0) { cx = 0; cy += 18; }
			int rx = x + cx, ry = y + cy;
			float t = ease(set.hashCode() + ":" + i, on.test(i) ? 1f : 0f, 150);
			rr(ctx, rx, ry, cw, 15, 3, Colors.lerp(Colors.chip(), Colors.C2, t));
			if (t > 0.02f) rr(ctx, rx + 2, ry + 1, cw - 4, 13, 3, Colors.alpha(Colors.C2, (int) (255 * t)));
			txt(ctx, names[i], rx + 6, ry + 4, Colors.lerp(Colors.dim(), Colors.text(), t));
			if (live) addHit(rx, ry, cw, 15, kind, owner, set, i);
			cx += cw + 4;
		}
	}

	private int rowH(Setting s, int w) {
		if (s instanceof BoolSetting) return 20;
		if (s instanceof NumberSetting) return 29;
		String[] names = s instanceof ModeSetting m ? m.modes : ((MultiSetting) s).names;
		return 14 + chipsHeight(names, w) + 7;
	}

	private int panelTotal(Module m, int w) {
		int total = 0;
		String last = null;
		for (Setting s : m.settings) {
			String g = groupName(s);
			if (!g.equals(last)) { total += 18; last = g; }
			total += rowH(s, w);
		}
		return total;
	}

	private int drawSetting(DrawContext ctx, Module owner, Setting s, int x, int y, int w, boolean live) {
		if (s instanceof BoolSetting b) {
			float t = ease(b.hashCode() + "b", b.get() ? 1f : 0f, 150);
			txt(ctx, b.name, x, y + 4, Colors.lerp(Colors.dim(), Colors.text(), t));
			drawSwitch(ctx, x + w - 24, y + 4, t);
			if (live) addHit(x - 2, y, w + 4, 18, K_BOOL, owner, b, 0);
		} else if (s instanceof NumberSetting n) {
			txt(ctx, n.name, x, y + 2, Colors.dim());
			String val = n.step >= 1 ? String.valueOf(n.getI()) : (n.step >= 0.1 ? String.format("%.1f", n.get()) : String.format("%.2f", n.get()));
			txt(ctx, val, x + w - textRenderer.getWidth(val), y + 2, Colors.faint());
			int sy = y + 16;
			float t = (float) ((n.get() - n.min) / (n.max - n.min));
			fill(ctx, x, sy, x + w, sy + 3, Colors.track());
			int fx = x + (int) (w * t);
			grad(ctx, x, sy, Math.max(x + 1, fx), sy + 3, Colors.C1, Colors.skeet(t));
			rr(ctx, MathHelper.clamp(fx - 3, x, x + w - 6), sy - 3, 6, 9, 3, 0xFFFFFFFF);
			if (live) addHit(x, sy - 5, w, 13, K_SLIDER, owner, n, 0);
		} else if (s instanceof ModeSetting md) {
			txt(ctx, md.name, x, y + 2, Colors.dim());
			chips(ctx, md.modes, i -> i == md.index(), x, y + 14, w, K_MODE, owner, md, live);
		} else if (s instanceof MultiSetting ms) {
			txt(ctx, ms.name, x, y + 2, Colors.dim());
			chips(ctx, ms.names, ms::get, x, y + 14, w, K_MULTI, owner, ms, live);
		}
		return rowH(s, w);
	}

	private void setSlider(Hit h, double mx) {
		NumberSetting n = (NumberSetting) h.setting;
		double t = MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0);
		n.set(n.min + t * (n.max - n.min));
	}

	// ------------------------------------------------------------------ render

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		long now = System.nanoTime();
		dt = Math.min(100f, (now - lastNano) / 1_000_000f);
		lastNano = now;

		InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);
		if (im != null) im.apply();
		gs = im != null ? MathHelper.clamp(im.guiScale.getI() / 100f, 0.4f, 1.1f) : 1f;
		readLayout(im);
		vw = Math.max(160, Math.round(width / gs));
		vh = Math.max(120, Math.round(height / gs));
		clampWindows();
		mx /= gs; my /= gs;

		float animMs = im != null ? im.animSpeed.getF() : 300f;
		int corner = im != null ? im.corner.getI() : 10;
		float glowAmt = im != null ? im.glow.getF() : 5f;

		hits.clear();
		pageRects.clear();
		tooltip = null;
		wx = mainX;
		wy = mainY;
		tabMs += dt;
		sbAlpha += (0.45f - sbAlpha) * 0.12f;

		float open;
		if (closing) {
			closeMs += dt;
			open = 1f - Math.min(1f, closeMs / Math.max(60f, animMs * 0.7f));
			if (closeMs >= animMs * 0.7f) { finishClose = true; close(); return; }
		} else {
			openMs += dt;
			open = Math.min(1f, openMs / Math.max(60f, animMs));
		}
		float eo = 1f - (1f - open) * (1f - open) * (1f - open);
		animating = open < 0.999f;
		ga = eo;

		ctx.fill(0, 0, width, height, Colors.alpha(0x000000, (int) (130 * eo)));
		for (int i = 0; i < particleCount && i < px.length; i++) {
			float sp = im != null ? im.particleSpeed.getF() / 100f : 1f;
			px[i] += pvx[i] * sp;
			py[i] += pvy[i] * sp;
			if (py[i] < 0) { py[i] = height; px[i] = (float) (Math.random() * width); }
			if (px[i] < 0) px[i] = width;
			if (px[i] > width) px[i] = 0;
			int s = 1 + (i % 2);
			ctx.fill((int) px[i], (int) py[i], (int) px[i] + s, (int) py[i] + s, Colors.alpha(Colors.skeet(i / (float) Math.max(1, particleCount)), (int) (130 * eo)));
		}

		var ms = ctx.getMatrices();
		ms.pushMatrix();
		ms.scale(gs, gs);
		if (im != null && im.anim.is("Tilt")) {
			float sc = 0.9f + 0.1f * eo;
			float cx = wx + winW / 2f, cy = wy + winH / 2f;
			ms.translate(cx, cy + (1f - eo) * 14f);
			ms.scale(sc, sc);
			ms.translate(-cx, -cy);
		} else if (im != null && im.anim.is("Scale")) {
			float sc = 0.92f + 0.08f * eo;
			float cx = wx + winW / 2f, cy = wy + winH / 2f;
			ms.translate(cx, cy);
			ms.scale(sc, sc);
			ms.translate(-cx, -cy);
		}

		boolean interactive = !animating;
		if (selected != null) panelModule = selected;
		float panelA = ease("panel", selected != null ? 1f : 0f, 220);

		drawWindow(ctx, mx, my, delta, im, interactive, corner, glowAmt);
		if (panelModule != null && panelA > 0.01f) drawPanel(ctx, mx, my, panelA, corner);
		drawRail(ctx, mx, my, im, corner);
		drawRipples(ctx);

		if (listening != null) {
			txtS(ctx, "press a key to bind " + listening.name + "   (esc = clear)", wx + winW / 2 - 100, wy + winH + 8, 0xFFFFD27A);
		}
		if (tooltip != null) {
			int tw = textRenderer.getWidth(tooltip) + 10;
			rr(ctx, mx + 10, my + 12, tw, 16, 5, Colors.alpha(0x0A0A10, 240));
			txt(ctx, tooltip, mx + 15, my + 16, Colors.text());
		}
		ms.popMatrix();
	}

	private void drawRipples(DrawContext ctx) {
		long now = System.currentTimeMillis();
		for (int i = ripples.size() - 1; i >= 0; i--) {
			Ripple r = ripples.get(i);
			float age = (now - r.t) / 420f;
			if (age >= 1f) { ripples.remove(i); continue; }
			int rad = 6 + (int) (age * 34);
			int alpha = (int) ((1f - age) * 110);
			for (int a = 0; a < 40; a++) {
				double ang = a / 40.0 * Math.PI * 2.0;
				int x = r.x + (int) Math.round(Math.cos(ang) * rad);
				int y = r.y + (int) Math.round(Math.sin(ang) * rad);
				ctx.fill(x, y, x + 2, y + 2, Colors.alpha(Colors.wave(0f), alpha));
			}
		}
	}

	private void addRipple(int x, int y, InterfaceModule im) {
		if (im == null || !im.ripples.get()) return;
		if (ripples.size() > 24) ripples.remove(0);
		Ripple r = new Ripple();
		r.x = x;
		r.y = y;
		r.t = System.currentTimeMillis();
		ripples.add(r);
	}

	// ---------- main window

	private void drawWindow(DrawContext ctx, int mx, int my, float delta, InterfaceModule im, boolean interactive, int corner, float glowAmt) {
		int styleA = im == null || im.style.is("Classic") ? 0xF8 : im.style.is("Liquid") ? 0xCC : 0x8C;
		int alpha = (int) (styleA * (im == null ? 100 : im.opacity.get()) / 100.0);

		int glowSteps = Math.round(glowAmt);
		for (int i = glowSteps; i >= 1; i--) rr(ctx, wx - i, wy - i, winW + i * 2, winH + i * 2, corner + 2, Colors.alpha(Colors.C2, 5));
		if (im == null || im.shadow.get()) {
			for (int i = 4; i >= 1; i--) rr(ctx, wx - i, wy + 2, winW + i * 2, winH + i * 2, corner + 2, Colors.alpha(0x000000, 8));
		}
		rr(ctx, wx - 1, wy - 1, winW + 2, winH + 2, corner + 1, Colors.alpha(Colors.C2, 64));
		rr(ctx, wx, wy, winW, winH, corner, Colors.alpha(Colors.bg(), alpha));
		if (im == null || im.headerLine.get()) {
			grad(ctx, wx + corner, wy, wx + winW - corner, wy + 1, Colors.alpha(Colors.C1, 150), Colors.alpha(Colors.C3, 150));
		}
		if (im != null && im.sheen.get()) {
			for (int i = 0; i < 26; i++) {
				int yy = wy + 2 + i * 2;
				if (yy >= wy + winH - 2) break;
				int al = (int) (10 * (1f - i / 26f));
				fill(ctx, wx + 2, yy, wx + winW - 2, yy + 2, Colors.alpha(0xFFFFFF, al));
			}
		}
		if (im != null && im.style.is("Liquid")) {
			grad(ctx, wx + 2, wy + 2, wx + winW - 2, wy + 64, Colors.alpha(Colors.C1, 30), Colors.alpha(Colors.C3, 30));
		}

		int cx = contentX(), cw = contentW();
		int hdr = wy + 36;
		int ftr = wy + winH - (im == null || im.footer.get() ? 22 : 8);
		fill(ctx, cx - 2, hdr, cx + cw + 2, hdr + 1, Colors.alpha(Colors.track(), 150));
		if (im == null || im.footer.get()) fill(ctx, cx - 2, ftr, cx + cw + 2, ftr + 1, Colors.alpha(Colors.track(), 150));

		String q = search.getText().trim().toLowerCase();
		boolean moduleGrid = !q.isEmpty() || (pages[page] != Category.SPOTIFY && pages[page] != Category.THEMES);

		int sw = Math.min(200, cw / 2);
		rr(ctx, cx, wy + 9, sw, 18, 9, Colors.alpha(Colors.chip(), 210));
		circle(ctx, cx + 11, wy + 18, 4, Colors.faint());
		circle(ctx, cx + 11, wy + 18, 2, Colors.alpha(Colors.bg(), 255));
		fill(ctx, cx + 14, wy + 21, cx + 17, wy + 22, Colors.faint());
		placeField(search, cx + 22, wy + 14, sw - 30);
		if (interactive) renderField(ctx, search, mx, my, delta);
		if (search.getText().isEmpty() && !search.isFocused()) txt(ctx, "Search...", cx + 22, wy + 14, Colors.faint());

		// breadcrumb
		String crumb = "lovenote";
		int crumbX = cx + sw + 10;
		txt(ctx, crumb, crumbX, wy + 14, Colors.alpha(Colors.faint(), 170));
		crumbX += textRenderer.getWidth(crumb) + 4;
		txt(ctx, "\u203A", crumbX, wy + 14, Colors.alpha(Colors.faint(), 120));
		crumbX += 8;
		txt(ctx, !q.isEmpty() ? "search" : pages[page].label.toLowerCase(), crumbX, wy + 14, Colors.wave(0f));

		int bx = wx + winW - 16;
		boolean cHov = interactive && mx >= bx - 4 && mx <= bx + 10 && my >= wy + 10 && my <= wy + 26;
		circle(ctx, bx + 3, wy + 18, 5, Colors.alpha(cHov ? 0xFFEF4444 : Colors.chip(), 255));
		addHit(bx - 3, wy + 10, 14, 14, K_CLOSE, null, null, 0);

		// title, sliding in on tab change
		float tSlide = MathHelper.clamp(tabMs / 220f, 0f, 1f);
		float tEase = 1f - (1f - tSlide) * (1f - tSlide);
		int titleY = wy + 46 + (int) ((1f - tEase) * 8f);
		la = Math.min(1f, tEase + 0.15f);
		String title = !q.isEmpty() ? "All" : pages[page].label;
		txtScaled(ctx, title, cx, titleY, Colors.text(), 1.32f);
		int count = visibleModules(q).size();
		String sub = count + (count == 1 ? " module" : " modules");
		int subX = cx + (int) (textRenderer.getWidth(title) * 1.32f) + 8;
		if (subX + textRenderer.getWidth(sub) < cx + cw) txt(ctx, sub, subX, titleY + 4, Colors.faint());
		la = 1f;

		int top = wy + 66, bottom = ftr - 6;
		if (q.isEmpty() && pages[page] == Category.HUD) {
			int cwid = textRenderer.getWidth("Edit layout") + 16;
			int ex = cx + cw - cwid;
			boolean hov = interactive && mx >= ex && mx <= ex + cwid && my >= wy + 44 && my <= wy + 62;
			float hv = ease("editbtn", hov ? 1f : 0f, 120);
			rr(ctx, ex, wy + 44, cwid, 18, 9, Colors.lerp(Colors.chip(), Colors.C2, hv * 0.55f));
			txt(ctx, "Edit layout", ex + 8, wy + 49, Colors.lerp(Colors.dim(), Colors.text(), hv));
			addHit(ex, wy + 44, cwid, 18, K_EDITOR, null, null, 0);
		}

		if (moduleGrid) {
			renderCards(ctx, mx, my, cx, top, cw, bottom, q, interactive, im, corner);
		} else if (pages[page] == Category.SPOTIFY) {
			renderSpotify(ctx, mx, my, delta, cx, top, cw, bottom, interactive, im, corner);
		} else {
			renderThemes(ctx, mx, my, delta, cx, top, cw, im, interactive, corner);
		}

		if (im == null || im.footer.get()) drawFooter(ctx, cx, cw, ftr, im);

		if (!interactive) return;
		if (pages[page] != Category.SPOTIFY || !q.isEmpty()) idField.setFocused(false);
		if (pages[page] != Category.THEMES || !q.isEmpty()) { primField.setFocused(false); secField.setFocused(false); }
	}

	private void drawFooter(DrawContext ctx, int cx, int cw, int ftr, InterfaceModule im) {
		int y = ftr + 7;
		txt(ctx, "lovenote", cx, y, Colors.faint());
		int left = cx + textRenderer.getWidth("lovenote") + 6;
		long secs = (System.currentTimeMillis() - sessionStart) / 1000L;
		String sess = String.format("%02d:%02d", secs / 60, secs % 60);
		txt(ctx, sess, left, y, Colors.alpha(Colors.faint(), 140));

		int enabled = 0;
		for (Module m : LovenoteClient.modules.all()) if (m.enabled) enabled++;
		String[] seg = { enabled + " on", client.getCurrentFps() + " fps", "ctrl+scroll" };
		int[] w = new int[seg.length];
		int total = 0;
		for (int i = 0; i < seg.length; i++) { w[i] = textRenderer.getWidth(seg[i]); total += w[i] + 10; }
		total -= 10;
		int x = cx + cw - total;
		for (int i = 0; i < seg.length; i++) {
			if (x > cx + 80) txt(ctx, seg[i], x, y, Colors.alpha(Colors.faint(), 160));
			x += w[i] + 10;
		}
	}

	private void placeField(TextFieldWidget f, int x, int y, int w) {
		f.setX(Math.round(x * gs));
		f.setY(Math.round(y * gs));
		f.setWidth(Math.round(w * gs));
		f.setEditableColor(Colors.text());
	}

	private void renderField(DrawContext ctx, TextFieldWidget f, int mx, int my, float delta) {
		if (ga <= 0.9f) return;
		var ms = ctx.getMatrices();
		ms.pushMatrix();
		ms.scale(1f / gs, 1f / gs);
		f.render(ctx, Math.round(mx * gs), Math.round(my * gs), delta);
		ms.popMatrix();
	}

	// ---------- module cards

	private List<Module> visibleModules(String q) {
		List<Module> out = new ArrayList<>();
		for (Module m : LovenoteClient.modules.all()) {
			if (m.hidden) continue;
			if (!q.isEmpty()) {
				if (m.name.toLowerCase().contains(q) || m.description.toLowerCase().contains(q)) out.add(m);
			} else if (m.category == pages[page] || m.alsoIn.contains(pages[page])) {
				out.add(m);
			}
		}
		InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);
		String mode = im != null ? im.sort.get() : "Name";
		if (mode.equals("Enabled first")) {
			out.sort((x, y) -> Boolean.compare(y.enabled, x.enabled) != 0
					? Boolean.compare(y.enabled, x.enabled)
					: x.name.compareToIgnoreCase(y.name));
		} else if (mode.equals("Category")) {
			out.sort((x, y) -> x.category == y.category
					? x.name.compareToIgnoreCase(y.name)
					: x.category.ordinal() - y.category.ordinal());
		} else {
			out.sort((x, y) -> x.name.compareToIgnoreCase(y.name));
		}
		return out;
	}

	private void renderCards(DrawContext ctx, int mx, int my, int cx, int top, int cw, int bottom, String q, boolean interactive, InterfaceModule im, int corner) {
		List<Module> list = visibleModules(q);
		int ncols = cols(im);
		boolean compact = im != null && im.compact.get();
		int gap = 7;
		int cardW = (cw - (ncols - 1) * gap) / ncols;
		int cardH = compact ? 30 : 42;
		int rows = (list.size() + ncols - 1) / ncols;
		maxScroll = Math.max(0, rows * (cardH + gap) - (bottom - top));
		scrollTarget = MathHelper.clamp(scrollTarget, 0, maxScroll);
		scrollShown += (scrollTarget - scrollShown) * (1f - (float) Math.exp(-dt / 40f));
		if (Math.abs(scrollTarget - scrollShown) < 0.2f) scrollShown = scrollTarget;

		if (mx > cx - 6 && mx < cx + cw + 12 && my > top && my < bottom) sbAlpha = 1f;

		boolean showDesc = im == null || im.descriptions.get();
		float lift = im != null ? im.hoverLift.getF() : 2f;
		int cr = Math.max(2, corner - 2);

		setClip(ctx, cx - 2, top - 2, cx + cw + 4, bottom);
		for (int i = 0; i < list.size(); i++) {
			Module m = list.get(i);
			int col = i % ncols;
			int x = cx + col * (cardW + gap);
			int y = top + (i / ncols) * (cardH + gap) - Math.round(scrollShown);
			if (y + cardH < top - 8 || y > bottom + 8) continue;

			float st = stagger(i, im);
			la = st;
			int sy = y + (int) ((1f - st) * 10f);

			boolean hov = interactive && mx >= x && mx < x + cardW && my >= sy && my < sy + cardH && my >= top && my <= bottom;
			float hv = ease(m.name + ":h", hov ? 1f : 0f, 130);
			float on = ease(m.name + ":s", m.enabled ? 1f : 0f, 170);
			float sel = ease(m.name + ":sel", selected == m ? 1f : 0f, 170);

			int ly = sy - (int) (hv * lift);
			if (sel > 0.02f) rr(ctx, x - 1, ly - 1, cardW + 2, cardH + 2, cr + 1, Colors.alpha(Colors.C2, (int) (150 * sel)));
			if (hv > 0.02f) rr(ctx, x - 1, ly - 1, cardW + 2, cardH + 2, cr + 1, Colors.alpha(Colors.wave(0f), (int) (70 * hv)));

			String cardStyle = im != null ? im.card.get() : "Solid";
			if (cardStyle.equals("Outline")) {
				rr(ctx, x, ly, cardW, cardH, cr, Colors.lerp(Colors.bg(), Colors.cardHover(), hv));
				rr(ctx, x, ly, cardW, cardH, cr, Colors.alpha(Colors.C2, (int) (40 + 90 * hv + 70 * on)));
			} else if (cardStyle.equals("Glass")) {
				rr(ctx, x, ly, cardW, cardH, cr, Colors.alpha(Colors.card(), (int) (150 + 70 * hv)));
			} else {
				rr(ctx, x, ly, cardW, cardH, cr, Colors.lerp(Colors.card(), Colors.cardHover(), hv));
			}
			if (on > 0.02f) rr(ctx, x, ly + 9, 2, cardH - 18, 1, Colors.alpha(Colors.wave(x * 0.004f), (int) (255 * on)));

			circle(ctx, x + 13, ly + (compact ? 15 : 13), 2, Colors.lerp(Colors.faint(), Colors.wave(0f), on));
			int textW = cardW - 15 - 44;
			String name = textRenderer.trimToWidth(m.name, textW);
			txt(ctx, name, x + 20, ly + (compact ? 11 : 9), Colors.lerp(Colors.dim(), Colors.text(), Math.max(on, hv * 0.8f)));
			if (!compact && showDesc) {
				txt(ctx, textRenderer.trimToWidth(m.description, textW), x + 20, ly + 24, Colors.faint());
			}
			drawSwitch(ctx, x + cardW - 33, ly + (compact ? 10 : 16), on);
			if (m.key != -1 && !compact) {
				String kb = "[" + keyName(m.key) + "]";
				txt(ctx, kb, x + cardW - 38 - textRenderer.getWidth(kb), ly + 26, Colors.alpha(Colors.faint(), 150));
			}
			txt(ctx, "\u203A", x + cardW - 12, ly + (compact ? 11 : 16), Colors.alpha(Colors.faint(), (int) (120 + 100 * hv)));

			addHit(x, ly, cardW, cardH, K_CARD, m, null, 0);
			addHit(x + cardW - 37, ly + (compact ? 4 : 10), 34, 22, K_SWITCH, m, null, 0);

			if (hov && im != null && im.tooltips.get() && !name.equals(m.name)) tooltip = m.name;
			la = 1f;
		}
		endClip(ctx);
		scrollbar(ctx, cx + cw + 6, top, bottom, scrollShown, maxScroll);
		if (list.isEmpty()) txt(ctx, "No modules found", cx + 4, top + 8, Colors.faint());
	}

	// ---------- rail

	private void drawRail(DrawContext ctx, int mx, int my, InterfaceModule im, int corner) {
		int rx = railX();
		float speed = im != null ? im.sidebar.getF() : 300f;
		boolean hover = dragWin == 0 && !animating && mx >= rx && mx < rx + railW && my >= wy && my < wy + winH;
		float sb = ease("sb", hover ? 1f : 0f, speed);
		int sbw = RAIL_MIN + (int) ((railW - RAIL_MIN) * sb);
		int sx = railRight ? rx + railW - sbw : rx;

		if (sb > 0.02f) {
			for (int i = 5; i >= 1; i--) rr(ctx, sx + sbw - 2, wy + 4, i * 2 + 2, winH - 8, 8, Colors.alpha(0x000000, (int) (9 * sb)));
		}
		rr(ctx, sx, wy, sbw, winH, corner, Colors.dark ? 0xFA0C0C12 : 0xFAEDEDF4);
		grad(ctx, sx + 2, wy + 8, sx + sbw - 2, wy + 9, Colors.alpha(Colors.C1, 90), Colors.alpha(Colors.C3, 90));

		// ---- spinning 3D drain gang logo
		int logoCx = sx + 23;
		int logoCy = wy + 24;
		boolean showLogo = im == null || im.logo.get();
		int logoSize = im != null ? im.logoSize.getI() : 26;
		long spinMs = im != null ? (long) im.logoSpin.getI() : 2600L;
		if (showLogo) {
			if (im == null || im.logoGlow.get()) {
				for (int g = 12; g >= 1; g--) {
					int al = (int) (6 * (1f - g / 13f) + 3);
					circle(ctx, logoCx, logoCy, g + 8, Colors.alpha(Colors.C2, al));
				}
			}
			DrainLogo.draw3D(ctx, logoCx, logoCy, logoSize, Colors.wave(0f), DrainLogo.spin(spinMs), 4);
		} else {
			circle(ctx, logoCx, logoCy, 9, Colors.wave(0f));
			txtS(ctx, "\u2665", logoCx - 3, logoCy - 4, 0xFFFFFFFF);
		}
		if (sb > 0.05f) txtS(ctx, "lovenote", sx + 42, wy + 20, Colors.alpha(Colors.text(), (int) (255 * sb)));

		// ---- rows
		String q = search.getText().trim();
		int avail = winH - 56 - 40;
		int rowH = MathHelper.clamp(avail / Math.max(1, pages.length), 16, 24);
		int[] ys = new int[pages.length];
		int y = wy + 56;
		for (int i = 0; i < pages.length; i++) {
			if (i == 7 || i == 9) y += 5;
			ys[i] = y;
			y += rowH;
		}

		// sliding selection pill
		int targetY = ys[MathHelper.clamp(page, 0, pages.length - 1)];
		pillY = pillY < 0f ? targetY : pillY + (targetY - pillY) * (1f - (float) Math.exp(-dt / 70f));
		if (q.isEmpty() && sb > 0.05f) {
			rr(ctx, sx + 5, Math.round(pillY), sbw - 10, rowH, 6, Colors.alpha(Colors.C2, (int) (70 * sb)));
			rr(ctx, sx + 5, Math.round(pillY) + rowH / 2 - 7, 2, 14, 1, Colors.alpha(Colors.wave(0f), (int) (255 * sb)));
		}

		for (int i = 0; i < pages.length; i++) {
			int ry = ys[i];
			boolean hov = hover && mx >= sx + 5 && mx < sx + sbw - 5 && my >= ry && my < ry + rowH;
			float hv = ease("pg" + i, hov ? 1f : 0f, 120);
			float slide = (1f - MathHelper.clamp((tabMs - i * 12f) / 220f, 0f, 1f)) * 10f;
			if (hv > 0.02f && i != page) rr(ctx, sx + 5, ry, sbw - 10, rowH, 6, Colors.alpha(Colors.C2, (int) (26 * hv)));
			if (i < icons.length) ctx.drawItem(icons[i], sx + 13 + (int) slide, ry + (rowH - 16) / 2);
			if (sb > 0.05f) txt(ctx, pages[i].label, sx + 42, ry + (rowH - 8) / 2, Colors.alpha(Colors.lerp(Colors.dim(), Colors.text(), Math.max(hv * 0.7f, i == page ? 1f : 0f)), (int) (255 * sb)));
			pageRects.add(new int[]{sx + 5, ry, sbw - 10, rowH, i});
		}

		int ey = wy + winH - 36;
		boolean ehov = hover && mx >= sx + 5 && mx < sx + sbw - 5 && my >= ey && my < ey + rowH;
		float eh = ease("pgE", ehov ? 1f : 0f, 120);
		if (eh > 0.02f) rr(ctx, sx + 5, ey, sbw - 10, rowH, 6, Colors.alpha(Colors.C2, (int) (28 * eh)));
		ctx.drawItem(editorIcon, sx + 13, ey + (rowH - 16) / 2);
		if (sb > 0.05f) txt(ctx, "HUD editor", sx + 42, ey + (rowH - 8) / 2, Colors.alpha(Colors.dim(), (int) (255 * sb)));
		pageRects.add(new int[]{sx + 5, ey, sbw - 10, rowH, -2});
	}

	// ---------- settings window

	private void drawPanel(DrawContext ctx, int mx, int my, float panelA, int corner) {
		Module m = panelModule;
		float saved = ga;
		ga = saved * panelA;

		var ms = ctx.getMatrices();
		float sc = 0.94f + 0.06f * panelA;
		float ccx = panX + PW / 2f, ccy = panY + PH / 2f;
		ms.pushMatrix();
		ms.translate(ccx, ccy);
		ms.scale(sc, sc);
		ms.translate(-ccx, -ccy);

		int px = panX + (int) ((1f - panelA) * 22), py = panY;

		for (int i = 7; i >= 1; i--) rr(ctx, px - i, py - i, PW + i * 2, PH + i * 2, corner, Colors.alpha(Colors.C2, 5));
		rr(ctx, px - 1, py - 1, PW + 2, PH + 2, corner + 1, Colors.alpha(Colors.C2, 70));
		rr(ctx, px, py, PW, PH, corner, Colors.alpha(Colors.card(), 0xF8));
		addHit(px, py, PW, PH, K_NONE, null, null, 0);

		txtS(ctx, m.name, px + 13, py + 10, Colors.text());
		txt(ctx, textRenderer.trimToWidth(m.description, PW - 84), px + 13, py + 23, Colors.faint());

		float on = ease(m.name + ":s", m.enabled ? 1f : 0f, 170);
		drawSwitch(ctx, px + PW - 74, py + 12, on);
		addHit(px + PW - 77, py + 8, 28, 18, K_PTOGGLE, m, null, 0);
		circle(ctx, px + PW - 40, py + 17, 4, 0xFF3DD68C);
		circle(ctx, px + PW - 28, py + 17, 4, 0xFFF5B83D);
		circle(ctx, px + PW - 16, py + 17, 4, 0xFFEF4444);
		addHit(px + PW - 22, py + 11, 12, 12, K_PCLOSE, m, null, 0);

		fill(ctx, px + 9, py + 37, px + PW - 9, py + 38, Colors.alpha(Colors.track(), 170));

		int top = py + 42, bottom = py + PH - 34;
		int total = 4 + panelTotal(m, PW - 26);
		panelMax = Math.max(0, total - (bottom - top));
		panelScrollTarget = MathHelper.clamp(panelScrollTarget, 0, panelMax);
		panelScrollShown += (panelScrollTarget - panelScrollShown) * (1f - (float) Math.exp(-dt / 40f));
		if (Math.abs(panelScrollTarget - panelScrollShown) < 0.2f) panelScrollShown = panelScrollTarget;

		setClip(ctx, px, top, px + PW, bottom);
		int y = top + 4 - Math.round(panelScrollShown);
		String lastGroup = null;
		for (Setting s : m.settings) {
			String g = groupName(s);
			if (!g.equals(lastGroup)) {
				if (lastGroup != null) fill(ctx, px + 13, y - 2, px + PW - 13, y - 1, Colors.alpha(Colors.track(), 120));
				y += 4;
				txt(ctx, g, px + 13, y, Colors.alpha(Colors.faint(), 190));
				y += 14;
				lastGroup = g;
			}
			y += drawSetting(ctx, m, s, px + 13, y, PW - 26, true);
		}
		endClip(ctx);
		scrollbar(ctx, px + PW - 6, top, bottom, panelScrollShown, panelMax);

		int by = py + PH - 27;
		fill(ctx, px + 9, by - 6, px + PW - 9, by - 5, Colors.alpha(Colors.track(), 170));
		txt(ctx, "Keybind", px + 13, by + 3, Colors.dim());
		String kn = listening == m ? "..." : keyName(m.key);
		int kw = textRenderer.getWidth(kn) + 16;
		boolean khov = mx >= px + PW - 13 - kw && mx <= px + PW - 13 && my >= by && my <= by + 16;
		float kh = ease("bind", khov || listening == m ? 1f : 0f, 120);
		rr(ctx, px + PW - 13 - kw, by, kw, 16, 8, Colors.lerp(Colors.chip(), Colors.C2, kh * 0.7f));
		txt(ctx, kn, px + PW - 13 - kw + 8, by + 4, Colors.lerp(Colors.text(), 0xFF101015, kh * 0.7f));
		addHit(px + PW - 13 - kw, by, kw, 16, K_BIND, m, null, 0);
		ms.popMatrix();
		ga = saved;
	}

	// ---------- themes page

	private int themeCard(DrawContext ctx, int x, int y, int w, String title, InterfaceModule im, boolean interactive, int corner, Setting... settings) {
		int h = 26;
		for (Setting s : settings) h += rowH(s, w - 22);
		rr(ctx, x, y, w, h, corner - 2, Colors.card());
		rr(ctx, x + 11, y + 9, 7, 7, 2, Colors.wave(0f));
		txt(ctx, title, x + 24, y + 8, Colors.text());
		int yy = y + 24;
		for (Setting s : settings) yy += drawSetting(ctx, im, s, x + 12, yy, w - 22, interactive);
		return h;
	}

	private void renderThemes(DrawContext ctx, int mx, int my, float delta, int cx, int cy, int cw, InterfaceModule im, boolean interactive, int corner) {
		if (im == null) return;
		int colW = (cw - 8) / 2, x1 = cx, x2 = cx + colW + 8;
		int accRows = (Colors.PRESETS.length + ACC_COLS - 1) / ACC_COLS;
		int accentH = 100 + accRows * 18;

		int leftH = 4 + (26 + rowH(im.main, colW - 22) + rowH(im.style, colW - 22) + rowH(im.card, colW - 22)
				+ rowH(im.opacity, colW - 22) + rowH(im.glow, colW - 22) + rowH(im.shadow, colW - 22)
				+ rowH(im.headerLine, colW - 22) + rowH(im.sheen, colW - 22)) + 6 + accentH + 6;
		int rightH = 4 + (26 + rowH(im.anim, colW - 22) + rowH(im.animSpeed, colW - 22) + rowH(im.sidebar, colW - 22)
				+ rowH(im.stagger, colW - 22) + rowH(im.staggerSpeed, colW - 22) + rowH(im.hoverLift, colW - 22)
				+ rowH(im.ripples, colW - 22)) + 6;

		int top = cy + 2, bottom = wy + winH - 28;
		maxScroll = Math.max(0, Math.max(leftH, rightH) - (bottom - top));
		scrollShown += (scrollTarget - scrollShown) * (1f - (float) Math.exp(-dt / 40f));
		if (Math.abs(scrollTarget - scrollShown) < 0.2f) scrollShown = scrollTarget;

		setClip(ctx, cx - 2, top - 2, cx + cw + 4, bottom);

		int y = top - Math.round(scrollShown);
		y += themeCard(ctx, x1, y, colW, "Logo", im, interactive, corner,
				im.logo, im.logoSpin, im.logoSize, im.logoGlow) + 6;
		y += themeCard(ctx, x1, y, colW, "Window", im, interactive, corner,
				im.guiScale, im.windowWidth, im.windowHeight, im.corner, im.railWidth, im.railSide, im.footer) + 6;
		y += themeCard(ctx, x1, y, colW, "Content", im, interactive, corner,
				im.columns, im.sort, im.compact, im.descriptions, im.tooltips) + 6;

		int h = accentH;
		int accentY = y;
		rr(ctx, x1, accentY, colW, h, corner - 2, Colors.card());
		rr(ctx, x1 + 11, accentY + 9, 7, 7, 2, Colors.wave(0f));
		txt(ctx, "Accent", x1 + 24, accentY + 8, Colors.text());
		String modeLbl = im.accent.is("Custom") ? "custom" : im.accent.get();
		txt(ctx, modeLbl, x1 + colW - 12 - textRenderer.getWidth(modeLbl), accentY + 8, Colors.faint());

		int sq = 15, sgap = 3;
		int gx = x1 + 12, gy = accentY + 26;
		for (int i = 0; i < Colors.PRESETS.length; i++) {
			int r = i / ACC_COLS, c = i % ACC_COLS;
			int bx = gx + c * (sq + sgap);
			int by = gy + r * (sq + sgap);
			boolean sel = im.accent.index() == i;
			if (sel) rr(ctx, bx - 2, by - 2, sq + 4, sq + 4, 4, Colors.alpha(Colors.text(), 210));
			grad(ctx, bx, by, bx + sq, by + sq, 0xFF000000 | Colors.PRESETS[i][0], 0xFF000000 | Colors.PRESETS[i][1]);
			float hv = ease("acc" + i, sel ? 1f : 0f, 140);
			if (hv > 0.01f && !sel) rr(ctx, bx, by, sq, sq, 3, Colors.alpha(Colors.wave(0f), (int) (70 * hv)));
			addHit(bx, by, sq, sq, K_ACCENT, null, null, i);
			if (interactive && mx >= bx && mx < bx + sq && my >= by && my < by + sq) tooltip = Colors.PRESET_NAMES[i];
		}

		int fy = gy + accRows * (sq + sgap) + 8;
		fill(ctx, x1 + 12, fy - 5, x1 + colW - 12, fy - 4, Colors.alpha(Colors.track(), 150));
		txt(ctx, "Custom hex", x1 + 12, fy, Colors.faint());

		int fw = (colW - 34) / 2;
		int fx1 = x1 + 12, fx2 = fx1 + fw + 10;
		int fy2 = fy + 12;
		settingFields = true;
		if (!primField.isFocused() && !primField.getText().equalsIgnoreCase(im.customPrimary)) primField.setText(im.customPrimary);
		if (!secField.isFocused() && !secField.getText().equalsIgnoreCase(im.customSecondary)) secField.setText(im.customSecondary);
		settingFields = false;

		rr(ctx, fx1, fy2, fw, 16, 6, Colors.alpha(Colors.chip(), 220));
		rr(ctx, fx1, fy2, 3, 16, 1, Colors.alpha(0xFF000000 | im.customPrimaryInt(), 255));
		txt(ctx, "#", fx1 + 4, fy2 + 4, Colors.faint());
		placeField(primField, fx1 + 10, fy2 + 4, fw - 14);
		if (interactive) renderField(ctx, primField, mx, my, delta);

		rr(ctx, fx2, fy2, fw, 16, 6, Colors.alpha(Colors.chip(), 220));
		rr(ctx, fx2, fy2, 3, 16, 1, Colors.alpha(0xFF000000 | im.customSecondaryInt(), 255));
		txt(ctx, "#", fx2 + 4, fy2 + 4, Colors.faint());
		placeField(secField, fx2 + 10, fy2 + 4, fw - 14);
		if (interactive) renderField(ctx, secField, mx, my, delta);

		int uy = fy2 + 22;
		boolean cSel = im.accent.is("Custom");
		boolean uh = interactive && mx >= x1 + 12 && mx <= x1 + fw * 2 + 22 && my >= uy && my <= uy + 16;
		float uhv = ease("useCustom", uh ? 1f : 0f, 120);
		rr(ctx, x1 + 12, uy, fw * 2 + 10, 16, 8, Colors.lerp(cSel ? Colors.C2 : Colors.chip(), Colors.C2, uhv * 0.6f));
		String ul = cSel ? "Custom active" : "Use custom";
		txt(ctx, ul, x1 + 12 + (fw * 2 + 10 - textRenderer.getWidth(ul)) / 2, uy + 4,
				Colors.lerp(Colors.text(), 0xFF101015, uhv * 0.7f));
		addHit(x1 + 12, uy, fw * 2 + 10, 16, K_ACCENT, null, null, Colors.PRESETS.length);
		int leftBottom = accentY + h + 6;

		int ry = top - Math.round(scrollShown);
		ry += themeCard(ctx, x2, ry, colW, "Animation", im, interactive, corner,
				im.anim, im.animSpeed, im.sidebar, im.stagger, im.staggerSpeed, im.hoverLift, im.ripples) + 6;
		ry += themeCard(ctx, x2, ry, colW, "Style", im, interactive, corner,
				im.main, im.style, im.card, im.opacity, im.glow, im.shadow, im.headerLine, im.sheen) + 6;
		ry += themeCard(ctx, x2, ry, colW, "Background", im, interactive, corner,
				im.particles, im.particleSpeed) + 6;
		int rightBottom = ry;

		endClip(ctx);
		maxScroll = Math.max(0, Math.max(leftBottom, rightBottom) - (bottom - top));
		scrollTarget = MathHelper.clamp(scrollTarget, 0, maxScroll);
		scrollbar(ctx, cx + cw + 6, top, bottom, scrollShown, maxScroll);
	}

	// ---------- spotify page

	private void control(DrawContext ctx, int x, int y, int w, String glyph, int kind, boolean interactive, double mx, double my) {
		boolean hov = interactive && mx >= x && mx <= x + w && my >= y && my <= y + 22;
		float h = ease("spc" + kind, hov ? 1f : 0f, 110);
		rr(ctx, x, y, w, 22, 7, Colors.lerp(Colors.chip(), Colors.C2, h * 0.55f));
		txt(ctx, glyph, x + (w - textRenderer.getWidth(glyph)) / 2, y + 7, Colors.lerp(Colors.dim(), Colors.text(), h));
		addHit(x, y, w, 22, kind, null, null, 0);
	}

	private void seek(Hit h, double mx) {
		SpotifyManager sp = SpotifyManager.I;
		if (sp.durationMs <= 0) return;
		double t = MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0);
		sp.seek((long) (t * sp.durationMs));
	}

	private void vol(Hit h, double mx) {
		localVol = (int) Math.round(MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0) * 100.0);
		SpotifyManager.I.setVolume(localVol);
	}

	private void renderSpotify(DrawContext ctx, int mx, int my, float delta, int cx, int top, int cw, int bottom,
			boolean interactive, InterfaceModule im, int corner) {
		SpotifyManager sp = SpotifyManager.I;
		LyricsManager L = LyricsManager.I;
		boolean live = sp.connected && sp.hasTrack;

		int cardW = Math.min(cw, 470);
		int x = cx + (cw - cardW) / 2;
		int y = top + 2;
		int cardH = Math.min(bottom - y - 2, 336);
		rr(ctx, x, y, cardW, cardH, corner - 2, Colors.card());
		grad(ctx, x + 10, y, x + cardW - 10, y + 2, Colors.C1, Colors.C3);

		int art = 96;
		int ax = x + 16, ay = y + 18;
		if (live && sp.artTexture != null) {
			ctx.drawTexture(RenderPipelines.GUI_TEXTURED, sp.artTexture, ax, ay, 0, 0, art, art, 64, 64, 64, 64);
		} else {
			rr(ctx, ax, ay, art, art, 10, Colors.alpha(Colors.chip(), 230));
			txtScaled(ctx, "\u266A", ax + 36, ay + 26, Colors.faint(), 3.2f);
		}

		int tx = ax + art + 16;
		int tw = x + cardW - 16 - tx;
		txt(ctx, "NOW PLAYING", tx, y + 18, Colors.alpha(Colors.faint(), 190));
		SpotifyHud.marquee(ctx, textRenderer, live ? sp.title : "Nothing playing", tx, y + 32, tw, Colors.text());
		SpotifyHud.marquee(ctx, textRenderer,
				live ? sp.artist : (sp.connected ? "Open Spotify and press play" : "Bridge not connected"),
				tx, y + 45, tw, Colors.dim());
		if (live && !sp.album.isEmpty()) txt(ctx, textRenderer.trimToWidth(sp.album, tw), tx, y + 58, Colors.faint());

		int barY = ay + art + 14;
		long prog = live ? sp.progress() : 0;
		float frac = live && sp.durationMs > 0 ? MathHelper.clamp(prog / (float) sp.durationMs, 0f, 1f) : 0f;
		int barX = x + 16, barW = cardW - 32;
		rr(ctx, barX, barY, barW, 5, 2, Colors.track());
		if (frac > 0f) grad(ctx, barX, barY, barX + Math.max(2, (int) (barW * frac)), barY + 5, Colors.C1, Colors.C3);
		circle(ctx, MathHelper.clamp(barX + (int) (barW * frac), barX + 2, barX + barW - 2), barY + 2, 3, 0xFFFFFFFF);
		addHit(barX - 4, barY - 4, barW + 8, 13, SP_SEEK, null, null, 0);
		txt(ctx, SpotifyHud.fmt(prog), barX, barY + 8, Colors.faint());
		String dur = SpotifyHud.fmt(live ? sp.durationMs : 0);
		txt(ctx, dur, barX + barW - textRenderer.getWidth(dur), barY + 8, Colors.faint());

		int cy2 = barY + 28;
		int bw = 34, gap = 10;
		int bx = x + (cardW - (bw * 3 + gap * 2)) / 2;
		control(ctx, bx, cy2, bw, "\u23EE", SP_PREV, interactive, mx, my);
		control(ctx, bx + bw + gap, cy2, bw, live && sp.playing ? "\u23F8" : "\u25B6", SP_PLAY, interactive, mx, my);
		control(ctx, bx + (bw + gap) * 2, cy2, bw, "\u23ED", SP_NEXT, interactive, mx, my);

		int volY = cy2 + 42;
		txt(ctx, "Volume", x + 16, volY, Colors.dim());
		if (localVol < 0) localVol = 100;
		int vx = x + 70, vwid = cardW - 32 - 54 - 36;
		rr(ctx, vx, volY + 3, vwid, 3, 1, Colors.track());
		grad(ctx, vx, volY + 3, vx + Math.max(2, (int) (vwid * localVol / 100f)), volY + 6, Colors.C1, Colors.C3);
		addHit(vx - 4, volY - 3, vwid + 8, 12, SP_VOL, null, null, 0);
		txt(ctx, localVol + "%", vx + vwid + 8, volY, Colors.faint());

		int conY = volY + 22;
		String cbtn = sp.connected ? "Disconnect" : "Connect";
		int cbw = textRenderer.getWidth(cbtn) + 20;
		boolean chov = interactive && mx >= x + 16 && mx <= x + 16 + cbw && my >= conY && my <= conY + 18;
		float ch = ease("spcon", chov ? 1f : 0f, 120);
		rr(ctx, x + 16, conY, cbw, 18, 9, Colors.lerp(Colors.chip(), Colors.C2, ch * 0.6f));
		txt(ctx, cbtn, x + 26, conY + 5, Colors.lerp(Colors.text(), 0xFF101015, ch * 0.7f));
		addHit(x + 16, conY, cbw, 18, sp.connected ? SP_DISCONNECT : SP_CONNECT, null, null, 0);
		txt(ctx, textRenderer.trimToWidth(sp.status, cardW - cbw - 40), x + 16 + cbw + 10, conY + 5, Colors.faint());

		int ly = conY + 28;
		Lyrics lyricMod = LovenoteClient.modules.get(Lyrics.class);
		boolean lyrOn = lyricMod != null && lyricMod.enabled;
		drawSwitch(ctx, x + cardW - 38, ly + 1, ease("splyr", lyrOn ? 1f : 0f, 150));
		txt(ctx, "Lyrics HUD", x + 16, ly, lyrOn ? Colors.text() : Colors.dim());
		addHit(x + 16, ly - 4, cardW - 32, 18, K_LYRICS, null, null, 0);

		if (L.hasLyrics()) {
			int shown = 0;
			for (int i = -1; i <= 2 && shown < 3; i++) {
				String t = L.lineAt(i);
				if (t.isEmpty()) continue;
				int col = i == 0 ? Colors.wave(0f) : (i < 0 ? Colors.faint() : Colors.dim());
				txt(ctx, textRenderer.trimToWidth(t, cardW - 32), x + 16, ly + 20 + shown * 13, col);
				shown++;
			}
		} else {
			txt(ctx, textRenderer.trimToWidth(L.status, cardW - 32), x + 16, ly + 20, Colors.faint());
		}
	}

	// ---------- input

	private boolean over(Hit h, double mx, double my) {
		if (mx < h.clip[0] || mx > h.clip[2] || my < h.clip[1] || my > h.clip[3]) return false;
		return mx >= h.x && mx <= h.x + h.w && my >= h.y && my <= h.y + h.h;
	}

	private boolean overField(TextFieldWidget f, double mx, double my) {
		return mx >= f.getX() / gs - 2 && mx <= (f.getX() + f.getWidth()) / gs + 2
				&& my >= f.getY() / gs - 2 && my <= (f.getY() + 12) / gs + 2;
	}

	private boolean inSearch(double mx, double my) {
		int swid = Math.min(200, contentW() / 2);
		return mx >= contentX() && mx <= contentX() + swid && my >= wy + 9 && my <= wy + 27;
	}

	private void applyAccent(InterfaceModule im, int idx) {
		if (im == null) return;
		if (idx >= 0 && idx < Colors.PRESETS.length) im.accent.setIndex(idx);
		else im.accent.set("Custom");
		im.apply();
	}

	private void changed(Module m) {
		if (m != null && m.enabled) {
			m.onDisable();
			m.onEnable();
		}
	}

	@Override
	public void removed() {
		Config.save();
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		int btn = button;
		double mx = mouseX;
		double my = mouseY;
		mx /= gs;
		my /= gs;
		InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);

		if (listening != null) {
			listening.key = button == 1 ? -1 : GLFW.GLFW_MOUSE_BUTTON_LEFT;
			listening = null;
			return true;
		}
		if (button == 1 && selected != null) {
			selected = null;
			panelModule = null;
			return true;
		}
		if (button != 0 && button != 1) return super.mouseClicked(new Click(mx, my, click.buttonInfo()), doubled);

		search.setFocused(false);
		primField.setFocused(false);
		secField.setFocused(false);
		idField.setFocused(false);

		if (button == 0 && pages[page] == Category.THEMES && search.getText().isEmpty()) {
			if (overField(primField, mx, my)) { primField.setFocused(true); return true; }
			if (overField(secField, mx, my)) { secField.setFocused(true); return true; }
		}
		if (button == 0 && inSearch(mx, my)) { search.setFocused(true); return true; }

		for (int[] r : pageRects) {
			if (mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3]) {
				if (r[4] == -2) {
					client.setScreen(new com.lovenote.gui.HudEditorScreen());
				} else {
					page = r[4];
					tabMs = 0;
					scrollTarget = 0;
					scrollShown = 0;
					pillY = -1f;
					search.setText("");
				}
				return true;
			}
		}

		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit hit = hits.get(i);
			if (!over(hit, mx, my)) continue;
			if (hit.kind == K_NONE) {
				if (my > panY + 34) return true;
				continue;
			}
			addRipple((int) mx, (int) my, im);
			switch (hit.kind) {
				case K_CARD -> {
					selected = hit.module;
					panelModule = hit.module;
					panelScrollTarget = 0;
				}
				case K_SWITCH -> hit.module.toggle();
				case K_BOOL -> {
					((BoolSetting) hit.setting).toggle();
					changed(hit.module);
				}
				case K_SLIDER -> {
					dragging = hit;
					setSlider(hit, mx);
					changed(hit.module);
				}
				case K_MODE -> {
					((ModeSetting) hit.setting).setIndex(hit.idx);
					changed(hit.module);
				}
				case K_MULTI -> {
					((MultiSetting) hit.setting).toggle(hit.idx);
					changed(hit.module);
				}
				case K_PTOGGLE -> hit.module.toggle();
				case K_PCLOSE -> {
					selected = null;
					panelModule = null;
				}
				case K_BIND -> listening = hit.module;
				case K_ACCENT -> applyAccent(im, hit.idx);
				case K_EDITOR -> client.setScreen(new com.lovenote.gui.HudEditorScreen());
				case K_CLOSE -> close();
				case K_LYRICS -> {
					Lyrics lyr = LovenoteClient.modules.get(Lyrics.class);
					if (lyr != null) lyr.toggle();
				}
				case SP_CONNECT -> SpotifyManager.I.connect();
				case SP_DISCONNECT -> SpotifyManager.I.disconnect();
				case SP_PREV -> SpotifyManager.I.previous();
				case SP_PLAY -> SpotifyManager.I.togglePlay();
				case SP_NEXT -> SpotifyManager.I.next();
				case SP_SEEK -> {
					dragging = hit;
					seek(hit, mx);
				}
				case SP_VOL -> {
					dragging = hit;
					vol(hit, mx);
				}
				default -> { }
			}
			return true;
		}

		if (button == 0 && selected != null && mx >= panX && mx <= panX + PW && my >= panY && my <= panY + 34) {
			dragWin = 2;
			grabX = (int) mx - panX;
			grabY = (int) my - panY;
			return true;
		}
		if (button == 0 && mx >= wx && mx <= wx + winW && my >= wy && my <= wy + 34) {
			dragWin = 1;
			grabX = (int) mx - wx;
			grabY = (int) my - wy;
			return true;
		}
		return super.mouseClicked(new Click(mx, my, click.buttonInfo()), doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		int btn = button;
		double mx = mouseX;
		double my = mouseY;
		double deltaX = offsetX;
		double deltaY = offsetY;
		double dx = offsetX;
		double dy = offsetY;
		mx /= gs;
		my /= gs;
		if (dragWin == 1) {
			mainX = (int) mx - grabX;
			mainY = (int) my - grabY;
			clampWindows();
			return true;
		}
		if (dragWin == 2) {
			panX = (int) mx - grabX;
			panY = (int) my - grabY;
			clampWindows();
			return true;
		}
		if (dragging != null) {
			if (dragging.kind == K_SLIDER) {
				setSlider(dragging, mx);
				changed(dragging.module);
				return true;
			}
			if (dragging.kind == SP_SEEK) {
				seek(dragging, mx);
				return true;
			}
			if (dragging.kind == SP_VOL) {
				vol(dragging, mx);
				return true;
			}
		}
		return super.mouseDragged(new Click(mx, my, click.buttonInfo()), dx, dy);
	}

	@Override
	public boolean mouseReleased(Click click) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		int btn = button;
		double mx = mouseX;
		double my = mouseY;
		dragWin = 0;
		dragging = null;
		return super.mouseReleased(new Click(mx, my, click.buttonInfo()));
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double horizontal, double amount) {
		mx /= gs;
		my /= gs;
		InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);
		if (InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) && im != null) {
			im.guiScale.set(im.guiScale.get() + amount * 5);
			return true;
		}
		if (selected != null && mx >= panX && mx <= panX + PW && my >= panY && my <= panY + PH) {
			panelScrollTarget = MathHelper.clamp(panelScrollTarget - (float) amount * 24f, 0f, (float) panelMax);
			return true;
		}
		scrollTarget = MathHelper.clamp(scrollTarget - (float) amount * 28f, 0f, (float) maxScroll);
		sbAlpha = 1f;
		return true;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		int keyCode = input.key();
		int scanCode = input.scancode();
		int modifiers = input.modifiers();
		int key = keyCode;
		int scan = scanCode;
		int mods = modifiers;
		if (listening != null) {
			listening.key = key == GLFW.GLFW_KEY_ESCAPE ? -1 : key;
			listening = null;
			return true;
		}
		boolean typing = search.isFocused() || primField.isFocused() || secField.isFocused() || idField.isFocused();
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			if (typing) {
				search.setFocused(false);
				primField.setFocused(false);
				secField.setFocused(false);
				idField.setFocused(false);
				return true;
			}
			close();
			return true;
		}
		if (typing) {
			if (search.isFocused() && search.keyPressed(input)) return true;
			if (primField.isFocused() && primField.keyPressed(input)) return true;
			if (secField.isFocused() && secField.keyPressed(input)) return true;
			if (idField.isFocused() && idField.keyPressed(input)) return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public boolean charTyped(CharInput input) {
		char c = (char) input.codepoint();
		int modifiers = input.modifiers();
		if (search.isFocused()) return search.charTyped(input);
		if (primField.isFocused()) return primField.charTyped(input);
		if (secField.isFocused()) return secField.charTyped(input);
		if (idField.isFocused()) return idField.charTyped(input);
		return super.charTyped(input);
	}
}
