package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.ToolUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Mines the obsidian that surrounds the nearest player so crystals can reach them. */
public class AutoCity extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 4.5, 2.0, 6.0, 0.5));
	private BlockPos current;
	private int prevSlot = -1;

	public AutoCity() {
		super("AutoCity", Category.COMBAT);
	}

	private static boolean mineable(BlockState s) {
		Block b = s.getBlock();
		return b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN || b == Blocks.ENDER_CHEST || b == Blocks.ANCIENT_DEBRIS;
	}

	private void reset() {
		current = null;
		if (prevSlot != -1 && mc.player != null) mc.player.getInventory().setSelectedSlot(prevSlot);
		prevSlot = -1;
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
		if (current != null && !mineable(mc.world.getBlockState(current))) reset();

		if (current == null) {
			PlayerEntity t = AutoTrap.nearest(range.get() + 2);
			if (t == null) return;
			for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
				BlockPos p = t.getBlockPos().offset(d);
				if (mineable(mc.world.getBlockState(p)) && mc.player.getEyePos().distanceTo(Vec3d.ofCenter(p)) <= range.get()) {
					current = p;
					break;
				}
			}
			if (current == null) return;
			int slot = ToolUtil.bestSlot(mc.world.getBlockState(current));
			if (slot != -1) { prevSlot = mc.player.getInventory().getSelectedSlot(); mc.player.getInventory().setSelectedSlot(slot); }
			mc.interactionManager.attackBlock(current, Direction.UP);
		} else {
			mc.interactionManager.updateBlockBreakingProgress(current, Direction.UP);
		}
		mc.player.swingHand(Hand.MAIN_HAND);
	}

	@Override
	public void onDisable() {
		reset();
	}
}
