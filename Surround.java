package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.BlockUtil;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Surrounds your feet with obsidian (needs obsidian in the hotbar). */
public class Surround extends Module {
	private final NumberSetting perTick = add(new NumberSetting("Blocks per tick", 2, 1, 4, 1));

	public Surround() {
		super("Surround", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (BlockUtil.findHotbar(Items.OBSIDIAN) < 0) return;
		BlockPos feet = mc.player.getBlockPos();
		int placed = 0;
		for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
			if (placed >= perTick.getI()) break;
			if (BlockUtil.placeItem(feet.offset(d), Items.OBSIDIAN)) placed++;
		}
	}
}
