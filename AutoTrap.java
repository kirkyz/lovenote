package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.BlockUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Boxes in the nearest player with obsidian. */
public class AutoTrap extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 5.0, 2.0, 6.0, 0.5));
	private final NumberSetting perTick = add(new NumberSetting("Blocks per tick", 2, 1, 4, 1));
	private final BoolSetting top = add(new BoolSetting("Top cover", true));

	public AutoTrap() {
		super("AutoTrap", Category.COMBAT);
	}

	static PlayerEntity nearest(double range) {
		var mc = net.minecraft.client.MinecraftClient.getInstance();
		PlayerEntity best = null;
		double bd = range;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
			double d = mc.player.distanceTo(p);
			if (d <= bd) { bd = d; best = p; }
		}
		return best;
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (BlockUtil.findHotbar(Items.OBSIDIAN) < 0) return;
		PlayerEntity t = nearest(range.get());
		if (t == null) return;
		BlockPos feet = t.getBlockPos();
		int placed = 0;
		for (int layer = 0; layer < 2 && placed < perTick.getI(); layer++) {
			for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
				if (placed >= perTick.getI()) break;
				BlockPos p = feet.up(layer).offset(d);
				if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(p)) > 4.6) continue;
				if (BlockUtil.placeItem(p, Items.OBSIDIAN)) placed++;
			}
		}
		if (top.get() && placed < perTick.getI()) BlockUtil.placeItem(feet.up(2), Items.OBSIDIAN);
	}
}
