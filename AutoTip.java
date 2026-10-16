package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;

public class AutoTip extends Module {
	private final ModeSetting command = add(new ModeSetting("Command", "/tip all", "/tip all", "/tip"));
	private final NumberSetting minutes = add(new NumberSetting("Every (min)", 10, 1, 60, 1));

	private int timer;

	@Override
	public void onEnable() { timer = 20; }

	public AutoTip() {
		super("AutoTip", Category.DONUT);
		describe("Tips everyone on a timer for free coins.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.getNetworkHandler() == null) return;
		if (timer > 0) { timer--; return; }
		mc.getNetworkHandler().sendChatCommand(command.get().substring(1));
		timer = minutes.getI() * 1200;
	}
}
