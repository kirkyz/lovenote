package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoPearl extends Module {
	private final NumberSetting fall = add(new NumberSetting("Fall distance", 4, 2, 20, 1));

	private int timer;

	public AutoPearl() {
		super("AutoPearl", Category.PLAYER);
		describe("Throws a pearl when you fall too far.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null) return;
		if (timer > 0) { timer--; return; }
		if (mc.player.fallDistance < fall.getF()) return;
		if (!mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)
				&& !mc.player.getOffHandStack().isOf(Items.ENDER_PEARL)) return;
		mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
		mc.player.swingHand(Hand.MAIN_HAND);
		timer = 10;
	}
}