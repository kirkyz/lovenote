package com.lovenote.module.impl;

import com.lovenote.mixin.MinecraftClientAccessor;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

/** Removes (or shortens) the delay between placing blocks / using items. */
public class FastPlace extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 0, 0, 4, 1));

	public FastPlace() {
		super("FastPlace", Category.MISC);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		((MinecraftClientAccessor) mc).lovenote$setItemUseCooldown(delay.getI());
	}
}
