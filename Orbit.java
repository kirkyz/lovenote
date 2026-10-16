package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.ParticleUtil;

/** Particles that circle your player: halo above the head, a ring at the feet, or a rising helix. */
public class Orbit extends Module {
	private final ModeSetting style = add(new ModeSetting("Style", "Halo", "Halo", "Feet Ring", "Helix", "Double Ring"));
	private final ModeSetting particle = add(new ModeSetting("Particle", "End Rod", ParticleUtil.names()));
	private final NumberSetting radius = add(new NumberSetting("Radius", 0.7, 0.3, 2.0, 0.05));
	private final NumberSetting count = add(new NumberSetting("Points", 3, 1, 8, 1));
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.25, 0.05, 0.8, 0.05));
	private double angle;

	public Orbit() {
		super("Orbit", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		angle += speed.get();
		int n = count.getI();
		double r = radius.get();
		var effect = ParticleUtil.get(particle.get());
		double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
		double h = mc.player.getHeight();

		for (int i = 0; i < n; i++) {
			double a = angle + i * (Math.PI * 2.0 / n);
			double px = x + Math.cos(a) * r, pz = z + Math.sin(a) * r;
			switch (style.get()) {
				case "Halo" -> mc.world.addParticleClient(effect, px, y + h + 0.25, pz, 0, 0, 0);
				case "Feet Ring" -> mc.world.addParticleClient(effect, px, y + 0.05, pz, 0, 0.01, 0);
				case "Double Ring" -> {
					mc.world.addParticleClient(effect, px, y + h + 0.25, pz, 0, 0, 0);
					mc.world.addParticleClient(effect, x + Math.cos(-a) * r, y + 0.05, z + Math.sin(-a) * r, 0, 0.01, 0);
				}
				case "Helix" -> {
					double t = ((angle * 0.15) + i / (double) n) % 1.0;
					mc.world.addParticleClient(effect, px, y + t * h, pz, 0, 0, 0);
				}
			}
		}
	}
}
