package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.util.Hand;

/** Keeps you moving so servers don't kick you. */
public class AntiAFK extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "All", "Rotate", "Jump", "Swing", "All"));
	private final NumberSetting interval = add(new NumberSetting("Interval (ticks)", 40, 10, 200, 5));

	private int timer;

	public AntiAFK() {
		super("AntiAFK", Category.MISC);
		describe("Moves so you are never idle.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (timer > 0) { timer--; return; }
		timer = interval.getI();
		String m = mode.get();
		if (m.equals("Rotate") || m.equals("All")) mc.player.setYaw(mc.player.getYaw() + 35f);
		if ((m.equals("Jump") || m.equals("All")) && mc.player.isOnGround()) mc.player.jump();
		if (m.equals("Swing") || m.equals("All")) mc.player.swingHand(Hand.MAIN_HAND);
	}
}