package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributes;

/** Undoes the slowdown from eating, blocking and drawing a bow. */
public class NoSlow extends Module {
	private final NumberSetting factor = add(new NumberSetting("Factor", 5.0, 1.0, 6.0, 0.1));

	public NoSlow() {
		super("NoSlow", Category.MOVEMENT);
		describe("No slowdown while eating or blocking.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		speed(mc.player.isUsingItem() ? (float) factor.get() : 1f);
	}

	@Override
	public void onDisable() {
		speed(1f);
	}

	private void speed(float mult) {
		if (mc.player == null) return;
		mc.player.getAbilities().setWalkSpeed(0.1f * mult);
		var inst = mc.player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
		if (inst != null) inst.setBaseValue(0.1 * mult);
	}
}