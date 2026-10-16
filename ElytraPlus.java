package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/** Auto-deploys the elytra and gives you a boost. */
public class ElytraPlus extends Module {
	private final NumberSetting boost = add(new NumberSetting("Boost", 0.06, 0.02, 0.20, 0.01));
	private final BoolSetting autoDeploy = add(new BoolSetting("Auto deploy", true));

	public ElytraPlus() {
		super("ElytraPlus", Category.MOVEMENT);
		describe("Elytra auto-deploy and rocket boost.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.options == null) return;
		if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return;

		if (autoDeploy.get() && !mc.player.isOnGround() && !mc.player.isGliding()
				&& !mc.player.isTouchingWater() && mc.options.jumpKey.isPressed()) {
			mc.player.startGliding();
			return;
		}
		if (mc.player.isGliding() && mc.options.jumpKey.isPressed()) {
			Vec3d look = mc.player.getRotationVec(1f);
			mc.player.setVelocity(mc.player.getVelocity().add(look.multiply(boost.get())));
		}
	}
}