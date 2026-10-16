package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

public class BiomeBlend extends Module {
	private final NumberSetting radius = add(new NumberSetting("Radius", 2, 0, 7, 1));

	public BiomeBlend() {
		super("BiomeBlend", Category.RENDER);
		describe("Biome colour blend radius.");
	}

	@Override
	public void onTick() {
		if (mc.options.getBiomeBlendRadius().getValue() != radius.getI())
			mc.options.getBiomeBlendRadius().setValue(radius.getI());
	}
}
