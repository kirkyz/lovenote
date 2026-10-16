package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

public class FovOverride extends Module {
	private final NumberSetting fov = add(new NumberSetting("Fov", 90, 30, 110, 1));
	private int saved = -1;

	public FovOverride() {
		super("FovOverride", Category.RENDER);
		describe("Locks your field of view.");
	}

	@Override
	public void onEnable() {
		if (saved < 0) saved = mc.options.getFov().getValue();
		mc.options.getFov().setValue(fov.getI());
	}

	@Override
	public void onTick() {
		if (mc.options.getFov().getValue() != fov.getI()) mc.options.getFov().setValue(fov.getI());
	}

	@Override
	public void onDisable() {
		if (saved >= 0) mc.options.getFov().setValue(saved);
	}
}
