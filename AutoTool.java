package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.ToolUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** Switches to the best tool while you mine, then switches back. */
public class AutoTool extends Module {
	private int prev = -1;

	public AutoTool() {
		super("AutoTool", Category.MISC);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		boolean mining = mc.options.attackKey.isPressed() && mc.currentScreen == null
				&& mc.crosshairTarget instanceof BlockHitResult br && mc.crosshairTarget.getType() == HitResult.Type.BLOCK;
		if (mining) {
			BlockHitResult br = (BlockHitResult) mc.crosshairTarget;
			int slot = ToolUtil.bestSlot(mc.world.getBlockState(br.getBlockPos()));
			if (slot != -1 && slot != mc.player.getInventory().getSelectedSlot()) {
				if (prev == -1) prev = mc.player.getInventory().getSelectedSlot();
				mc.player.getInventory().setSelectedSlot(slot);
			}
		} else if (prev != -1) {
			mc.player.getInventory().setSelectedSlot(prev);
			prev = -1;
		}
	}

	@Override
	public void onDisable() {
		if (prev != -1 && mc.player != null) mc.player.getInventory().setSelectedSlot(prev);
		prev = -1;
	}
}
