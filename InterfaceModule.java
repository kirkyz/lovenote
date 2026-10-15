package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;

/** Holds the colour theme and menu layout. Pick "Custom" to unlock the HSV picker. */
public class InterfaceModule extends Module {
	public final ModeSetting theme = add(new ModeSetting("Theme", "Lovenote", Colors.themeNames()));

	public final NumberSetting hue = add(new NumberSetting("Hue", 330, 0, 360, 1));
	public final NumberSetting saturation = add(new NumberSetting("Saturation", 100, 0, 100, 1));
	public final NumberSetting brightness = add(new NumberSetting("Brightness", 100, 0, 100, 1));
	public final NumberSetting spread = add(new NumberSetting("Gradient spread", 18, 0, 60, 1));
	public final BoolSetting chroma = add(new BoolSetting("Chroma cycle", false));
	public final NumberSetting chromaSpeed = add(new NumberSetting("Chroma speed", 12, 1, 60, 1));

	// ---- menu layout
	public final NumberSetting uiScale = add(new NumberSetting("UI scale", 100, 60, 160, 5));
	public final NumberSetting panelWidth = add(new NumberSetting("Panel width", 430, 340, 900, 10));
	public final NumberSetting panelHeight = add(new NumberSetting("Panel height", 265, 200, 700, 5));
	public final NumberSetting corner = add(new NumberSetting("Corner radius", 6, 0, 14, 1));
	public final NumberSetting rowHeight = add(new NumberSetting("Row height", 16, 14, 26, 1));
	public final NumberSetting textScale = add(new NumberSetting("Text scale %", 88, 70, 110, 1));
	public final NumberSetting dim = add(new NumberSetting("Backdrop dim", 110, 0, 220, 5));

	public final BoolSetting animations = add(new BoolSetting("Animations", true));
	public final BoolSetting columns = add(new BoolSetting("Two columns", true));
	public final BoolSetting tooltips = add(new BoolSetting("Tooltips", true));
	public final BoolSetting descriptions = add(new BoolSetting("Card descriptions", true));
	public final BoolSetting glass = add(new BoolSetting("Glass layers", true));
	public final BoolSetting glow = add(new BoolSetting("Accent glow", true));
	public final BoolSetting logo = add(new BoolSetting("Drain logo", true));
	public final BoolSetting particles = add(new BoolSetting("Backdrop particles", true));
	public final BoolSetting sidebarIcons = add(new BoolSetting("Sidebar icons", true));
	public final BoolSetting keybindHints = add(new BoolSetting("Show binds", true));

	public InterfaceModule() {
		super("Interface", Category.HUD);
		describe("Theme, layout and menu behaviour.");
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