package com.lovenote.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class BlockUtil {
	private BlockUtil() {}

	public static int findHotbar(Item item) {
		MinecraftClient mc = MinecraftClient.getInstance();
		for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isOf(item)) return i;
		return -1;
	}

	public static int findAnyBlock() {
		MinecraftClient mc = MinecraftClient.getInstance();
		for (int i = 0; i < 9; i++) {
			ItemStack s = mc.player.getInventory().getStack(i);
			if (!s.isEmpty() && s.getItem() instanceof BlockItem) return i;
		}
		return -1;
	}

	public static boolean placeItem(BlockPos pos, Item item) {
		return placeSlot(pos, findHotbar(item));
	}

	public static boolean placeAnyBlock(BlockPos pos) {
		return placeSlot(pos, findAnyBlock());
	}

	private static boolean placeSlot(BlockPos pos, int slot) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (slot < 0 || mc.world == null || mc.player == null || mc.interactionManager == null) return false;
		if (!mc.world.getBlockState(pos).isAir()) return false;

		for (Direction d : Direction.values()) {
			BlockPos n = pos.offset(d);
			if (mc.world.getBlockState(n).isAir()) continue;
			Direction face = d.getOpposite();
			Vec3d hit = Vec3d.ofCenter(n).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
			PlayerInventory inv = mc.player.getInventory();
			int prev = inv.getSelectedSlot();
			inv.setSelectedSlot(slot);
			mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(hit, face, n, false));
			mc.player.swingHand(Hand.MAIN_HAND);
			inv.setSelectedSlot(prev);
			return true;
		}
		return false;
	}
}
