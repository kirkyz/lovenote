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
			panY = mainY + 
