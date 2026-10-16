package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** Jumps automatically at the edge of a block. */
public class Parkour extends Module {
	public Parkour() {
		super("Parkour", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || !mc.player.isOnGround() || mc.player.isSneaking()) return;
		Vec3d v = mc.player.getVelocity();
		if (v.horizontalLengthSquared() < 0.005) return;
		Vec3d ahead = mc.player.getEntityPos().add(v.x * 3.0, -0.5, v.z * 3.0);
		if (mc.world.getBlockState(BlockPos.ofFloored(ahead)).isAir() && mc.world.getBlockState(BlockPos.ofFloored(ahead).down()).isAir()) {
			mc.player.jump();
		}
	}
}
