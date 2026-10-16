package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.entity.LivingEntity;

import java.lang.reflect.Field;

public class NoJumpDelay extends Module {
	private Field cooldown;

	public NoJumpDelay() {
		super("NoJumpDelay", Category.MOVEMENT);
		describe("Removes the delay between jumps.");
	}

	private Field findField() {
		if (cooldown != null) return cooldown;
		String mapped = null;
		try {
			MappingResolver r = FabricLoader.getInstance().getMappingResolver();
			mapped = r.mapFieldName(r.getCurrentRuntimeNamespace(),
					"net.minecraft.entity.LivingEntity", "jumpingCooldown", "I");
		} catch (Throwable ignored) {
		}
		for (Class<?> c = LivingEntity.class; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field f : c.getDeclaredFields()) {
				if (f.getType() != int.class) continue;
				if (f.getName().equals("jumpingCooldown") || (mapped != null && f.getName().equals(mapped))) {
					f.setAccessible(true);
					cooldown = f;
					return cooldown;
				}
			}
		}
		return null;
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		Field f = findField();
		if (f == null) return;
		try {
			if (f.getInt(mc.player) > 0) f.setInt(mc.player, 0);
		} catch (Throwable ignored) {
		}
	}
}
