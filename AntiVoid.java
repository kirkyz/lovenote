package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Snaps you back up when you fall past the void line. */
public class AntiVoid extends Module {
	private final NumberSetting voidY = add(new NumberSetting("Void Y", -64, -128, 0, 1));
	private final NumberSetting pullTo = add(new NumberSetting("Pull to Y", 100, 20, 256, 5));
	private final BoolSetting onlyFalling = add(new BoolSetting("Only while falling", true));
	private final NumberSetting cooldown = add(new NumberSetting("Cooldown (ticks)", 20, 5, 100, 5));

	private int cd;

	public AntiVoid() {
		super("AntiVoid", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null) return;
		if (cd > 0) { cd--; return; }
		if (mc.player.getY() > voidY.get()) return;
		if (onlyFalling.get() && mc.player.getVelocity().y >= 0) return;

		double x = mc.player.getX(), y = pullTo.get(), z = mc.player.getZ();
		mc.player.setPosition(x, y, z);
		mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
		if (mc.getNetworkHandler() != null) {
			mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
		}
		cd = cooldown.getI();
	}
}