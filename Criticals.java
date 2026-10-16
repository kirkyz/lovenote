package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Packet", "Packet", "Jump"));

	public Criticals() {
		super("Criticals", Category.COMBAT);
	}

	@Override
	public void onAttack(Entity target) {
		if (mc.player == null || !(target instanceof LivingEntity)) return;
		if (!mc.player.isOnGround() || mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.hasVehicle()) return;
		double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
		if (mode.is("Packet")) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, false));
		} else {
			mc.player.jump();
		}
	}
}
