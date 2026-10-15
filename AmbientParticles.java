package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.ParticleUtil;

import java.util.Random;

/** Fills the air around you with snow / stars / glow / notes, Moon-style. */
public class AmbientParticles extends Module {
	private final ModeSetting particle = add(new ModeSetting("Particle", "Snow", ParticleUtil.names()));
	private final NumberSetting amount = add(new NumberSetting("Amount", 4, 1, 20, 1));
	private final NumberSetting interval = add(new NumberSetting("Interval (ticks)", 3, 1, 20, 1));
	private final NumberSetting height = add(new NumberSetting("Height", 7, 1, 16, 1));
	private final NumberSetting drift = add(new NumberSetting("Drift", 0.02, 0, 0.2, 0.01));
	private int ticks;
	private final NumberSetting area = add(new NumberSetting("Area", 12, 4, 24, 1));
	private final Random rnd = new Random();

	public AmbientParticles() {
		super("AmbientParticles", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (++ticks % interval.getI() != 0) return;
		var e = ParticleUtil.get(particle.get());
		double a = area.get();
		for (int i = 0; i < amount.getI(); i++) {
			double x = mc.player.getX() + (rnd.nextDouble() * 2 - 1) * a;
			double y = mc.player.getY() + rnd.nextDouble() * height.get() - 1;
			double z = mc.player.getZ() + (rnd.nextDouble() * 2 - 1) * a;
			mc.world.addParticle(e, x, y, z, (rnd.nextDouble() - 0.5) * drift.get(), -drift.get(), (rnd.nextDouble() - 0.5) * drift.get());
		}
	}
}
