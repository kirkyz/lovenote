package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;

public class Sprint extends Module {
	public Sprint() {
		super("Sprint", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (mc.player.input.hasForwardMovement() && !mc.player.isSneaking() && !mc.player.horizontalCollision
				&& mc.player.getHungerManager().getFoodLevel() > 6) {
			mc.player.setSprinting(true);
		}
	}
}
