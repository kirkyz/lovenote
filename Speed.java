package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;

public class Speed extends Module {
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.32, 0.1, 1.0, 0.01));
	private final BoolSetting autoJump = add(new BoolSetting("Auto jump", true));

	public Speed() {
		super("Speed", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		double f = mc.player.input.getMovementInput().y, s = mc.player.input.getMovementInput().x;
		if (f == 0 && s == 0) return;
		if (mc.player.isSneaking() || mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.isGliding()) return;
		if (autoJump.get() && mc.player.isOnGround()) mc.player.jump();

		double len = Math.sqrt(f * f + s * s);
		f /= len;
		s /= len;
		double rad = Math.toRadians(mc.player.getYaw());
		double sin = Math.sin(rad), cos = Math.cos(rad);
		double sp = speed.get();
		mc.player.setVelocity((-sin * f + cos * s) * sp, mc.player.getVelocity().y, (cos * f + sin * s) * sp);
	}
}
