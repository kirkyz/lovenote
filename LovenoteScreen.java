package com.lovenote.gui;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.module.impl.InterfaceModule;
import com.lovenote.module.impl.SpotifyHud;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.MultiSetting;
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

/**
 * Lovenote menu: draggable main window with an expanding sidebar, search, module cards and a
 * separate draggable settings window. Everything animates with time-based easing.
 */
public class LovenoteScreen extends Screen {
	private static final int W = 500, H = 320, PW = 180, PH = 300;
	private static final int ACC_COLS = 11;

	// remembered between openings
	private static int mainX = Integer.MIN_VALUE, mainY, panX = Integer.MIN_VALUE, panY, page;

	private static final int K_NONE = -1, K_CARD = 0, K_SWITCH = 1, K_BOOL = 2, K_SLIDER = 3, K_MODE = 4, K_MULTI = 5,
			K_PTOGGLE = 6, K_PCLOSE = 7, K_BIND = 8, K_ACCENT = 9, K_EDITOR = 10,
			SP_CONNECT = 20, SP_DISCONNECT = 21, SP_PREV = 22, SP_PLAY = 23, SP_NEXT = 24, SP_SEEK = 25, SP_VOL = 26;

	private static final class Hit {
		int x, y, w, h, kind, idx;
		Module module;
		Setting setting;
		int[] clip;
	}

	private final Category[] pages = Category.values();
	private final ItemStack[] icons = {
			new ItemStack(Items.NETHERITE_SWORD), new ItemStack(Items.FEATHER), new ItemStack(Items.ENDER_EYE),
			new ItemStack(Items.COMPASS), new ItemStack(Items.REDSTONE), new ItemStack(Items.JUKEBOX), new ItemStack(Items.PAINTING)};
	private final ItemStack editorIcon = new ItemStack(Items.ITEM_FRAME);

	private final List<Hit> hits = new ArrayList<>();
	private final List<int[]> pageRects = new ArrayList<>(); // x,y,w,h,pageIndex (-2 = editor)
	private final Map<String, Float> anim = new HashMap<>();

	private int wx, wy;
	private float gs = 1f;
	private int vw, vh;
	private float dt, ga = 1f, openMs, closeMs, scrollTarget, scrollShown, panelScrollTarget, panelScrollShown;
	private int maxScroll, panelMax;
	private boolean closing, finishClose, animating;
	private long lastNano;
	private Module selected, panelModule, listening;
	private int dragWin, grabX, grabY, localVol = -1;
	private Hit dragging;
	private int[] clip = {0, 0, 99999, 99999};
	private boolean settingFields;

	private float[] px, py, pvx, pvy;
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
		InterfaceModule im0 = LovenoteClient.modules.get(InterfaceModule.class);
		gs = im0 != null ? MathHelper.clamp(im0.guiScale.getI() / 100f, 0.4f, 1.1f) : 1f;
		vw = Math.max(160, Math.round(width / gs));
		vh = Math.max(120, Math.round(height / gs));

		if (mainX == Integer.MIN_VALUE) {
			mainX = (vw - W) / 2;
			mainY = (vh - H) / 2;
		}
		if (panX == Integer.MIN_VALUE) {
			panX = mainX + W + 8 + PW <= vw - 4 ? mainX + W + 8 : mainX + W - PW - 10;
			panY = mainY + 10;
		}
		mainX = MathHelper.clamp(mainX, Math.min(0, vw - W), Math.max(0, vw - W));
		mainY = MathHelper.clamp(mainY, Math.min(0, vh - H), Math.max(0, vh - H));
		panX = MathHelper.clamp(panX, 0, Math.max(0, vw - PW));
		panY = MathHelper.clamp(panY, 0, Math.max(0, vh - PH));
		openMs = 0;
		closing = false;
		finishClose = false;
		lastNano = System.nanoTime();

		Random r = new Random();
		px = new float[55]; py = new float[55]; pvx = new float[55]; pvy = new float[55];
		for (int i = 0; i < px.length; i++) {
			px[i] = r.nextFloat() * width;
			py[i] = r.nextFloat() * height;
			pvx[i] = (r.nextFloat() - 0.5f) * 0.25f;
			pvy[i] = -0.08f - r.nextFloat() * 0.35f;
		}

