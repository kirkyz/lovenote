package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

public class MaxFps extends Module {
	private final NumberSetting fps = add(new NumberSetting("Fps", 144, 30, 500, 5));
	private int saved = -1;

	public MaxFps() {
		super("MaxFps", Category.RENDER);
		describe("Caps the framerate.");
	}

	@Override
	public void onEnable() {
		if (saved < 0) saved = mc.options.getMaxFps().getValue();
		mc.options.getMaxFps().setValue(fps.getI());
	}

	@Override
	public void onTick() {
		if (mc.options.getMaxFps().getValue() != fps.getI()) mc.options.getMaxFps().setValue(fps.getI());
	}

	@Override
	public void onDisable() {
		if (saved >= 0) mc.options.getMaxFps().setValue(saved);
	}
}
