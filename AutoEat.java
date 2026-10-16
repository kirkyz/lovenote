package com.lovenote.module.impl;
import net.minecraft.component.DataComponentTypes;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;

/** Eats for you when hunger drops. */
public class AutoEat extends Module {
	private final NumberSetting hunger = add(new NumberSetting("Hunger", 14, 1, 19, 1));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 4, 0, 20, 1));

	private int timer;
	private int prevSlot = -1;
	private boolean eating;

	public AutoEat() {
		super("AutoEat", Category.MISC);
		describe("Eats when you get hungry.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.options == null) return;
		if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) {
			if (eating) stop();
			return;
		}
		if (timer > 0) { timer--; return; }

		if (eating) {
			if (!mc.player.isUsingItem()) stop();
			return;
		}
		if (mc.player.getHungerManager().getFoodLevel() > hunger.getI()) return;

		var inv = mc.player.getInventory();
		for (int i = 0; i < 9; i++) {
			ItemStack s = inv.getStack(i);
			if (s.isEmpty() || !s.contains(DataComponentTypes.FOOD)) continue;
			prevSlot = inv.getSelectedSlot();
			inv.setSelectedSlot(i);
			mc.options.useKey.setPressed(true);
			eating = true;
			timer = delay.getI();
			return;
		}
	}

	private void stop() {
		mc.options.useKey.setPressed(false);
		if (prevSlot >= 0) mc.player.getInventory().setSelectedSlot(prevSlot);
		prevSlot = -1;
		eating = false;
	}

	@Override
	public void onDisable() {
		if (mc.player != null && mc.options != null && eating) stop();
	}
}