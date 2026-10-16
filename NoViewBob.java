package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;

public class NoViewBob extends Module {
	private final BoolSetting hide = add(new BoolSetting("Hide bobbing", true));

	public NoViewBob() {
		super("NoViewBob", Category.RENDER);
		describe("Removes walking camera bob.");
	}

	@Override
	public void onTick() {
		if (mc.options.getBobView().getValue() != hide.get()) mc.options.getBobView().setValue(hide.get());
	}
}