		search = mkField("Search...", 32, "");
		search.setChangedListener(s -> { scrollTarget = 0; scrollShown = 0; });
		idField = mkField("Client ID", 64, SpotifyManager.I.clientId);
		idField.setChangedListener(s -> SpotifyManager.I.setClientId(s.trim()));
		primField = mkField("Primary", 6, "");
		secField = mkField("Secondary", 6, "");
		primField.setChangedListener(s -> onHexTyped(s, true));
		secField.setChangedListener(s -> onHexTyped(s, false));
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
		int al = (int) ((c >>> 24) * ga);
		return (al << 24) | (c & 0xFFFFFF);
	}

	private void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int c) { ctx.fill(x1, y1, x2, y2, a(c)); }
	private void rr(DrawContext ctx, int x, int y, int w, int h, int c) { RenderUtil.rrect(ctx, x, y, w, h, a(c)); }
	private void grad(DrawContext ctx, int x1, int y1, int x2, int y2, int c1, int c2) { RenderUtil.gradientH(ctx, x1, y1, x2, y2, a(c1), a(c2)); }

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
		ms.push();
		ms.translate(x, y, 0);
		ms.scale(sc, sc, 1f);
		txt(ctx, s, 0, 0, c);
		ms.pop();
	}

	private void circle(DrawContext ctx, int cx, int cy, int r, int c) {
		for (int dy = -r; dy <= r; dy++) {
			int hw = (int) Math.round(Math.sqrt(r * r - dy * dy));
			ctx.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, a(c));
		}
	}

	/** Time-based exponential smoothing toward target; durMs ~ how long the move takes. */
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
		// 1.20.1's enableScissor uses the coords as-is (no matrix), so undo our gs scale here
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

	// ------------------------------------------------------------------ settings widgets

	private void drawSwitch(DrawContext ctx, int x, int y, float t) {
		rr(ctx, x, y, 22, 10, Colors.lerp(Colors.track(), Colors.C2, t));
		if (t > 0.02f) grad(ctx, x + 2, y + 1, x + 20, y + 9, Colors.alpha(Colors.C1, (int) (255 * t)), Colors.alpha(Colors.C3, (int) (255 * t)));
		rr(ctx, x + 1 + (int) (t * 12), y + 1, 8, 8, 0xFFFFFFFF);
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
			rr(ctx, rx, ry, cw, 15, Colors.lerp(Colors.chip(), Colors.C2, t));
			if (t > 0.02f) grad(ctx, rx + 2, ry + 1, rx + cw - 2, ry + 14, Colors.alpha(Colors.C1, (int) (255 * t)), Colors.alpha(Colors.C3, (int) (255 * t)));
			txt(ctx, names[i], rx + 6, ry + 4, Colors.lerp(Colors.dim(), 0xFF101015, t));
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
			rr(ctx, MathHelper.clamp(fx - 3, x, x + w - 6), sy - 3, 6, 9, 0xFFFFFFFF);
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
		vw = Math.max(160, Math.round(width / gs));
		vh = Math.max(120, Math.round(height / gs));

		// keep the windows reachable when the scale changes mid-session
		mainX = MathHelper.clamp(mainX, Math.min(0, vw - W), Math.max(0, vw - W));
		mainY = MathHelper.clamp(mainY, Math.min(0, vh - H), Math.max(0, vh - H));
		panX = MathHelper.clamp(panX, 0, Math.max(0, vw - PW));
		panY = MathHelper.clamp(panY, 0, Math.max(0, vh - PH));
		mx /= gs; my /= gs; // every hover test below is in virtual coords

		float animMs = im != null ? im.animSpeed.getF() : 300f;

		hits.clear();
		pageRects.clear();
		wx = mainX;
		wy = mainY;

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

		ctx.fill(0, 0, width, height, Colors.alpha(0x000000, (int) (120 * eo)));
		for (int i = 0; i < px.length; i++) {
			px[i] += pvx[i];
			py[i] += pvy[i];
			if (py[i] < 0) { py[i] = height; px[i] = (float) (Math.random() * width); }
			if (px[i] < 0) px[i] = width;
			if (px[i] > width) px[i] = 0;
			int s = 1 + (i % 2);
			ctx.fill((int) px[i], (int) py[i], (int) px[i] + s, (int) py[i] + s, Colors.alpha(Colors.skeet(i / (float) px.length), (int) (130 * eo)));
		}

		var ms = ctx.getMatrices();
		ms.push();
		ms.scale(gs, gs, 1f);
		if (im != null && im.anim.is("Tilt")) {
			float sc = 0.9f + 0.1f * eo;
			float cx = wx + W / 2f, cy = wy + H / 2f;
			ms.translate(cx, cy + (1f - eo) * 14f, 0);
			ms.scale(sc, sc, 1f);
			ms.translate(-cx, -cy, 0);
		}

		boolean interactive = !animating;
		if (selected != null) panelModule = selected;
		float panelA = ease("panel", selected != null ? 1f : 0f, 220);

		drawWindow(ctx, mx, my, delta, im, interactive);
		if (panelModule != null && panelA > 0.01f) drawPanel(ctx, mx, my, panelA);
		drawSidebar(ctx, mx, my, im);

		if (listening != null) {
			txtS(ctx, "press a key to bind " + listening.name + "  (esc = clear)", wx + W / 2 - 90, wy + H + 8, 0xFFFFD27A);
		}
		ms.pop();
	}

	// ---------- main window

	private void drawWindow(DrawContext ctx, int mx, int my, float delta, InterfaceModule im, boolean interactive) {
		int styleA = im == null || im.style.is("Classic") ? 0xF6 : im.style.is("Liquid") ? 0xC4 : 0x84;
		int alpha = (int) (styleA * (im == null ? 100 : im.opacity.get()) / 100.0);
		for (int i = 7; i >= 1; i--) rr(ctx, wx - i, wy - i, W + i * 2, H + i * 2, Colors.alpha(0x000000, 10));
		rr(ctx, wx - 1, wy - 1, W + 2, H + 2, Colors.alpha(Colors.C2, 80));
		rr(ctx, wx, wy, W, H, Colors.alpha(Colors.bg(), alpha));
		if (im != null && im.style.is("Liquid")) grad(ctx, wx + 2, wy + 2, wx + W - 2, wy + 60, Colors.alpha(Colors.C1, 34), Colors.alpha(Colors.C3, 34));

		int cx = wx + 46, cw = W - 58;
		String q = search.getText().trim().toLowerCase();
		boolean moduleGrid = !q.isEmpty() || (pages[page] != Category.SPOTIFY && pages[page] != Category.THEMES);

		// search box
		rr(ctx, cx, wy + 12, 178, 18, Colors.chip());
		circle(ctx, cx + 11, wy + 20, 4, Colors.faint());
		circle(ctx, cx + 11, wy + 20, 2, Colors.chip());
		fill(ctx, cx + 14, wy + 23, cx + 17, wy + 24, Colors.faint());
		placeField(search, cx + 22, wy + 17, 150);
		if (interactive) renderField(ctx, search, mx, my, delta);
		if (search.getText().isEmpty() && !search.isFocused()) txt(ctx, "Search...", cx + 22, wy + 17, Colors.faint());

		String title = !q.isEmpty() ? "All" : pages[page].label;
		txtScaled(ctx, title, cx, wy + 38, Colors.text(), 1.35f);

		int top = wy + 62, bottom = wy + H - 12;
		if (q.isEmpty() && pages[page] == Category.HUD) {
			int cwid = textRenderer.getWidth("Edit layout") + 14;
			int ex = cx + cw - cwid;
			boolean hov = interactive && mx >= ex && mx <= ex + cwid && my >= wy + 38 && my <= wy + 54;
			float hv = ease("editbtn", hov ? 1f : 0f, 120);
			rr(ctx, ex, wy + 38, cwid, 16, Colors.lerp(Colors.chip(), Colors.C2, hv * 0.6f));
			txt(ctx, "Edit layout", ex + 7, wy + 42, Colors.lerp(Colors.dim(), Colors.text(), hv));
			addHit(ex, wy + 38, cwid, 16, K_EDITOR, null, null, 0);
		}

		if (moduleGrid) {
			renderCards(ctx, mx, my, cx, top, cw, bottom, q, interactive);
		} else if (pages[page] == Category.SPOTIFY) {
			renderSpotify(ctx, mx, my, delta, cx, top - 6, cw, interactive);
		} else {
			renderThemes(ctx, mx, my, delta, cx, top - 6, cw, im, interactive);
		}
		if (!interactive) return;
		if (pages[page] != Category.SPOTIFY || !q.isEmpty()) idField.setFocused(false);
		if (pages[page] != Category.THEMES || !q.isEmpty()) { primField.setFocused(false); secField.setFocused(false); }
	}

	/** Fields live in GUI space (1.20.1 scissors ignore the matrix), so store scaled coords. */
	private void placeField(TextFieldWidget f, int x, int y, int w) {
		f.setX(Math.round(x * gs));
		f.setY(Math.round(y * gs));
		f.setWidth(Math.round(w * gs));
		f.setEditableColor(Colors.text());
	}

	private void renderField(DrawContext ctx, TextFieldWidget f, int mx, int my, float delta) {
		if (ga <= 0.9f) return;
		var ms = ctx.getMatrices();
		ms.push();
		ms.scale(1f / gs, 1f / gs, 1f); // drop out of the menu scale: fields are in GUI space
		f.render(ctx, Math.round(mx * gs), Math.round(my * gs), delta);
		ms.pop();
	}

	// ---------- module cards

	private List<Module> visibleModules(String q) {
		List<Module> out = new ArrayList<>();
		for (Module m : LovenoteClient.modules.all()) {
			if (m.hidden) continue;
			if (!q.isEmpty()) {
				if (m.name.toLowerCase().contains(q) || m.description.toLowerCase().contains(q)) out.add(m);
			} else if (m.category == pages[page]) {
				out.add(m);
			}
		}
		return out;
	}

	private void renderCards(DrawContext ctx, int mx, int my, int cx, int top, int cw, int bottom, String q, boolean interactive) {
		List<Module> list = visibleModules(q);
		int cardW = (cw - 8) / 2, cardH = 38, gap = 6;
		int rows = (list.size() + 1) / 2;
		maxScroll = Math.max(0, rows * (cardH + gap) - (bottom - top));
		scrollTarget = MathHelper.clamp(scrollTarget, 0, maxScroll);
		scrollShown += (scrollTarget - scrollShown) * (1f - (float) Math.exp(-dt / 40f));
		if (Math.abs(scrollTarget - scrollShown) < 0.2f) scrollShown = scrollTarget;

		setClip(ctx, cx - 2, top - 2, cx + cw + 2, bottom);
		for (int i = 0; i < list.size(); i++) {
			Module m = list.get(i);
			int x = cx + (i % 2) * (cardW + 8);
			int y = top + (i / 2) * (cardH + gap) - Math.round(scrollShown);
			if (y + cardH < top - 4 || y > bottom + 4) continue;

			boolean hov = interactive && mx >= x && mx < x + cardW && my >= y && my < y + cardH && my >= top && my <= bottom;
			float hv = ease(m.name + ":h", hov ? 1f : 0f, 130);
			float on = ease(m.name + ":s", m.enabled ? 1f : 0f, 170);
			float sel = ease(m.name + ":sel", selected == m ? 1f : 0f, 170);

			if (sel > 0.02f) rr(ctx, x - 1, y - 1, cardW + 2, cardH + 2, Colors.alpha(Colors.C2, (int) (170 * sel)));
			rr(ctx, x, y, cardW, cardH, Colors.lerp(Colors.card(), Colors.cardHover(), hv));
			txt(ctx, m.name, x + 10, y + 8, Colors.lerp(Colors.dim(), Colors.text(), Math.max(on, hv * 0.7f)));
			String d = textRenderer.trimToWidth(m.description, cardW - 62);
			txt(ctx, d, x + 10, y + 21, Colors.faint());
			drawSwitch(ctx, x + cardW - 34, y + 14, on);

			addHit(x, y, cardW, cardH, K_CARD, m, null, 0);
			addHit(x + cardW - 38, y + 8, 34, 22, K_SWITCH, m, null, 0);
		}
		endClip(ctx);
		if (list.isEmpty()) txt(ctx, "No modules found", cx + 4, top + 8, Colors.faint());
	}

	// ---------- sidebar

	private void drawSidebar(DrawContext ctx, int mx, int my, InterfaceModule im) {
		float speed = im != null ? im.sidebar.getF() : 300f;
		int curW = 38 + (int) (80 * anim.getOrDefault("sb", 0f));
		boolean hover = dragWin == 0 && !animating && mx >= wx && mx < wx + curW && my >= wy && my < wy + H;
		float sb = ease("sb", hover ? 1f : 0f, speed);
		int sbw = 38 + (int) (80 * sb);

		if (sb > 0.02f) for (int i = 5; i >= 1; i--) rr(ctx, wx + sbw - 2, wy + 4, i * 2 + 2, H - 8, Colors.alpha(0x000000, (int) (9 * sb)));
		rr(ctx, wx, wy, sbw, H, Colors.dark ? 0xF40A0A0E : 0xF4E4E4EC);

		circle(ctx, wx + 19, wy + 22, 9, Colors.C2);
		txtS(ctx, "\u2665", wx + 16, wy + 18, 0xFFFFFFFF);
		if (sb > 0.05f) txtS(ctx, "lovenote", wx + 34, wy + 18, Colors.alpha(Colors.wave(0f), (int) (255 * sb)));

		String q = search.getText().trim();
		int y = wy + 48;
		for (int i = 0; i < pages.length; i++) {
			if (i == 5) y += 8;
			boolean sel = i == page && q.isEmpty();
			boolean hov = hover && mx >= wx + 4 && mx < wx + sbw - 4 && my >= y && my < y + 24;
			float hv = ease("pg" + i, hov ? 1f : 0f, 120);
			float sl = ease("ps" + i, sel ? 1f : 0f, 170);
			if (sl > 0.02f || hv > 0.02f) rr(ctx, wx + 4, y, sbw - 8, 24, Colors.alpha(Colors.C2, (int) (62 * sl + 26 * hv)));
			if (sl > 0.02f) rr(ctx, wx + 4, y + 5, 2, 14, Colors.alpha(Colors.wave(i * 0.1f), (int) (255 * sl)));
			if (i < icons.length) ctx.drawItem(icons[i], wx + 11, y + 4);
			if (sb > 0.05f) txt(ctx, pages[i].label, wx + 34, y + 8, Colors.alpha(Colors.lerp(Colors.dim(), Colors.text(), sl), (int) (255 * sb)));
			pageRects.add(new int[]{wx + 4, y, sbw - 8, 24, i});
			y += 26;
		}

		int ey = wy + H - 32;
		boolean ehov = hover && mx >= wx + 4 && mx < wx + sbw - 4 && my >= ey && my < ey + 24;
		float eh = ease("pgE", ehov ? 1f : 0f, 120);
		if (eh > 0.02f) rr(ctx, wx + 4, ey, sbw - 8, 24, Colors.alpha(Colors.C2, (int) (30 * eh)));
		ctx.drawItem(editorIcon, wx + 11, ey + 4);
		if (sb > 0.05f) txt(ctx, "HUD editor", wx + 34, ey + 8, Colors.alpha(Colors.dim(), (int) (255 * sb)));
		pageRects.add(new int[]{wx + 4, ey, sbw - 8, 24, -2});
	}

	// ---------- settings window (separate, draggable)

	private void drawPanel(DrawContext ctx, int mx, int my, float panelA) {
		Module m = panelModule;
		float saved = ga;
		ga = saved * panelA;
		int px = panX + (int) ((1f - panelA) * 22), py = panY;

		for (int i = 6; i >= 1; i--) rr(ctx, px - i, py - i, PW + i * 2, PH + i * 2, Colors.alpha(0x000000, 10));
		rr(ctx, px - 1, py - 1, PW + 2, PH + 2, Colors.alpha(Colors.C2, 80));
		rr(ctx, px, py, PW, PH, Colors.alpha(Colors.card(), 0xF6));
		addHit(px, py, PW, PH, K_NONE, null, null, 0);

		txtS(ctx, m.name, px + 12, py + 9, Colors.text());
		txt(ctx, textRenderer.trimToWidth(m.description, PW - 80), px + 12, py + 21, Colors.faint());

		float on = ease(m.name + ":s", m.enabled ? 1f : 0f, 170);
		drawSwitch(ctx, px + PW - 70, py + 10, on);
		addHit(px + PW - 73, py + 6, 28, 18, K_PTOGGLE, m, null, 0);
		circle(ctx, px + PW - 34, py + 15, 4, 0xFF3DD68C);
		circle(ctx, px + PW - 22, py + 15, 4, 0xFFF5B83D);
		circle(ctx, px + PW - 10, py + 15, 4, 0xFFEF4444);
		addHit(px + PW - 16, py + 9, 12, 12, K_PCLOSE, m, null, 0);

		fill(ctx, px + 8, py + 34, px + PW - 8, py + 35, Colors.track());

		int top = py + 38, bottom = py + PH - 32;
		int total = 4;
		for (Setting s : m.settings) total += rowH(s, PW - 24);
		panelMax = Math.max(0, total - (bottom - top));
		panelScrollTarget = MathHelper.clamp(panelScrollTarget, 0, panelMax);
		panelScrollShown += (panelScrollTarget - panelScrollShown) * (1f - (float) Math.exp(-dt / 40f));
		if (Math.abs(panelScrollTarget - panelScrollShown) < 0.2f) panelScrollShown = panelScrollTarget;

		setClip(ctx, px, top, px + PW, bottom);
		int y = top + 4 - Math.round(panelScrollShown);
		for (Setting s : m.settings) y += drawSetting(ctx, m, s, px + 12, y, PW - 24, true);
		endClip(ctx);

		// keybind row
		int by = py + PH - 26;
		fill(ctx, px + 8, by - 5, px + PW - 8, by - 4, Colors.track());
		txt(ctx, "Keybind", px + 12, by + 3, Colors.dim());
		String kn = listening == m ? "..." : keyName(m.key);
		int kw = textRenderer.getWidth(kn) + 14;
		boolean khov = mx >= px + PW - 12 - kw && mx <= px + PW - 12 && my >= by && my <= by + 15;
		float kh = ease("bind", khov || listening == m ? 1f : 0f, 120);
		rr(ctx, px + PW - 12 - kw, by, kw, 15, Colors.lerp(Colors.chip(), Colors.C2, kh * 0.7f));
		txt(ctx, kn, px + PW - 12 - kw + 7, by + 4, Colors.lerp(Colors.text(), 0xFF101015, kh * 0.7f));
		addHit(px + PW - 12 - kw, by, kw, 15, K_BIND, m, null, 0);
		ga = saved;
	}

	// ---------- themes page

	private int themeCard(DrawContext ctx, int x, int y, int w, String title, InterfaceModule im, boolean interactive, Setting... settings) {
		int h = 26;
		for (Setting s : settings) h += rowH(s, w - 22);
		rr(ctx, x, y, w, h, Colors.card());
		rr(ctx, x + 10, y + 9, 7, 7, Colors.wave(0f));
		txt(ctx, title, x + 22, y + 8, Colors.text());
		int yy = y + 24;
		for (Setting s : settings) yy += drawSetting(ctx, im, s, x + 11, yy, w - 22, interactive);
		return h;
	}

	private void renderThemes(DrawContext ctx, int mx, int my, float delta, int cx, int cy, int cw, InterfaceModule im, boolean interactive) {
		if (im == null) return;
		int colW = (cw - 8) / 2, x1 = cx, x2 = cx + colW + 8;

		int accRows = (Colors.PRESETS.length + ACC_COLS - 1) / ACC_COLS;
		int accentH = 100 + accRows * 18;

		int leftH = 4
				+ (26 + rowH(im.guiScale, colW - 22)) + 6
				+ (26 + rowH(im.main, colW - 22)) + 6
				+ accentH + 6
				+ (26 + rowH(im.sidebar, colW - 22));
		int rightH = 4
				+ (26 + rowH(im.anim, colW - 22) + rowH(im.animSpeed, colW - 22)) + 6
				+ (26 + rowH(im.style, colW - 22) + rowH(im.opacity, colW - 22));

		int top = cy + 4, bottom = wy + H - 10;
		maxScroll = Math.max(0, Math.max(leftH, rightH) - (bottom - top));
		scrollTarget = MathHelper.clamp(scrollTarget, 0, maxScroll);
		scrollShown += (scrollTarget - scrollShown) * (1f - (float) Math.exp(-dt / 40f));
		if (Math.abs(scrollTarget - scrollShown) < 0.2f) scrollShown = scrollTarget;

		setClip(ctx, cx - 2, top - 2, cx + cw + 2, bottom);

		int y = top - Math.round(scrollShown);
		y += themeCard(ctx, x1, y, colW, "Menu", im, interactive, im.guiScale) + 6;
		y += themeCard(ctx, x1, y, colW, "Main color", im, interactive, im.main) + 6;

		// accent card
		int h = accentH;
		rr(ctx, x1, y, colW, h, Colors.card());
		rr(ctx, x1 + 10, y + 9, 7, 7, Colors.wave(0f));
		txt(ctx, "Accent color", x1 + 22, y + 8, Colors.text());
		int selIdx = im.accent.index();
		for (int i = 0; i < Colors.PRESETS.length; i++) {
			int ccx = x1 + 18 + (i % ACC_COLS) * 18;
			int ccy = y + 34 + (i / ACC_COLS) * 18;
			int col = 0xFF000000 | Colors.PRESETS[i][0];
			float t = ease("acc" + i, selIdx == i ? 1f : 0f, 140);
			if (t > 0.02f) { circle(ctx, ccx, ccy, 8, Colors.alpha(Colors.text(), (int) (255 * t))); circle(ctx, ccx, ccy, 7, Colors.card()); }
			circle(ctx, ccx, ccy, 5 + (int) Math.round(t), col);
			addHit(ccx - 9, ccy - 9, 18, 18, K_ACCENT, null, null, i);
		}

		int fy = y + 34 + accRows * 18 + 6;
		txt(ctx, "Primary", x1 + 12, fy, Colors.dim());
		txt(ctx, "Secondary", x1 + colW / 2 + 4, fy, Colors.dim());
		if (!primField.isFocused() && !secField.isFocused()) {
			settingFields = true;
			String p = String.format("%06X", Colors.C1 & 0xFFFFFF), s = String.format("%06X", Colors.C3 & 0xFFFFFF);
			if (!primField.getText().equalsIgnoreCase(p)) primField.setText(p);
			if (!secField.getText().equalsIgnoreCase(s)) secField.setText(s);
			settingFields = false;
		}
		int fw = colW / 2 - 22;
		circle(ctx, x1 + 18, fy + 19, 5, Colors.C1);
		rr(ctx, x1 + 28, fy + 11, fw, 16, Colors.chip());
		placeField(primField, x1 + 34, fy + 15, fw - 10);
		circle(ctx, x1 + colW / 2 + 10, fy + 19, 5, Colors.C3);
		rr(ctx, x1 + colW / 2 + 20, fy + 11, fw, 16, Colors.chip());
		placeField(secField, x1 + colW / 2 + 26, fy + 15, fw - 10);
		if (interactive) { renderField(ctx, primField, mx, my, delta); renderField(ctx, secField, mx, my, delta); }
		drawSetting(ctx, im, im.gradient, x1 + 11, fy + 32, colW - 22, interactive);
		y += h + 6;

		themeCard(ctx, x1, y, colW, "Sidebar speed", im, interactive, im.sidebar);

		int y2 = top - Math.round(scrollShown);
		y2 += themeCard(ctx, x2, y2, colW, "Animation", im, interactive, im.anim, im.animSpeed) + 6;
		themeCard(ctx, x2, y2, colW, "GUI style", im, interactive, im.style, im.opacity);

		endClip(ctx);
	}

	// ---------- spotify page

	private void button(DrawContext ctx, int x, int y, int w, int h, String label, int mx, int my, int kind, boolean interactive) {
		boolean hov = interactive && mx >= x && mx < x + w && my >= y && my < y + h;
		float hv = ease("btn" + kind, hov ? 1f : 0f, 120);
		rr(ctx, x, y, w, h, Colors.lerp(Colors.chip(), Colors.C2, hv * 0.55f));
		txt(ctx, label, x + (w - textRenderer.getWidth(label)) / 2, y + (h - 8) / 2 + 1, Colors.lerp(Colors.dim(), Colors.text(), hv));
		addHit(x, y, w, h, kind, null, null, 0);
	}

	private void bar(DrawContext ctx, int x, int y, int w, float f) {
		fill(ctx, x, y, x + w, y + 4, Colors.track());
		int fx = x + (int) (w * f);
		grad(ctx, x, y, Math.max(x + 1, fx), y + 4, Colors.C1, Colors.skeet(f));
		rr(ctx, MathHelper.clamp(fx - 3, x, x + w - 6), y - 2, 6, 8, 0xFFFFFFFF);
	}

	private void renderSpotify(DrawContext ctx, int mx, int my, float delta, int cx, int cy, int cw, boolean interactive) {
		SpotifyManager s = SpotifyManager.I;
		rr(ctx, cx, cy + 4, cw, 86, Colors.card());
		txt(ctx, "Spotify connection", cx + 10, cy + 10, Colors.text());
		rr(ctx, cx + 9, cy + 24, cw - 18, 16, Colors.chip());
		placeField(idField, cx + 15, cy + 28, cw - 30);
		if (idField.getText().isEmpty() && !idField.isFocused()) txt(ctx, "paste your Client ID", cx + 15, cy + 28, Colors.faint());
		if (interactive) renderField(ctx, idField, mx, my, delta);
		button(ctx, cx + 9, cy + 46, 66, 15, "connect", mx, my, SP_CONNECT, interactive);
		button(ctx, cx + 80, cy + 46, 76, 15, "disconnect", mx, my, SP_DISCONNECT, interactive);
		boolean ok = s.status.startsWith("Connected");
		txt(ctx, textRenderer.trimToWidth(s.status, cw - 18), cx + 9, cy + 67, ok ? 0xFF8EE0A8 : 0xFFE8B86A);
		if (!s.connected) txt(ctx, "redirect uri: http://127.0.0.1:8888/callback", cx + 9, cy + 78, Colors.faint());

		int gy = cy + 98;
		rr(ctx, cx, gy, cw, 118, Colors.card());
		int ax = cx + 10, ay = gy + 12;
		if (s.connected && s.artTexture != null) ctx.drawTexture(s.artTexture, ax, ay, 64, 64, 0, 0, 64, 64, 64, 64);
		else { rr(ctx, ax, ay, 64, 64, Colors.chip()); txtS(ctx, "\u266A", ax + 29, ay + 28, Colors.wave(0f)); }

		int tx = cx + 84, tw = cw - 94;
		if (s.connected && s.hasTrack) {
			marquee(ctx, s.title, tx, gy + 12, tw, Colors.text());
			marquee(ctx, s.artist, tx, gy + 24, tw, Colors.dim());
			marquee(ctx, s.album, tx, gy + 36, tw, Colors.faint());
		} else txt(ctx, s.connected ? "nothing playing" : "not connected", tx, gy + 12, Colors.dim());

		long prog = s.progress();
		float f = s.durationMs <= 0 ? 0f : Math.min(1f, prog / (float) s.durationMs);
		int by = gy + 58;
		bar(ctx, tx, by, tw, f);
		addHit(tx, by - 5, tw, 14, SP_SEEK, null, null, 0);
		txt(ctx, SpotifyHud.fmt(prog), tx, by + 9, Colors.faint());
		String dur = SpotifyHud.fmt(s.durationMs);
		txt(ctx, dur, tx + tw - textRenderer.getWidth(dur), by + 9, Colors.faint());

		int bty = gy + 82;
		button(ctx, tx, bty, 32, 16, "<<", mx, my, SP_PREV, interactive);
		button(ctx, tx + 36, bty, 32, 16, s.playing ? "||" : ">", mx, my, SP_PLAY, interactive);
		button(ctx, tx + 72, bty, 32, 16, ">>", mx, my, SP_NEXT, interactive);
		int vol = localVol >= 0 ? localVol : Math.max(0, s.volume);
		txt(ctx, "vol " + vol, tx + 112, bty + 4, Colors.faint());
		int vx = tx + 150, vw = tw - 150;
		bar(ctx, vx, bty + 6, vw, vol / 100f);
		addHit(vx, bty + 2, vw, 12, SP_VOL, null, null, 0);
		txt(ctx, "skip / pause / seek / volume need spotify premium", cx + 10, gy + 104, 0xFF4E4E5E);
	}

	private void marquee(DrawContext ctx, String text, int x, int y, int maxW, int color) {
		int w = textRenderer.getWidth(text);
		if (w <= maxW || animating) { txt(ctx, textRenderer.trimToWidth(text, maxW), x, y, color); return; }
		int gap = 24, off = (int) ((System.currentTimeMillis() / 30) % (w + gap));
		setClip(ctx, x, y - 1, x + maxW, y + 10);
		txt(ctx, text, x - off, y, color);
		txt(ctx, text, x - off + w + gap, y, color);
		endClip(ctx);
	}

	// ------------------------------------------------------------------ input

	private boolean inClip(Hit h, double mx, double my) {
		return mx >= h.clip[0] && mx <= h.clip[2] && my >= h.clip[1] && my <= h.clip[3];
	}

	private TextFieldWidget[] fields() {
		String q = search.getText().trim();
		List<TextFieldWidget> l = new ArrayList<>();
		l.add(search);
		if (q.isEmpty() && pages[page] == Category.SPOTIFY) l.add(idField);
		if (q.isEmpty() && pages[page] == Category.THEMES) { l.add(primField); l.add(secField); }
		return l.toArray(new TextFieldWidget[0]);
	}

	private boolean anyFieldFocused() {
		for (TextFieldWidget f : fields()) if (f.isFocused()) return true;
		return false;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		if (animating || closing) return true;
		double gmx = mx, gmy = my; // GUI space, for the text fields
		mx /= gs; my /= gs;

		for (int[] r : pageRects) {
			if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
				if (r[4] == -2) { client.setScreen(new HudEditorScreen()); return true; }
				page = r[4];
				search.setText("");
				scrollTarget = 0;
				scrollShown = 0;
				listening = null;
				for (TextFieldWidget f : new TextFieldWidget[]{search, idField, primField, secField}) f.setFocused(false);
				return true;
			}
		}

		TextFieldWidget[] fs = fields();
		boolean hitField = false;
		for (TextFieldWidget f : fs) {
			boolean in = f.mouseClicked(gmx, gmy, btn);
			f.setFocused(in);
			if (in) hitField = true;
		}
		if (hitField) return true;

		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit h = hits.get(i);
			if (mx < h.x || mx >= h.x + h.w || my < h.y || my >= h.y + h.h || !inClip(h, mx, my)) continue;
			switch (h.kind) {
				case K_NONE -> {}
				case K_CARD -> { if (btn == 0) { selected = h.module; panelScrollTarget = 0; panelScrollShown = 0; } else if (btn == 1) listening = h.module; }
				case K_SWITCH, K_PTOGGLE -> { if (btn == 0) h.module.toggle(); }
				case K_PCLOSE -> selected = null;
				case K_BOOL -> { if (btn == 0) ((BoolSetting) h.setting).toggle(); }
				case K_SLIDER -> { if (btn == 0) { dragging = h; setSlider(h, mx); } }
				case K_MODE -> ((ModeSetting) h.setting).setIndex(h.idx);
				case K_MULTI -> ((MultiSetting) h.setting).toggle(h.idx);
				case K_BIND -> { if (btn == 1) h.module.key = -1; else listening = h.module; }
				case K_ACCENT -> {
					InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);
					if (im != null) im.accent.setIndex(h.idx);
				}
				case K_EDITOR -> client.setScreen(new HudEditorScreen());
				case SP_CONNECT -> SpotifyManager.I.connect();
				case SP_DISCONNECT -> SpotifyManager.I.disconnect();
				case SP_PREV -> SpotifyManager.I.previous();
				case SP_PLAY -> SpotifyManager.I.togglePlay();
				case SP_NEXT -> SpotifyManager.I.next();
				case SP_SEEK -> {
					SpotifyManager sp = SpotifyManager.I;
					if (sp.hasTrack) sp.seek((long) (MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0) * sp.durationMs));
				}
				case SP_VOL -> { dragging = h; localVol = (int) (MathHelper.clamp((mx - h.x) / (double) h.w, 0.0, 1.0) * 100); }
				default -> {}
			}
			return true;
		}

		// window drag zones (checked after widgets so buttons win)
		float panelA = anim.getOrDefault("panel", 0f);
		if (panelModule != null && panelA > 0.5f && mx >= panX && mx < panX + PW && my >= panY && my < panY + 34) {
			dragWin = 2; grabX = (int) mx - panX; grabY = (int) my - panY;
			return true;
		}
		boolean topStrip = mx >= wx + 38 && mx < wx + W && my >= wy && my < wy + 10;
		boolean titleStrip = mx >= wx + 46 && mx < wx + W - 12 && my >= wy + 34 && my < wy + 58;
		boolean logoZone = mx >= wx && mx < wx + 38 && my >= wy && my < wy + 40;
		if (topStrip || titleStrip || logoZone) {
			dragWin = 1; grabX = (int) mx - wx; grabY = (int) my - wy;
			return true;
		}
		return super.mouseClicked(mx, my, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		mx /= gs; my /= gs; dx /= gs; dy /= gs;
		if (dragWin == 1) {
			mainX = MathHelper.clamp((int) mx - grabX, Math.min(0, vw - W), Math.max(0, vw - W));
			mainY = MathHelper.clamp((int) my - grabY, Math.min(0, vh - H), Math.max(0, vh - H));
			return true;
		}
		if (dragWin == 2) {
			panX = MathHelper.clamp((int) mx - grabX, 0, Math.max(0, vw - PW));
			panY = MathHelper.clamp((int) my - grabY, 0, Math.max(0, vh - PH));
			return true;
		}
		if (dragging != null) {
			if (dragging.kind == K_SLIDER) setSlider(dragging, mx);
			else if (dragging.kind == SP_VOL) localVol = (int) (MathHelper.clamp((mx - dragging.x) / (double) dragging.w, 0.0, 1.0) * 100);
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int btn) {
		if (dragging != null && dragging.kind == SP_VOL && localVol >= 0) SpotifyManager.I.setVolume(localVol);
		dragging = null;
		dragWin = 0;
		localVol = -1;
		return super.mouseReleased(mx / gs, my / gs, btn);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double amount) {
		if (hasControlDown()) {
			InterfaceModule im = LovenoteClient.modules.get(InterfaceModule.class);
			if (im != null) im.guiScale.set(im.guiScale.get() + amount * 5);
			return true;
		}
		mx /= gs; my /= gs;
		if (panelModule != null && selected != null && mx >= panX && mx < panX + PW && my >= panY && my < panY + PH) {
			panelScrollTarget = MathHelper.clamp(panelScrollTarget - (float) amount * 22f, 0, panelMax);
		} else {
			scrollTarget = MathHelper.clamp(scrollTarget - (float) amount * 30f, 0, maxScroll);
		}
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (listening != null) {
			listening.key = keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode;
			listening = null;
			return true;
		}
		if (anyFieldFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) { for (TextFieldWidget f : fields()) f.setFocused(false); return true; }
			for (TextFieldWidget f : fields()) if (f.isFocused()) f.keyPressed(keyCode, scanCode, modifiers);
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
		if (anyFieldFocused()) {
			for (TextFieldWidget f : fields()) if (f.isFocused()) f.charTyped(chr, modifiers);
			return true;
		}
		return super.charTyped(chr, modifiers);
	}
}
