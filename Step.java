package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

/** Steps up full blocks without jumping. */
public class Step extends Module {
	private final NumberSetting height = add(new NumberSetting("Height", 1.0, 0.5, 1.5, 0.1));
	private int timer;

	public Step() {
		super("Step", Category.MOVEMENT);
		describe("Steps up full blocks without jumping.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (timer > 0) { timer--; return; }
		if (!mc.player.horizontalCollision || !mc.player.isOnGround()) return;
		mc.player.setPos(mc.player.getX(), mc.player.getY() + height.get(), mc.player.getZ());
		mc.player.setOnGround(false);
		timer = 2;
	}
}