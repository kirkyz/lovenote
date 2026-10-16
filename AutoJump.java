package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;

public class AutoJump extends Module {
	private final BoolSetting on = add(new BoolSetting("Enabled", true));

	public AutoJump() {
		super("AutoJump", Category.PLAYER);
		describe("Vanilla auto jump toggle.");
	}

	@Override
	public void onTick() {
		if (mc.options.getAutoJump().getValue() != on.get()) mc.options.getAutoJump().setValue(on.get());
	}
}
