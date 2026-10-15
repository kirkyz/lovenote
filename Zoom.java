package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Hold a key to zoom in. */
public class Zoom extends Module {
	private final ModeSetting key = add(new ModeSetting("Key", "C", "C", "V", "B", "Z", "Left Alt"));
	private final NumberSetting amount = add(new NumberSetting("FOV", 25, 10, 60, 1));

	private int prev = -1;

	public Zoom() {
		super("Zoom", Category.VISUALS);
		describe("Hold a key to zoom in.");
	}

	private int code() {
		return switch (key.get()) {
			case "V" -> GLFW.GLFW_KEY_V;
			case "B" -> GLFW.GLFW_KEY_B;
			case "Z" -> GLFW.GLFW_KEY_Z;
			case "Left Alt" -> GLFW.GLFW_KEY_LEFT_ALT;
			default -> GLFW.GLFW_KEY_C;
		};
	}

	@Override
	public void onTick() {
		if (mc.options == null) return;
		boolean down = InputUtil.isKeyPressed(mc.getWindow().getHandle(), code());
		if (down && prev < 0) {
			prev = mc.options.getFov().getValue();
			mc.options.getFov().setValue(amount.getI());
		} else if (!down && prev >= 0) {
			mc.options.getFov().setValue(prev);
			prev = -1;
		}
	}

	@Override
	public void onDisable() {
		if (prev >= 0 && mc.options != null) {
			mc.options.getFov().setValue(prev);
			prev = -1;
		}
	}
}