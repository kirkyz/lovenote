package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.BlockUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

/** Places cobwebs on the nearest player. */
public class AutoWeb extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 4.5, 2.0, 6.0, 0.5));

	public AutoWeb() {
		super("AutoWeb", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (BlockUtil.findHotbar(Items.COBWEB) < 0) return;
		PlayerEntity t = AutoTrap.nearest(range.get());
		if (t == null) return;
		BlockPos feet = t.getBlockPos();
		if (!BlockUtil.placeItem(feet, Items.COBWEB)) BlockUtil.placeItem(feet.up(), Items.COBWEB);
	}
}
