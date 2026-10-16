package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;

public class EntityShadows extends Module {
	private final BoolSetting show = add(new BoolSetting("Show shadows", false));

	public EntityShadows() {
		super("EntityShadows", Category.RENDER);
		describe("Toggles entity drop shadows.");
	}

	@Override
	public void onTick() {
		if (mc.options.getEntityShadows().getValue() != show.get())
			mc.options.getEntityShadows().setValue(show.get());
	}
}
