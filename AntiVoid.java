package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

/** Bounces you back up if you are about to fall into the void. */
public class AntiVoid extends Module {
	private final NumberSetting height = add(new NumberSetting("Trigger height", 4, 1, 16, 1));

	public AntiVoid() {
		super("AntiVoid", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (mc.player.getY() < mc.world.getBottomY() + height.get() && !mc.player.isOnGround() && mc.player.fallDistance > 2f) {
			mc.player.setVelocity(mc.player.getVelocity().x, 0.7, mc.player.getVelocity().z);
			mc.player.fallDistance = 0f;
		}
	}
}
