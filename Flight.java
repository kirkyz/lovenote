package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

public class Flight extends Module {
	private final NumberSetting hspeed = add(new NumberSetting("Horizontal", 0.6, 0.1, 3.0, 0.05));
	private final NumberSetting vspeed = add(new NumberSetting("Vertical", 0.4, 0.1, 2.0, 0.05));

	public Flight() {
		super("Flight", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		mc.player.fallDistance = 0;
		double y = 0;
		if (mc.options.jumpKey.isPressed()) y = vspeed.get();
		else if (mc.options.sneakKey.isPressed()) y = -vspeed.get();

		double f = mc.player.input.getMovementInput().y, s = mc.player.input.getMovementInput().x;
		double vx = 0, vz = 0;
		if (f != 0 || s != 0) {
			double len = Math.sqrt(f * f + s * s);
			f /= len;
			s /= len;
			double rad = Math.toRadians(mc.player.getYaw());
			double sin = Math.sin(rad), cos = Math.cos(rad);
			vx = (-sin * f + cos * s) * hspeed.get();
			vz = (cos * f + sin * s) * hspeed.get();
		}
		mc.player.setVelocity(vx, y, vz);
	}
}
