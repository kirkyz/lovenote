package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class AntiLevitation extends Module {
	public AntiLevitation() {
		super("AntiLevitation", Category.PLAYER);
		describe("Cancels levitation so you can drop on command.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (!mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return;
		Vec3d v = mc.player.getVelocity();
		if (v.y > 0) mc.player.setVelocity(v.x, 0, v.z);
	}
}