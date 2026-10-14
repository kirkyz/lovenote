package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {
	public Fullbright() {
		super("Fullbright", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		StatusEffectInstance e = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
		if (e == null || e.getDuration() < 500) {
			mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
		}
	}

	@Override
	public void onDisable() {
		if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
	}
}
