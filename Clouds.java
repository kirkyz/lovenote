package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import net.minecraft.client.option.CloudRenderMode;

public class Clouds extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Off", "Off", "Fast", "Fancy"));

	public Clouds() {
		super("Clouds", Category.RENDER);
		describe("Cloud render mode, including fully off.");
	}

	@Override
	public void onTick() {
		CloudRenderMode want = switch (mode.get()) {
			case "Fast" -> CloudRenderMode.FAST;
			case "Fancy" -> CloudRenderMode.FANCY;
			default -> CloudRenderMode.OFF;
		};
		if (mc.options.getCloudRenderMode().getValue() != want) mc.options.getCloudRenderMode().setValue(want);
	}
}
