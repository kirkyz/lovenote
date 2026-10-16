package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;

public class Weather extends Module {
	private final ModeSetting mode = add(new ModeSetting("Weather", "Clear", "Clear", "Rain", "Thunder"));

	public Weather() {
		super("Weather", Category.RENDER);
		describe("Client-side weather override.");
	}

	@Override
	public void onTick() {
		if (mc.world == null) return;
		float rain = mode.is("Clear") ? 0f : 1f;
		float thunder = mode.is("Thunder") ? 1f : 0f;
		if (mc.world.getRainGradient(1f) != rain) mc.world.setRainGradient(rain);
		if (mc.world.getThunderGradient(1f) != thunder) mc.world.setThunderGradient(thunder);
	}
}
