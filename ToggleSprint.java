package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;

public class ToggleSprint extends Module {
	private final BoolSetting sneakToo = add(new BoolSetting("Also toggle sneak", false));

	public ToggleSprint() {
		super("ToggleSprint", Category.PLAYER);
		describe("Sprint stays on without holding the key.");
	}

	@Override
	public void onTick() {
		if (!mc.options.getSprintToggled().getValue()) mc.options.getSprintToggled().setValue(true);
		if (sneakToo.get() && !mc.options.getSneakToggled().getValue()) mc.options.getSneakToggled().setValue(true);
	}
}
