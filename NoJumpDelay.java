package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;

public class NoJumpDelay extends Module {
	public NoJumpDelay() {
		super("NoJumpDelay", Category.PLAYER);
		describe("Removes the cooldown between jumps.");
	}

	@Override
	public void onTick() {
		if (mc.player != null && mc.player.jumpingCooldown > 0) mc.player.jumpingCooldown = 0;
	}
}