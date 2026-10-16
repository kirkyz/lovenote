package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.MultiSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Aura extends Module {
	private final ModeSetting rotation = add(new ModeSetting("Rotation", "Packet", "Packet", "Smooth", "None"));
	private final ModeSetting auraMode = add(new ModeSetting("Aura Mode", "Blatant", "Blatant", "Normal"));
	private final ModeSetting version = add(new ModeSetting("Combat Version", "1.9+", "1.9+", "1.8"));
	private final NumberSetting attackRange = add(new NumberSetting("Attack Range", 3.0, 2.0, 6.0, 0.1));
	private final NumberSetting aimRange = add(new NumberSetting("Aim Range", 5.0, 2.0, 9.0, 0.1));
	private final NumberSetting fov = add(new NumberSetting("FOV", 360, 30, 360, 5));
	private final NumberSetting cps = add(new NumberSetting("CPS (1.8)", 10, 1, 20, 1));
	private final ModeSetting priority = add(new ModeSetting("Priority", "Distance", "Distance", "Health", "Angle"));
	private final MultiSetting targets = add(new MultiSetting("Targets",
			new String[]{"Players", "Mobs", "Animals", "Invisibles", "Naked Players"}, "Players", "Invisibles", "Naked Players"));
	private final BoolSetting walls = add(new BoolSetting("Through walls", false));

	private final Random rnd = new Random();
	private int timer;

	public Aura() {
		super("Aura", Category.COMBAT);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

		LivingEntity t = findTarget();
		if (t == null) return;

		float[] r = RotationUtil.to(mc.player, t);
		if (rotation.is("Smooth")) {
			float dy = MathHelper.wrapDegrees(r[0] - mc.player.getYaw());
			float dp = r[1] - mc.player.getPitch();
			mc.player.setYaw(mc.player.getYaw() + dy * 0.4f);
			mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + dp * 0.4f, -90f, 90f));
		}

		if (dist(t) > attackRange.get()) return;
		if (timer > 0) { timer--; return; }

		boolean normal = auraMode.is("Normal");
		boolean modern = version.is("1.9+");
		if (modern && mc.player.getAttackCooldownProgress(0.5f) < (normal ? 0.95f : 0.85f)) return;

		if (rotation.is("Packet")) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(r[0], r[1], mc.player.isOnGround(), false));
		}
		mc.interactionManager.attackEntity(mc.player, t);
		mc.player.swingHand(Hand.MAIN_HAND);

		if (modern) {
			timer = normal ? rnd.nextInt(2) : 0;
		} else {
			double base = 20.0 / cps.get();
			double jitter = normal ? 0.8 + rnd.nextDouble() * 0.4 : 1.0;
			timer = Math.max(0, (int) Math.round(base * jitter) - 1);
		}
	}

	private boolean valid(Entity e) {
		if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) return false;
		if (e instanceof PlayerEntity p) {
			if (!targets.is("Players") || p.isSpectator()) return false;
			if (AntiBot.isBot(p)) return false;
			if (!targets.is("Naked Players") && p.getArmor() == 0) return false;
		} else if (e instanceof AnimalEntity) {
			if (!targets.is("Animals")) return false;
		} else if (e instanceof MobEntity) {
			if (!targets.is("Mobs")) return false;
		} else {
			return false;
		}
		return !(le.isInvisible() && !targets.is("Invisibles"));
	}

	private LivingEntity findTarget() {
		List<LivingEntity> list = new ArrayList<>();
		for (Entity e : mc.world.getEntities()) {
			if (!valid(e)) continue;
			LivingEntity le = (LivingEntity) e;
			if (dist(le) > aimRange.get()) continue;
			float[] r = RotationUtil.to(mc.player, le);
			if (Math.abs(MathHelper.wrapDegrees(r[0] - mc.player.getYaw())) > fov.get() / 2.0) continue;
			if (!walls.get() && !mc.player.canSee(le)) continue;
			list.add(le);
		}
		if (list.isEmpty()) return null;
		Comparator<LivingEntity> cmp = switch (priority.get()) {
			case "Health" -> Comparator.comparingDouble(LivingEntity::getHealth);
			case "Angle" -> Comparator.comparingDouble(en -> Math.abs(MathHelper.wrapDegrees(RotationUtil.to(mc.player, en)[0] - mc.player.getYaw())));
			default -> Comparator.comparingDouble(this::dist);
		};
		list.sort(cmp);
		return list.get(0);
	}

	private double dist(Entity e) {
		Vec3d eye = mc.player.getEyePos();
		Box b = e.getBoundingBox();
		Vec3d p = new Vec3d(MathHelper.clamp(eye.x, b.minX, b.maxX), MathHelper.clamp(eye.y, b.minY, b.maxY), MathHelper.clamp(eye.z, b.minZ, b.maxZ));
		return eye.distanceTo(p);
	}
}
