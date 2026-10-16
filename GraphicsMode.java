package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;

public class GraphicsMode extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Fancy", "Fast", "Fancy", "Fabulous"));

	public GraphicsMode() {
		super("GraphicsMode", Category.RENDER);
		describe("Forces fast, fancy or fabulous graphics.");
	}

	@Override
	public void onTick() {
		net.minecraft.client.option.GraphicsMode want = switch (mode.get()) {
			case "Fast" -> net.minecraft.client.option.GraphicsMode.FAST;
			case "Fabulous" -> net.minecraft.client.option.GraphicsMode.FABULOUS;
			default -> net.minecraft.client.option.GraphicsMode.FANCY;
		};
		if (mc.options.getPreset().getValue() != want) mc.options.getPreset().setValue(want);
	}
}
