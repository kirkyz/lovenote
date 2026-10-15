package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

/** Custom field of view. Bind it to a key to use as a zoom. */
public class Zoom extends Module {
	private final NumberSetting fov = add(new NumberSetting("FOV", 30, 30, 110, 1));
	private int old = -1;

	public Zoom() {
		super("Zoom", Category.VISUALS);
	}

	@Override
	public void onEnable() {
		if (mc.options != null) old = mc.options.getFov().getValue();
	}

	@Override
	public void onTick() {
		if (mc.options != null) mc.options.getFov().setValue(fov.getI());
	}

	@Override
	public void onDisable() {
		if (mc.options != null && old != -1) mc.options.getFov().setValue(old);
		old = -1;
	}
}
