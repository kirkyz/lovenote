package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.RotationUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class AimAssist extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 5.0, 3.0, 8.0, 0.1));
	private final NumberSetting fov = add(new NumberSetting("FOV", 90, 30, 360, 5));
	private final NumberSetting strength = add(new NumberSetting("Strength", 0.25, 0.05, 1.0, 0.05));
	private final BoolSetting vertical = add(new BoolSetting("Vertical", true));
	private final BoolSetting onlyClicking = add(new BoolSetting("Only when clicking", false));

	public AimAssist() {
		super("AimAssist", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
		if (onlyClicking.get() && !mc.options.attackKey.isPressed()) return;

		PlayerEntity best = null;
		float bestDiff = Float.MAX_VALUE;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
			if (mc.player.distanceTo(p) > range.get()) continue;
			float[] r = RotationUtil.to(mc.player, p);
			float dy = Math.abs(MathHelper.wrapDegrees(r[0] - mc.player.getYaw()));
			if (dy > fov.get() / 2.0) continue;
			if (dy < bestDiff) { bestDiff = dy; best = p; }
		}
		if (best == null) return;

		float[] r = RotationUtil.to(mc.player, best);
		float dyaw = MathHelper.wrapDegrees(r[0] - mc.player.getYaw());
		float dpitch = r[1] - mc.player.getPitch();
		float s = strength.getF();
		mc.player.setYaw(mc.player.getYaw() + dyaw * s);
		if (vertical.get()) mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + dpitch * s, -90f, 90f));
	}
}
