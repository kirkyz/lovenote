package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.ParticleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Random;

public class HitParticles extends Module {
	private final ModeSetting particle = add(new ModeSetting("Particle", "Crit", ParticleUtil.names()));
	private final NumberSetting amount = add(new NumberSetting("Amount", 20, 5, 80, 1));
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.4, 0.1, 1.5, 0.05));
	private final Random rnd = new Random();

	public HitParticles() {
		super("HitParticles", Category.VISUALS);
	}

	@Override
	public void onAttack(Entity target) {
		if (mc.world == null || !(target instanceof LivingEntity)) return;
		double s = speed.get();
		for (int i = 0; i < amount.getI(); i++) {
			mc.world.addParticleClient(ParticleUtil.get(particle.get()),
					target.getX() + (rnd.nextDouble() - 0.5) * target.getWidth(),
					target.getY() + rnd.nextDouble() * target.getHeight(),
					target.getZ() + (rnd.nextDouble() - 0.5) * target.getWidth(),
					(rnd.nextDouble() - 0.5) * s, (rnd.nextDouble() - 0.3) * s, (rnd.nextDouble() - 0.5) * s);
		}
	}
}
