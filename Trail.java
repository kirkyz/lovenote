package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.ParticleUtil;

import java.util.Random;

public class Trail extends Module {
	private final ModeSetting particle = add(new ModeSetting("Particle", "End Rod", ParticleUtil.names()));
	private final NumberSetting amount = add(new NumberSetting("Amount", 2, 1, 8, 1));
	private final Random rnd = new Random();

	public Trail() {
		super("Trail", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (mc.player.getVelocity().horizontalLengthSquared() < 0.0025) return;
		for (int i = 0; i < amount.getI(); i++) {
			mc.world.addParticleClient(ParticleUtil.get(particle.get()),
					mc.player.getX() + (rnd.nextDouble() - 0.5) * 0.4,
					mc.player.getY() + 0.05 + rnd.nextDouble() * 0.2,
					mc.player.getZ() + (rnd.nextDouble() - 0.5) * 0.4,
					0, 0.01, 0);
		}
	}
}
