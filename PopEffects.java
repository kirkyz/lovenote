package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** Particle burst + log line + pop counter whenever someone pops a totem. */
public class PopEffects extends Module {
	private final NumberSetting amount = add(new NumberSetting("Particles", 35, 10, 100, 5));
	private final BoolSetting self = add(new BoolSetting("Include self", false));
	private final BoolSetting log = add(new BoolSetting("Log pops", true));

	private final Map<UUID, Integer> pops = new HashMap<>();
	private final Random rnd = new Random();

	public PopEffects() {
		super("PopEffects", Category.VISUALS);
	}

	@Override
	public void onTotemPop(Entity e) {
		if (mc.world == null) return;
		if (e == mc.player && !self.get()) return;
		for (int i = 0; i < amount.getI(); i++) {
			mc.world.addParticleClient(ParticleTypes.TOTEM_OF_UNDYING,
					e.getX(), e.getY() + e.getHeight() / 2.0, e.getZ(),
					(rnd.nextDouble() - 0.5) * 0.9, rnd.nextDouble() * 0.6, (rnd.nextDouble() - 0.5) * 0.9);
		}
		int n = pops.merge(e.getUuid(), 1, Integer::sum);
		if (log.get()) HitLog.push(e.getName().getString() + " popped a totem (" + n + ")");
	}

	@Override
	public void onDisable() {
		pops.clear();
	}
}
