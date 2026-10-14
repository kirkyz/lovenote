package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.util.Colors;

/** Holds the colour theme. */
public class InterfaceModule extends Module {
	public final ModeSetting theme = add(new ModeSetting("Theme", "Lovenote", "Lovenote", "Sakura", "Midnight", "Mint", "Skeet"));

	public InterfaceModule() {
		super("Interface", Category.HUD);
	}

	public void apply() {
		Colors.setTheme(theme.get());
	}

	@Override
	public void onTick() {
		apply();
	}
}
