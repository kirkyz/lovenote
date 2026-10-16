package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;

public class AutoSell extends Module {
	private final ModeSetting command = add(new ModeSetting("Command", "/sell all", "/sell all", "/sell hand", "/sellall"));
	private final NumberSetting delay = add(new NumberSetting("Delay (s)", 5, 1, 60, 1));

	private int timer;

	@Override
	public void onEnable() { timer = 10; }

	public AutoSell() {
		super("AutoSell", Category.DONUT);
		describe("Runs your sell command on a timer.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.getNetworkHandler() == null) return;
		if (timer > 0) { timer--; return; }
		mc.getNetworkHandler().sendChatCommand(command.get().substring(1));
		timer = delay.getI() * 20;
	}
}
