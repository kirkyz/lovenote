package com.lovenote.util;
import net.minecraft.particle.DragonBreathParticleEffect;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ParticleUtil {
	private static final Map<String, ParticleEffect> MAP = new LinkedHashMap<>();

	static {
		MAP.put("End Rod", ParticleTypes.END_ROD);
		MAP.put("Flame", ParticleTypes.FLAME);
		MAP.put("Soul Flame", ParticleTypes.SOUL_FIRE_FLAME);
		MAP.put("Crit", ParticleTypes.CRIT);
		MAP.put("Enchant", ParticleTypes.ENCHANT);
		MAP.put("Heart", ParticleTypes.HEART);
		MAP.put("Totem", ParticleTypes.TOTEM_OF_UNDYING);
		MAP.put("Portal", ParticleTypes.PORTAL);
		MAP.put("Dragon", DragonBreathParticleEffect.of(ParticleTypes.DRAGON_BREATH, 1.0f));
		MAP.put("Firework", ParticleTypes.FIREWORK);
		MAP.put("Snow", ParticleTypes.SNOWFLAKE);
		MAP.put("Soul", ParticleTypes.SOUL);
		MAP.put("Glow", ParticleTypes.GLOW);
		MAP.put("Note", ParticleTypes.NOTE);
	}

	private ParticleUtil() {}

	public static String[] names() {
		return MAP.keySet().toArray(new String[0]);
	}

	public static ParticleEffect get(String name) {
		return MAP.getOrDefault(name, ParticleTypes.END_ROD);
	}
}
