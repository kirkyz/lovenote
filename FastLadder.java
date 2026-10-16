package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class FastLadder extends Module {
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.28, 0.12, 0.6, 0.02));

	public FastLadder() {
		super("FastLadder", Category.PLAYER);
		describe("Climb ladders and vines much faster.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.options == null) return;
		if (!mc.player.isClimbing() || !mc.player.horizontalCollision) return;
		if (!mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed()) return;
		Vec3d v = mc.player.getVelocity();
		mc.player.setVelocity(v.x, speed.get(), v.z);
	}
}
