package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerInteractionManager;

import java.lang.reflect.Field;

/** Extends how far you can hit and place. */
public class Reach extends Module {
	private final NumberSetting extra = add(new NumberSetting("Extra reach", 1.0, 0.0, 3.0, 0.1));

	private Field field;

	public Reach() {
		super("Reach", Category.COMBAT);
		describe("Extends your block and entity reach.");
	}

	@Override
	public void onTick() {
		if (mc.interactionManager == null) return;
		if (field == null) field = find();
		if (field == null) return;
		try {
			field.setFloat(mc.interactionManager, 5.0f + (float) extra.get());
		} catch (Exception ignored) {}
	}

	@Override
	public void onDisable() {
		if (field == null || mc.interactionManager == null) return;
		try {
			field.setFloat(mc.interactionManager, 5.0f);
		} catch (Exception ignored) {}
	}

	/** The only float on the interaction manager sitting at the vanilla 5.0. */
	private static Field find() {
		ClientPlayerInteractionManager m = net.minecraft.client.MinecraftClient.getInstance().interactionManager;
		if (m == null) return null;
		for (Field f : ClientPlayerInteractionManager.class.getDeclaredFields()) {
			if (f.getType() != float.class) continue;
			f.setAccessible(true);
			try {
				if (Math.abs(f.getFloat(m) - 5.0f) < 0.001f) return f;
			} catch (Exception ignored) {}
		}
		return null;
	}
}