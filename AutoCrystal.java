package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Places end crystals next to the nearest player and breaks them, weighing target vs self damage. */
public class AutoCrystal extends Module {
	private final NumberSetting targetRange = add(new NumberSetting("Target range", 10, 4, 14, 1));
	private final NumberSetting placeRange = add(new NumberSetting("Place range", 4.5, 2.0, 6.0, 0.1));
	private final NumberSetting breakRange = add(new NumberSetting("Break range", 4.5, 2.0, 6.0, 0.1));
	private final NumberSetting minDamage = add(new NumberSetting("Min damage", 4, 1, 20, 1));
	private final NumberSetting maxSelf = add(new NumberSetting("Max self damage", 6, 1, 20, 1));
	private final NumberSetting placeDelay = add(new NumberSetting("Place delay", 2, 0, 10, 1));
	private final NumberSetting breakDelay = add(new NumberSetting("Break delay", 1, 0, 10, 1));
	private final BoolSetting place = add(new BoolSetting("Place", true));
	private final BoolSetting breakC = add(new BoolSetting("Break", true));

	private int pTimer, bTimer;

	public AutoCrystal() {
		super("AutoCrystal", Category.COMBAT);
	}

	/** Rough vanilla-style explosion damage at 'pos' for entity e (power 6, no blocks / armour). */
	private static double damage(Vec3d crystal, Entity e) {
		double dist = crystal.distanceTo(e.getEntityPos().add(0, e.getHeight() / 2, 0)) / 12.0;
		if (dist > 1.0) return 0;
		double impact = 1.0 - dist;
		return (impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0;
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
		PlayerEntity target = AutoTrap.nearest(targetRange.get());
		if (pTimer > 0) pTimer--;
		if (bTimer > 0) bTimer--;

		if (breakC.get() && bTimer == 0) {
			for (Entity e : mc.world.getEntities()) {
				if (!(e instanceof EndCrystalEntity) || mc.player.distanceTo(e) > breakRange.get()) continue;
				double self = damage(e.getEntityPos(), mc.player);
				double tgt = target == null ? 0 : damage(e.getEntityPos(), target);
				if (self > maxSelf.get() && tgt < minDamage.get() * 2) continue;
				if (target != null && tgt < 1 && self > 1) continue;
				mc.interactionManager.attackEntity(mc.player, e);
				mc.player.swingHand(Hand.MAIN_HAND);
				bTimer = breakDelay.getI();
				return;
			}
		}

		if (!place.get() || pTimer > 0 || target == null) return;
		boolean off = mc.player.getOffHandStack().isOf(Items.END_CRYSTAL);
		if (!off && !mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) return;

		BlockPos tp = target.getBlockPos();
		BlockPos best = null;
		double bestScore = -999;
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				for (int dy = -1; dy <= 1; dy++) {
					BlockPos b = tp.add(dx, dy - 1, dz);
					if (!mc.world.getBlockState(b).isOf(Blocks.OBSIDIAN) && !mc.world.getBlockState(b).isOf(Blocks.BEDROCK)) continue;
					if (!mc.world.getBlockState(b.up()).isAir() || !mc.world.getBlockState(b.up(2)).isAir()) continue;
					Vec3d center = new Vec3d(b.getX() + 0.5, b.getY() + 1.0, b.getZ() + 0.5);
					if (mc.player.getEyePos().distanceTo(center) > placeRange.get()) continue;
					List<Entity> blocking = mc.world.getOtherEntities(null, new Box(b.up()));
					if (!blocking.isEmpty()) continue;
					double tgt = damage(center, target), self = damage(center, mc.player);
					if (tgt < minDamage.get() || self > maxSelf.get()) continue;
					double score = tgt - self;
					if (score > bestScore) { bestScore = score; best = b; }
				}
			}
		}
		if (best == null) return;
		Hand hand = off ? Hand.OFF_HAND : Hand.MAIN_HAND;
		Vec3d hit = new Vec3d(best.getX() + 0.5, best.getY() + 1.0, best.getZ() + 0.5);
		mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(hit, Direction.UP, best, false));
		mc.player.swingHand(hand);
		pTimer = placeDelay.getI();
	}
}
