package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

/** Client-side time of day (0 = sunrise, 6000 = noon, 12000 = sunset, 18000 = midnight). */
public class TimeChanger extends Module {
	private final NumberSetting time = add(new NumberSetting("Time", 6000, 0, 24000, 500));

	public TimeChanger() {
		super("TimeChanger", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.world != null) mc.world.setTime(mc.world.getTime(), time.getI(), false);
	}
}
