package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.BlockUtil;
import net.minecraft.util.math.BlockPos;

/** Places blocks from your hotbar under your feet as you walk. */
public class Scaffold extends Module {
	public Scaffold() {
		super("Scaffold", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		BlockPos below = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ());
		BlockUtil.placeAnyBlock(below);
	}
}
