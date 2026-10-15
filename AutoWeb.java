package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.BlockUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/** Places blocks around the target so they can't run. */
public class AutoWeb extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 5, 2, 10, 1));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 2, 0, 10, 1));
	private final NumberSetting layers = add(new NumberSetting("Layers", 2, 1, 4, 1));
	private final BoolSetting above = add(new BoolSetting("Above head", true));

	private int timer;

	public AutoWeb() {
		super("AutoWeb", Category.COMBAT);
		describe("Places blocks around the target's feet.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (timer > 0) { timer--; return; }
		PlayerEntity t = nearest();
		if (t == null) return;
		BlockPos base = t.getBlockPos();
		for (int y = 0; y < layers.getI(); y++) {
			BlockPos p = base.up(y);
			if (!mc.world.getBlockState(p).isAir()) continue;
			BlockUtil.placeAnyBlock(p);
			timer = delay.getI();
			return;
		}
		if (above.get()) {
			BlockPos p = base.up(2);
			if (mc.world.getBlockState(p).isAir()) {
				BlockUtil.placeAnyBlock(p);
				timer = delay.getI();
			}
		}
	}

	private PlayerEntity nearest() {
		PlayerEntity best = null;
		double bd = range.get();
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive()) continue;
			double d = mc.player.distanceTo(p);
			if (d < bd) { bd = d; best = p; }
		}
		return best;
	}
}