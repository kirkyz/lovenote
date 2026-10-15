package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Random;

/** Hold left click on a target to attack at a steady (jittered) CPS. */
public class AutoClicker extends Module {
	private final NumberSetting cps = add(new NumberSetting("CPS", 12, 4, 20, 1));
	private final Random rnd = new Random();
	private int timer;

	public AutoClicker() {
		super("AutoClicker", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
		if (timer > 0) { timer--; return; }
		if (!mc.options.attackKey.isPressed()) return;
		if (mc.crosshairTarget instanceof EntityHitResult er && er.getEntity() instanceof LivingEntity) {
			mc.interactionManager.attackEntity(mc.player, er.getEntity());
			mc.player.swingHand(Hand.MAIN_HAND);
			timer = Math.max(0, (int) Math.round(20.0 / cps.get() * (0.85 + rnd.nextDouble() * 0.3)) - 1);
		}
	}
}
