package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** Resets your sprint when you hit something so you keep dealing full knockback. */
public class WTap extends Module {
	public WTap() {
		super("WTap", Category.COMBAT);
	}

	@Override
	public void onAttack(Entity t) {
		if (mc.player != null && t instanceof LivingEntity) mc.player.setSprinting(false);
	}
}
