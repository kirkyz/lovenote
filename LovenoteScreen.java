package com.lovenote.gui;

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
		String n = InputUtil.fromKeyCode(key, -1).getLocalizedText().getString().toUpperCase();
		return n.length() > 7 ? n.substring(0, 7) : n;
	}

	private void scrollbar(DrawContext ctx, int x, int top, int bottom, float shown, int max) {
		if (max <= 0) return;
		int trackH = bottom - top;
		int barH = Math.max(18, (int) (trackH * (trackH / (float) (trackH + max))));
		int barY = top + (int) ((trackH - barH) * (shown / max));
		rr(ctx, x, top, 3, trackH, 2, Colors.alpha(Colors.track(), (int) (120 * sbAlpha)));
		rr(ctx, x, barY, 3, barH, 2, Colors.
