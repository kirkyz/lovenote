package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Attacks automatically when your crosshair is on a target. */
public class Triggerbot extends Module {
	private final NumberSetting cooldown = add(new NumberSetting("Cooldown", 0.92, 0.0, 1.0, 0.01));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 0, 0, 10, 1));
	private final BoolSetting playersOnly = add(new BoolSetting("Players only", true));
	private int timer;

	public Triggerbot() {
		super("Triggerbot", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
		if (timer > 0) { timer--; return; }
		HitResult h = mc.crosshairTarget;
		if (!(h instanceof EntityHitResult er)) return;
		Entity e = er.getEntity();
		if (!(e instanceof LivingEntity le) || !le.isAlive()) return;
		if (playersOnly.get() && !(e instanceof PlayerEntity)) return;
		if (mc.player.getAttackCooldownProgress(0.5f) < cooldown.getF()) return;
		mc.interactionManager.attackEntity(mc.player, e);
		mc.player.swingHand(Hand.MAIN_HAND);
		timer = delay.getI();
	}
}
