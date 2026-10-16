package com.lovenote.module.impl;
import net.minecraft.util.PlayerInput;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;

/** Circles whoever is closest so you stay out of their reach. */
public class TargetStrafe extends Module {
	private final NumberSetting radius = add(new NumberSetting("Radius", 2.5, 1.0, 6.0, 0.1));
	private final ModeSetting dir = add(new ModeSetting("Direction", "Switch", "Left", "Right", "Switch"));
	private final BoolSetting jump = add(new BoolSetting("Jump", true));

	private boolean left = true;
	private int timer;

	public TargetStrafe() {
		super("TargetStrafe", Category.COMBAT);
		describe("Strafes around whoever is closest.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.player.input == null) return;
		PlayerEntity t = null;
		double best = 64.0;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive()) continue;
			double d = mc.player.distanceTo(p);
			if (d < best) { best = d; t = p; }
		}
		if (t == null) return;
		if (timer <= 0) { left = !left; timer = 20; }
		timer--;

		float side = dir.is("Left") ? 1f : dir.is("Right") ? -1f : (left ? 1f : -1f);
mc.player.input.playerInput = new PlayerInput(best > radius.get() + 0.4, false, side > 0f, side < 0f, mc.player.input.playerInput.jump(), mc.player.input.playerInput.sneak(), mc.player.input.playerInput.sprint());
		if (jump.get() && best < radius.get() + 0.6 && mc.player.isOnGround()) mc.player.jump();
	}

	@Override
	public void onDisable() {
		if (mc.player != null && mc.player.input != null) {
mc.player.input.playerInput = new PlayerInput(false, false, false, false, mc.player.input.playerInput.jump(), mc.player.input.playerInput.sneak(), mc.player.input.playerInput.sprint());
		}
	}
}