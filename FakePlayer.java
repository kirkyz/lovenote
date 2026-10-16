package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;

import java.util.UUID;

/** Spawns a client-side dummy player in front of you - handy for testing Aura and effects. */
public class FakePlayer extends Module {
	private OtherClientPlayerEntity fake;

	public FakePlayer() {
		super("FakePlayer", Category.MISC);
	}

	@Override
	public void onEnable() {
		if (mc.world == null || mc.player == null) return;
		fake = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), "FakePlayer"));
		double rad = Math.toRadians(mc.player.getYaw());
		double x = mc.player.getX() - Math.sin(rad) * 3, z = mc.player.getZ() + Math.cos(rad) * 3;
		fake.refreshPositionAndAngles(x, mc.player.getY(), z, mc.player.getYaw() + 180f, 0f);
		fake.setHealth(20f);
		fake.setId(1_900_000);
mc.world.addEntity(fake);
	}

	@Override
	public void onDisable() {
		if (fake != null && mc.world != null) mc.world.removeEntity(fake.getId(), Entity.RemovalReason.DISCARDED);
		fake = null;
	}
}
