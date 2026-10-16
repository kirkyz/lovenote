package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import net.minecraft.client.option.GraphicsMode;

public class GraphicsMode extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Fancy", "Fast", "Fancy", "Fabulous"));

	public GraphicsMode() {
		super("GraphicsMode", Category.RENDER);
		describe("Forces fast, fancy or fabulous graphics.");
	}

	@Override
	public void onTick() {
		GraphicsMode want = switch (mode.get()) {
			case "Fast" -> GraphicsMode.FAST;
			case "Fabulous" -> GraphicsMode.FABULOUS;
			default -> GraphicsMode.FANCY;
		};
		if (mc.options.getGraphicsMode().getValue() != want) mc.options.getGraphicsMode().setValue(want);
	}
}