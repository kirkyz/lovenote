package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SafeWalk extends Module {
	private boolean pressed;

	public SafeWalk() {
		super("SafeWalk", Category.PLAYER);
		describe("Sneaks at the edge of blocks so you never fall.");
	}

	private boolean unsafe() {
		if (mc.player == null || mc.world == null) return false;
		if (!mc.player.isOnGround()) return false;
		Vec3d v = mc.player.getVelocity();
		if (Math.abs(v.x) < 0.01 && Math.abs(v.z) < 0.01) return false;
		BlockPos p = mc.player.getBlockPos();
		double fx = p.getX() + 0.5 + Math.signum(v.x) * 0.45;
		double fz = p.getZ() + 0.5 + Math.signum(v.z) * 0.45;
		BlockPos below = new BlockPos((int) Math.floor(fx), p.getY() - 1, (int) Math.floor(fz));
		return mc.world.getBlockState(below).isAir();
	}

	@Override
	public void onTick() {
		boolean u = unsafe();
		if (u && !pressed) { mc.options.sneakKey.setPressed(true); pressed = true; }
		else if (!u && pressed) { mc.options.sneakKey.setPressed(false); pressed = false; }
	}

	@Override
	public void onDisable() {
		if (pressed) { mc.options.sneakKey.setPressed(false); pressed = false; }
	}
}