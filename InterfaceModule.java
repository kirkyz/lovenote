package com.lovenote.module.impl;

import com.google.gson.JsonObject;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;

import java.util.Arrays;

/** Theme + menu look. Edited from the Themes page. */
public class InterfaceModule extends Module {
	public final ModeSetting main = add(new ModeSetting("Main color", "Dark", "Light", "Dark"));
	public final ModeSetting accent = add(new ModeSetting("Accent", "Lovenote", accentNames()));
	public final BoolSetting gradient = add(new BoolSetting("Animation gradient", true));

	// ---- window / layout
	public final NumberSetting guiScale = add(new NumberSetting("Menu scale", 65, 40, 110, 5));
	public final NumberSetting windowWidth = add(new NumberSetting("Window width", 600, 460, 900, 10));
	public final NumberSetting windowHeight = add(new NumberSetting("Window height", 372, 280, 560, 4));
	public final NumberSetting corner = add(new NumberSetting("Corner radius", 10, 0, 18, 1));
	public final NumberSetting railWidth = add(new NumberSetting("Rail width", 118, 92, 168, 2));
	public final ModeSetting railSide = add(new ModeSetting("Rail side", "Left", "Left", "Right"));
	public final ModeSetting columns = add(new ModeSetting("Columns", "2", "1", "2", "3"));
	public final ModeSetting sort = add(new ModeSetting("Sort", "Name", "Name", "Category", "Enabled first"));
	public final BoolSetting compact = add(new BoolSetting("Compact cards", false));
	public final BoolSetting descriptions = add(new BoolSetting("Card descriptions", true));
	public final BoolSetting footer = add(new BoolSetting("Footer bar", true));
	public final BoolSetting tooltips = add(new BoolSetting("Tooltips", true));

	// ---- logo
	public final BoolSetting logo = add(new BoolSetting("Spinning logo", true));
	public final NumberSetting logoSpin = add(new NumberSetting("Logo spin (ms)", 2600, 800, 8000, 100));
	public final NumberSetting logoSize = add(new NumberSetting("Logo size", 26, 16, 40, 1));
	public final BoolSetting logoGlow = add(new BoolSetting("Logo glow", true));

	// ---- style
	public final ModeSetting style = add(new ModeSetting("GUI style", "Classic", "Classic", "Liquid", "Transparent"));
	public final ModeSetting card = add(new ModeSetting("Card style", "Solid", "Solid", "Outline", "Glass"));
	public final NumberSetting opacity = add(new NumberSetting("Opacity", 100, 40, 100, 1));
	public final NumberSetting glow = add(new NumberSetting("Accent glow", 5, 0, 10, 1));
	public final BoolSetting shadow = add(new BoolSetting("Drop shadow", true));
	public final BoolSetting headerLine = add(new BoolSetting("Header accent line", true));
	public final BoolSetting sheen = add(new BoolSetting("Inner sheen", true));

	// ---- animation
	public final ModeSetting anim = add(new ModeSetting("Animation", "Fade", "Fade", "Tilt", "Scale", "None"));
	public final NumberSetting animSpeed = add(new NumberSetting("Speed (ms)", 300, 100, 1000, 25));
	public final NumberSetting sidebar = add(new NumberSetting("Rail speed (ms)", 300, 100, 800, 25));
	public final BoolSetting stagger = add(new BoolSetting("Staggered cards", true));
	public final NumberSetting staggerSpeed = add(new NumberSetting("Stagger (ms)", 20, 0, 80, 2));
	public final NumberSetting hoverLift = add(new NumberSetting("Hover lift", 2, 0, 6, 1));
	public final BoolSetting ripples = add(new BoolSetting("Click ripples", true));

	// ---- background
	public final NumberSetting particles = add(new NumberSetting("Particles", 45, 0, 140, 5));
	public final NumberSetting particleSpeed = add(new NumberSetting("Particle speed", 100, 20, 300, 10));

	public String customPrimary = "A855F7", customSecondary = "9647FF";

	private static String[] accentNames() {
		String[] a = Arrays.copyOf(Colors.PRESET_NAMES, Colors.PRESET_NAMES.length + 1);
		a[a.length - 1] = "Custom";
		return a;
	}

	public InterfaceModule() {
		super("Interface", Category.HUD);
		hidden = true;
		enabled = true;
	}

	private static int parse(String hex, int fallback) {
		try {
			return Integer.parseInt(hex.replace("#", "").trim(), 16) & 0xFFFFFF;
		} catch (Exception e) {
			return fallback;
		}
	}

	public int customPrimaryInt() { return parse(customPrimary, 0xA855F7); }
	public int customSecondaryInt() { return parse(customSecondary, 0x9647FF); }

	public void apply() {
		Colors.dark = main.is("Dark");
		Colors.animated = gradient.get();
		int i = accent.index();
		if (i < Colors.PRESETS.length) Colors.setAccent(Colors.PRESETS[i][0], Colors.PRESETS[i][1]);
		else Colors.setAccent(customPrimaryInt(), customSecondaryInt());
	}

	@Override
	public void onTick() {
		apply();
	}

	@Override
	public void saveExtra(JsonObject o) {
		o.addProperty("customPrimary", customPrimary);
		o.addProperty("customSecondary", customSecondary);
	}

	@Override
	public void loadExtra(JsonObject o) {
		if (o.has("customPrimary")) customPrimary = o.get("customPrimary").getAsString();
		if (o.has("customSecondary")) customSecondary = o.get("customSecondary").getAsString();
	}
}
