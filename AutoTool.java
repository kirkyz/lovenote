package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/** Swaps to the fastest tool for whatever you are looking at. */
public class AutoTool extends Module {
	private final BoolSetting onlyMining = add(new BoolSetting("Only while mining", true));
	private final BoolSetting keep = add(new BoolSetting("Keep best tool", false));

	private int original = -1;

	public AutoTool() {
		super("AutoTool", Category.MISC);
	}

	@Override
	public void onDisable() {
		if (keep.get() && original != -1 && mc.player != null) mc.player.getInventory().selectedSlot = original;
		original = -1;
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (mc.currentScreen != null) return;
		if (onlyMining.get() && !mc.options.attackKey.isPressed()) return;
		if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

		BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
		BlockState state = mc.world.getBlockState(pos);
		if (state.isAir()) return;

		int best = -1;
		float bestSpeed = 1.0f;
		for (int i = 0; i < 9; i++) {
			ItemStack st = mc.player.getInventory().getStack(i);
			if (st.isEmpty()) continue;
			float speed = st.getMiningSpeedMultiplier(state);
			if (speed > bestSpeed) { bestSpeed = speed; best = i; }
		}
		if (best == -1) return;

		int sel = mc.player.getInventory().selectedSlot;
		if (best == sel) return;
		if (original == -1) original = sel;
		mc.player.getInventory().selectedSlot = best;
	}
}