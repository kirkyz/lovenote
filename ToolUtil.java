package com.lovenote.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public final class ToolUtil {
	private ToolUtil() {}

	/** Hotbar slot with the fastest tool for this block, or -1. */
	public static int bestSlot(BlockState state) {
		MinecraftClient mc = MinecraftClient.getInstance();
		int best = -1;
		float bestSpeed = 1.0f;
		for (int i = 0; i < 9; i++) {
			ItemStack st = mc.player.getInventory().getStack(i);
			float sp = st.getMiningSpeedMultiplier(state);
			if (sp > bestSpeed) { bestSpeed = sp; best = i; }
		}
		return best;
	}
}
