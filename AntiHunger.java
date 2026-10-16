package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {
	public AntiHunger() {
		super("AntiHunger", Category.PLAYER);
		describe("Sends onGround=false while moving to slow hunger.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.getNetworkHandler() == null) return;
		if (!mc.player.isSprinting()) return;
		if (mc.player.getVelocity().horizontalLengthSquared() < 0.003) return;
		mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
				mc.player.getX(), mc.player.getY(), mc.player.getZ(),
				mc.player.getYaw(), mc.player.getPitch(), false, false));
	}
}
