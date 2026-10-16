package com.lovenote.module.impl;

import com.lovenote.mixin.MinecraftClientAccessor;
import com.lovenote.module.Category;
import com.lovenote.module.Module;

public class FastUse extends Module {
	public FastUse() {
		super("FastUse", Category.PLAYER);
		describe("Removes the item use delay for instant eat and throw.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || !mc.player.isUsingItem()) return;
		((MinecraftClientAccessor) (Object) mc).lovenote$setItemUseCooldown(0);
	}
}
