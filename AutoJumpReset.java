package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class AutoJumpReset extends Module {
	private final NumberSetting cooldown = add(new NumberSetting("Cooldown (ticks)", 5, 1, 20, 1));

	private int timer;

	public AutoJumpReset() {
		super("AutoJumpReset", Category.COMBAT);
		describe("Jumps on hit to reset your sprint and take less knockback.");
	}

	@Override
	public void onAttack(Entity target) {
		if (timer > 0 || mc.player == null) return;
		if (!mc.player.isOnGround()) return;
		Vec3d v = mc.player.getVelocity();
		mc.player.setVelocity(v.x, 0.42, v.z);
		timer = cooldown.getI();
	}

	@Override
	public void onTick() {
		if (timer > 0) timer--;
	}
}