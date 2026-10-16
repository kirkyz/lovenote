package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;

public class AutoBuy extends Module {
	private final ModeSetting item = add(new ModeSetting("Item", "golden_carrot",
			"golden_carrot", "totem_of_undying", "golden_apple", "ender_pearl", "obsidian", "cobblestone"));
	private final NumberSetting amount = add(new NumberSetting("Amount", 64, 1, 64, 1));
	private final NumberSetting delay = add(new NumberSetting("Delay (s)", 10, 2, 120, 1));

	private int timer;

	@Override
	public void onEnable() { timer = 20; }

	public AutoBuy() {
		super("AutoBuy", Category.DONUT);
		describe("Buys a set item on a timer.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.getNetworkHandler() == null) return;
		if (timer > 0) { timer--; return; }
		mc.getNetworkHandler().sendChatCommand("buy " + item.get() + " " + amount.getI());
		timer = delay.getI() * 20;
	}
}
