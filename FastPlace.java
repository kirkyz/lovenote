package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/** Removes the 4 tick block place cooldown. */
public class FastPlace extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay", 0, 0, 4, 1));
	private final BoolSetting blocksOnly = add(new BoolSetting("Blocks only", true));

	public FastPlace() {
		super("FastPlace", Category.MISC);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (blocksOnly.get()) {
			ItemStack held = mc.player.getMainHandStack();
			if (held.isEmpty() || !(held.getItem() instanceof BlockItem)) return;
		}
		if (mc.itemUseCooldown > delay.getI()) mc.itemUseCooldown = delay.getI();
	}
}