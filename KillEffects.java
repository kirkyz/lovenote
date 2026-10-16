package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/** Plays a cosmetic (client-side only) effect when the player you were hitting dies. */
public class KillEffects extends Module {
	private final ModeSetting mode = add(new ModeSetting("Effect", "Lightning", "Lightning", "Totem", "Blood", "Firework", "Soul"));
	private final NumberSetting amount = add(new NumberSetting("Particles", 40, 10, 120, 5));

	private final Random rnd = new Random();
	private LivingEntity target;
	private int ticks;
	private int idCounter;

	public KillEffects() {
		super("KillEffects", Category.VISUALS);
	}

	@Override
	public void onAttack(Entity t) {
		if (t instanceof LivingEntity le) {
			target = le;
			ticks = 0;
		}
	}

	@Override
	public void onTick() {
		if (target == null || mc.world == null) return;
		if (!target.isAlive() || target.getHealth() <= 0f || target.isRemoved()) {
			trigger(target.getEntityPos());
			target = null;
			return;
		}
		if (++ticks > 200) target = null;
	}

	private void trigger(Vec3d p) {
		switch (mode.get()) {
			case "Lightning" -> {
				LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
				if (bolt != null) {
					bolt.setPosition(p.x, p.y, p.z);
					bolt.setCosmetic(true);
					bolt.setId(2_000_000 + (idCounter++ % 1000));
mc.world.addEntity(bolt);
				}
			}
			case "Totem" -> burst(ParticleTypes.TOTEM_OF_UNDYING, p, 0.6);
			case "Blood" -> burst(DustParticleEffect.DEFAULT, p, 0.25);
			case "Firework" -> burst(ParticleTypes.FIREWORK, p, 0.4);
			case "Soul" -> burst(ParticleTypes.SOUL, p, 0.15);
		}
	}

	private void burst(ParticleEffect e, Vec3d p, double s) {
		for (int i = 0; i < amount.getI(); i++) {
			mc.world.addParticleClient(e, p.x, p.y + 1.0, p.z,
					(rnd.nextDouble() - 0.5) * s * 2, rnd.nextDouble() * s, (rnd.nextDouble() - 0.5) * s * 2);
		}
	}
}
