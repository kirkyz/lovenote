package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.option.SimpleOption;

public class RenderDistance extends Module {
	private final NumberSetting chunks = add(new NumberSetting("Chunks", 12, 2, 32, 1));
	private int saved = -1;

	public RenderDistance() {
		super("RenderDistance", Category.RENDER);
		describe("Overrides the client view distance.");
	}

	@Override
	public void onEnable() {
		SimpleOption<Integer> o = mc.options.getViewDistance();
		if (saved < 0) saved = o.getValue();
		o.setValue(chunks.getI());
	}

	@Override
	public void onTick() {
		SimpleOption<Integer> o = mc.options.getViewDistance();
		if (o.getValue() != chunks.getI()) o.setValue(chunks.getI());
	}

	@Override
	public void onDisable() {
		if (saved >= 0) mc.options.getViewDistance().setValue(saved);
	}
}
