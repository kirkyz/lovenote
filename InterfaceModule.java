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
	public final ModeSetting anim = add(new ModeSetting("Animation", "Fade", "Fade", "Tilt"));
	public final NumberSetting animSpeed = add(new NumberSetting("Speed (ms)", 300, 100, 1000, 25));
	public final ModeSetting style = add(new ModeSetting("GUI style", "Classic", "Liquid", "Classic", "Transparent"));
	public final NumberSetting opacity = add(new NumberSetting("Opacity", 100, 40, 100, 1));
	public final NumberSetting guiScale = add(new NumberSetting("Menu scale", 65, 40, 110, 5));
	public final NumberSetting sidebar = add(new NumberSetting("Sidebar speed (ms)", 300, 100, 800, 25));

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
