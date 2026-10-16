package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;

public class SmoothCamera extends Module {
	private final BoolSetting on = add(new BoolSetting("Enabled", true));

	public SmoothCamera() {
		super("SmoothCamera", Category.RENDER);
		describe("Cinematic smoothed camera movement.");
	}

	@Override
	public void onEnable() { mc.options.smoothCameraEnabled = on.get(); }

	@Override
	public void onTick() {
		if (mc.options.smoothCameraEnabled != on.get()) mc.options.smoothCameraEnabled = on.get();
	}

	@Override
	public void onDisable() { mc.options.smoothCameraEnabled = false; }
}
