package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

/** Places end crystals on obsidian/bedrock under your crosshair and detonates crystals you look at. */
public class CrystalTap extends Module {
	private int timer;

	public CrystalTap() {
		super("CrystalTap", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null) return;
		if (timer > 0) { timer--; return; }
		HitResult h = mc.crosshairTarget;

		if (h instanceof EntityHitResult er && er.getEntity() instanceof EndCrystalEntity) {
			mc.interactionManager.attackEntity(mc.player, er.getEntity());
			mc.player.swingHand(Hand.MAIN_HAND);
			timer = 2;
			return;
		}
		if (h instanceof BlockHitResult br && h.getType() == HitResult.Type.BLOCK
				&& mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) && br.getSide() == Direction.UP) {
			BlockState st = mc.world.getBlockState(br.getBlockPos());
			if (st.isOf(Blocks.OBSIDIAN) || st.isOf(Blocks.BEDROCK)) {
				Box above = new Box(br.getBlockPos().up());
				if (mc.world.getEntitiesByClass(EndCrystalEntity.class, above, e -> true).isEmpty()) {
					mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, br);
					mc.player.swingHand(Hand.MAIN_HAND);
					timer = 3;
				}
			}
		}
	}
}
