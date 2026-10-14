package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Hand;

/** Breaks end crystals that are in range. */
public class CrystalBreak extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 4.5, 1.0, 6.0, 0.1));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 1, 0, 6, 1));
	private int timer;

	public CrystalBreak() {
		super("CrystalBreak", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
		if (timer > 0) { timer--; return; }
		for (Entity e : mc.world.getEntities()) {
			if (e instanceof EndCrystalEntity && mc.player.distanceTo(e) <= range.get()) {
				mc.interactionManager.attackEntity(mc.player, e);
				mc.player.swingHand(Hand.MAIN_HAND);
				timer = delay.getI();
				return;
			}
		}
	}
}
