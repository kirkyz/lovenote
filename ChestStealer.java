package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealer extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 1, 0, 10, 1));
	private final BoolSetting closeEmpty = add(new BoolSetting("Close when empty", true));

	private int timer;

	public ChestStealer() {
		super("ChestStealer", Category.DONUT);
		describe("Empties any chest you open.");
	}

	@Override
	public void onTick() {
		if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;
		if (mc.player == null || mc.interactionManager == null) return;
		if (timer > 0) { timer--; return; }

		GenericContainerScreenHandler h = screen.getScreenHandler();
		int slots = Math.min(h.getRows() * 9, h.slots.size());
		boolean any = false;
		for (int i = 0; i < slots; i++) {
			Slot slot = h.slots.get(i);
			if (!slot.hasStack()) continue;
			any = true;
			mc.interactionManager.clickSlot(h.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
			timer = delay.getI();
			return;
		}
		if (!any && closeEmpty.get()) mc.player.closeHandledScreen();
	}
}
