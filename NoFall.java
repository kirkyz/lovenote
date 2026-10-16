package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
	public NoFall() {
		super("NoFall", Category.MOVEMENT);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (mc.player.fallDistance > 2.5f && !mc.player.isOnGround()) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
		}
	}
}
