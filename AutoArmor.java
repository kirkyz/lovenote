package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 3, 0, 20, 1));
	private int timer;

	public AutoArmor() {
		super("AutoArmor", Category.COMBAT);
	}

	private static double score(ItemStack st, EquipmentSlot type) {
		if (!(st.getItem() instanceof ArmorItem a) || a.getSlotType() != type) return -1;
		return a.getProtection() + a.getToughness() * 0.5 + EnchantmentHelper.getLevel(Enchantments.PROTECTION, st) * 0.4;
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null) return;
		if (mc.currentScreen instanceof HandledScreen<?> && !(mc.currentScreen instanceof InventoryScreen)) return;
		if (timer > 0) { timer--; return; }

		PlayerScreenHandler h = mc.player.playerScreenHandler;
		EquipmentSlot[] types = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < 4; i++) {
			int armorSlot = 5 + i;
			ItemStack cur = h.getSlot(armorSlot).getStack();
			if (!cur.isEmpty() && !(cur.getItem() instanceof ArmorItem)) continue; // keep elytra etc.
			double best = score(cur, types[i]);
			int from = -1;
			for (int s = 9; s <= 44; s++) {
				double sc = score(h.getSlot(s).getStack(), types[i]);
				if (sc > best) { best = sc; from = s; }
			}
			if (from == -1) continue;
			mc.interactionManager.clickSlot(h.syncId, from, 0, SlotActionType.PICKUP, mc.player);
			mc.interactionManager.clickSlot(h.syncId, armorSlot, 0, SlotActionType.PICKUP, mc.player);
			if (!h.getCursorStack().isEmpty()) mc.interactionManager.clickSlot(h.syncId, from, 0, SlotActionType.PICKUP, mc.player);
			timer = delay.getI();
			return;
		}
	}
}
