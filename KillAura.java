package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 4.2, 3.0, 6.0, 0.1));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 0, 0, 10, 1));
	private final NumberSetting cooldown = add(new NumberSetting("Cooldown", 0.92, 0.0, 1.0, 0.01));
	private final ModeSetting priority = add(new ModeSetting("Priority", "Distance", "Distance", "Health"));
	private final ModeSetting rotate = add(new ModeSetting("Rotation", "Packet", "Packet", "None"));
	private final BoolSetting players = add(new BoolSetting("Players", true));
	private final BoolSetting mobs = add(new BoolSetting("Mobs", false));
	private final BoolSetting walls = add(new BoolSetting("Through walls", false));

	private int timer;

	public KillAura() {
		super("KillAura", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
		if (timer > 0) { timer--; return; }
		if (mc.player.getAttackCooldownProgress(0.5f) < cooldown.getF()) return;

		LivingEntity t = findTarget();
		if (t == null) return;

		if (rotate.is("Packet")) {
			float[] r = rotations(t);
			mc.player.networkHandler.sendPacket(
					new PlayerMoveC2SPacket.LookAndOnGround(r[0], r[1], mc.player.isOnGround(), false));
		}
		mc.interactionManager.attackEntity(mc.player, t);
		mc.player.swingHand(Hand.MAIN_HAND);
		timer = delay.getI();
	}

	private LivingEntity findTarget() {
		List<LivingEntity> list = new ArrayList<>();
		for (Entity e : mc.world.getEntities()) {
			if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
			boolean ok = (e instanceof PlayerEntity && players.get()) || (e instanceof MobEntity && mobs.get());
			if (!ok) continue;
			if (e instanceof PlayerEntity p && p.isSpectator()) continue;
			if (dist(le) > range.get()) continue;
			if (!walls.get() && !mc.player.canSee(le)) continue;
			list.add(le);
		}
		if (list.isEmpty()) return null;
		Comparator<LivingEntity> cmp = priority.is("Health")
				? Comparator.comparingDouble(LivingEntity::getHealth)
				: Comparator.comparingDouble(this::dist);
		list.sort(cmp);
		return list.get(0);
	}

	private double dist(Entity e) {
		Vec3d eye = mc.player.getEyePos();
		Box b = e.getBoundingBox();
		Vec3d p = new Vec3d(
				MathHelper.clamp(eye.x, b.minX, b.maxX),
				MathHelper.clamp(eye.y, b.minY, b.maxY),
				MathHelper.clamp(eye.z, b.minZ, b.maxZ));
		return eye.distanceTo(p);
	}

	private float[] rotations(Entity e) {
		Vec3d eye = mc.player.getEyePos();
		Vec3d to = e.getEntityPos().add(0, e.getHeight() * 0.6, 0);
		double dx = to.x - eye.x, dy = to.y - eye.y, dz = to.z - eye.z;
		double flat = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));
		return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f)};
	}
}
