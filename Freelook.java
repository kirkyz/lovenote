package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Hold a key to look around, then snap back. */
public class Freelook extends Module {
	private final ModeSetting key = add(new ModeSetting("Key", "Left Alt", "Left Alt", "Right Alt", "C", "V"));

	private float yaw, pitch;
	private boolean active;

	public Freelook() {
		super("Freelook", Category.VISUALS);
		describe("Hold a key to look around freely.");
	}

	private int code() {
		return switch (key.get()) {
			case "Right Alt" -> GLFW.GLFW_KEY_RIGHT_ALT;
			case "C" -> GLFW.GLFW_KEY_C;
			case "V" -> GLFW.GLFW_KEY_V;
			default -> GLFW.GLFW_KEY_LEFT_ALT;
		};
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		boolean down = InputUtil.isKeyPressed(mc.getWindow(), code());
		if (down && !active) {
			yaw = mc.player.getYaw();
			pitch = mc.player.getPitch();
			active = true;
		} else if (!down && active) {
			mc.player.setYaw(yaw);
			mc.player.setPitch(pitch);
			active = false;
		}
	}

	@Override
	public void onDisable() {
		if (active && mc.player != null) {
			mc.player.setYaw(yaw);
			mc.player.setPitch(pitch);
			active = false;
		}
	}
}