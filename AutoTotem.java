package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 0, 0, 10, 1));
	private final NumberSetting health = add(new NumberSetting("Health", 20, 1, 20, 1));
	private int timer;

	public AutoTotem() {
		super("AutoTotem", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null) return;
		if (mc.currentScreen instanceof HandledScreen<?> && !(mc.currentScreen instanceof InventoryScreen)) return;
		if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
		if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.get()) return;
		if (timer > 0) { timer--; return; }

		PlayerScreenHandler h = mc.player.playerScreenHandler;
		for (int i = 9; i <= 44; i++) {
			if (h.getSlot(i).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
				mc.interactionManager.clickSlot(h.syncId, i, 40, SlotActionType.SWAP, mc.player);
				timer = delay.getI();
				return;
			}
		}
	}
}
