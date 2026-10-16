package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;

public class Bhop extends Module {
	public Bhop() {
		super("Bhop", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		boolean moving = mc.player.input.hasForwardMovement() || mc.player.input.getMovementInput().x != 0f;
		if (mc.player.isOnGround() && moving && !mc.player.isSneaking()) mc.player.jump();
	}
}
