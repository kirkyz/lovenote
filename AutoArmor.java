package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Swaps in the best armour you are carrying. */
public class AutoArmor extends Module {
	private static final Item[][] TIERS = {
			{Items.NETHERITE_HELMET, Items.DIAMOND_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, Items.LEATHER_HELMET},
			{Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.LEATHER_CHESTPLATE},
			{Items.NETHERITE_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.LEATHER_LEGGINGS},
			{Items.NETHERITE_BOOTS, Items.DIAMOND_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, Items.LEATHER_BOOTS}
	};

	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 3, 0, 20, 1));
	private int timer;

	public AutoArmor() {
		super("AutoArmor", Category.COMBAT);
		describe("Wears your best armour automatically.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null) return;
		if (mc.currentScreen instanceof HandledScreen<?> && !(mc.currentScreen instanceof InventoryScreen)) return;
		if (timer > 0) { timer--; return; }

		for (int type = 0; type < 4; type++) {
			int armorSlot = 5 + type;
			ItemStack worn = mc.player.playerScreenHandler.getSlot(armorSlot).getStack();
			int bestRank = rank(worn, type);
			int bestSlot = -1;
			for (int i = 9; i <= 44; i++) {
				int r = rank(mc.player.playerScreenHandler.getSlot(i).getStack(), type);
				if (r > bestRank) { bestRank = r; bestSlot = i; }
			}
			if (bestSlot < 0) continue;
			click(bestSlot);
			click(armorSlot);
			click(bestSlot);
			timer = delay.getI();
			return;
		}
	}

	private void click(int slot) {
		mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
	}

	private int rank(ItemStack s, int type) {
		if (s.isEmpty()) return 0;
		Item[] t = TIERS[type];
		for (int i = 0; i < t.length; i++) if (s.isOf(t[i])) return t.length - i;
		return 0;
	}
}