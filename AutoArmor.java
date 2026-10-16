package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 3, 0, 20, 1));
	private int timer;

	public AutoArmor() {
		super("AutoArmor", Category.COMBAT);
	}

	/** Armour is now an EQUIPPABLE component, not an ArmorItem class. */
	private static boolean isArmorFor(ItemStack st, EquipmentSlot type) {
		EquippableComponent eq = st.get(DataComponentTypes.EQUIPPABLE);
		return eq != null && eq.slot() == type;
	}

	private static double defense(ItemStack st) {
		AttributeModifiersComponent mods = st.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
		if (mods == null) return 0.0;
		double total = 0.0;
		for (AttributeModifiersComponent.Entry e : mods.modifiers()) {
			if (e.attribute().getKey().map(k -> "armor".equals(k.getValue().getPath())).orElse(false)) {
				total += e.modifier().value();
			}
		}
		return total;
	}

	private static double score(ItemStack st, EquipmentSlot type) {
		if (!isArmorFor(st, type)) return -1;
		return defense(st);
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
			if (!cur.isEmpty() && defense(cur) <= 0.0) continue;
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
