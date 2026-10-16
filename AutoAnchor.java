package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoAnchor extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 5, 2, 8, 0.5));
	private final BoolSetting charge = add(new BoolSetting("Charge with glowstone", true));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 2, 0, 10, 1));

	private int timer;

	public AutoAnchor() {
		super("AutoAnchor", Category.COMBAT);
		describe("Charges and pops respawn anchors around you.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
		if (timer > 0) { timer--; return; }
		int r = (int) Math.ceil(range.get());
		double rr = range.get() * range.get();
		BlockPos o = mc.player.getBlockPos();
		boolean holdingGlow = mc.player.getMainHandStack().isOf(Items.GLOWSTONE);
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					BlockPos p = o.add(dx, dy, dz);
					if (!mc.world.getBlockState(p).isOf(Blocks.RESPAWN_ANCHOR)) continue;
					Vec3d c = Vec3d.ofCenter(p);
					if (mc.player.squaredDistanceTo(c) > rr) continue;
					int charges = mc.world.getBlockState(p).get(RespawnAnchorBlock.CHARGES);
					boolean want = charges > 0 && !holdingGlow;
					if (charges == 0 && holdingGlow && charge.get()) want = true;
					if (!want) continue;
					mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
							new BlockHitResult(c, Direction.UP, p, false));
					mc.player.swingHand(Hand.MAIN_HAND);
					timer = delay.getI();
					return;
				}
			}
		}
	}
}