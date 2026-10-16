package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Drops you faster so you land first. */
public class FastFall extends Module {
	private final NumberSetting speed = add(new NumberSetting("Speed", 2.0, 1.2, 5.0, 0.1));
	private final BoolSetting onlySneak = add(new BoolSetting("Only when sneaking", false));

	public FastFall() {
		super("FastFall", Category.MOVEMENT);
		describe("Drops faster so you land first.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (onlySneak.get() && !mc.player.isSneaking()) return;
		if (mc.player.isOnGround() || mc.player.isGliding() || mc.player.isTouchingWater()) return;
		Vec3d v = mc.player.getVelocity();
		if (v.y >= 0) return;
		mc.player.setVelocity(v.x, v.y * speed.get(), v.z);
	}
}