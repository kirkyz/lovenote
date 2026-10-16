package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.item.Items;

/** Eats a golden apple from your hotbar when health is low. */
public class AutoGApple extends Module {
	private final NumberSetting health = add(new NumberSetting("Health", 12, 1, 20, 1));
	private int prev = -1;
	private boolean eating;

	public AutoGApple() {
		super("AutoGApple", Category.COMBAT);
	}

	private int findApple() {
		for (int i = 0; i < 9; i++) {
			var st = mc.player.getInventory().getStack(i);
			if (st.isOf(Items.GOLDEN_APPLE) || st.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
		}
		return -1;
	}

	private void stop() {
		if (!eating) return;
		mc.options.useKey.setPressed(false);
		if (prev != -1 && mc.player != null) mc.player.getInventory().setSelectedSlot(prev);
		prev = -1;
		eating = false;
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
		int slot = findApple();
		if (slot != -1 && (hp <= health.get() || (eating && hp <= health.get() + 4))) {
			if (!eating) { prev = mc.player.getInventory().getSelectedSlot(); eating = true; }
			mc.player.getInventory().setSelectedSlot(slot);
			mc.options.useKey.setPressed(true);
		} else {
			stop();
		}
	}

	@Override
	public void onDisable() {
		stop();
	}
}
