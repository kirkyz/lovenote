package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;

/** Holds the colour theme. Pick "Custom" to unlock the HSV picker below. */
public class InterfaceModule extends Module {
	public final ModeSetting theme = add(new ModeSetting("Theme", "Lovenote", Colors.themeNames()));

	public final NumberSetting hue = add(new NumberSetting("Hue", 330, 0, 360, 1));
	public final NumberSetting saturation = add(new NumberSetting("Saturation", 100, 0, 100, 1));
	public final NumberSetting brightness = add(new NumberSetting("Brightness", 100, 0, 100, 1));
	public final NumberSetting spread = add(new NumberSetting("Gradient spread", 18, 0, 60, 1));
	public final BoolSetting chroma = add(new BoolSetting("Chroma cycle", false));
	public final NumberSetting chromaSpeed = add(new NumberSetting("Chroma speed", 12, 1, 60, 1));

	public InterfaceModule() {
		super("Interface", Category.HUD);
	}

	public void apply() {
		Colors.hue = hue.getI() / 360f;
		Colors.sat = saturation.getI() / 100f;
		Colors.bri = brightness.getI() / 100f;
		Colors.spread = spread.getI() / 100f;
		Colors.chroma = chroma.get();
		Colors.chromaSpeed = chromaSpeed.getI() / 100f;
		Colors.setTheme(theme.get());
	}

	@Override
	public void onTick() {
		apply();
	}
}
