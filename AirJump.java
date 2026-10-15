package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

public class AirJump extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 6, 2, 20, 1));
	private int timer;

	public AirJump() {
		super("AirJump", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.currentScreen != null) return;
		if (timer > 0) { timer--; return; }
		if (mc.options.jumpKey.isPressed() && !mc.player.isOnGround()) {
			mc.player.jump();
			timer = delay.getI();
		}
	}
}
