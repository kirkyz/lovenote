package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import net.minecraft.util.Hand;

/** 1.8 / 1.7 style combat feel: swing on every click, no cooldown gate. */
public class OldCombat extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "1.8", "1.8", "1.7"));
	private final BoolSetting swingEveryClick = add(new BoolSetting("Swing every click", true));
	private final BoolSetting swingWhileCooling = add(new BoolSetting("Swing while cooling", true));

	public OldCombat() {
		super("OldCombat", Category.COMBAT);
		describe("1.8 style combat feel with the cooldown gate removed.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
		if (!swingEveryClick.get() || !swingWhileCooling.get()) return;
		if (mc.options.attackKey.isPressed() && mc.player.getAttackCooldownProgress(0f) < 1f) {
			mc.player.swingHand(Hand.MAIN_HAND);
		}
	}
}