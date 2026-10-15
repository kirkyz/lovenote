package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

/** Middle click swaps a pearl or totem straight to your off hand. */
public class MiddleClick extends Module {
	private final ModeSetting item = add(new ModeSetting("Item", "Pearl", "Pearl", "Totem", "Both"));
	private final BoolSetting toOffhand = add(new BoolSetting("To off hand", true));

	private boolean held;

	public MiddleClick() {
		super("MiddleClick", Category.MISC);
		describe("Middle click sends a pearl or totem to your off hand.");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null) return;
		boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;
		if (!down) { held = false; return; }
		if (held) return;
		held = true;

		var inv = mc.player.getInventory();
		for (int i = 0; i < 36; i++) {
			ItemStack s = inv.getStack(i);
			if (!matches(s)) continue;
			int slot = i < 9 ? i + 36 : i;
			mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 40,
					SlotActionType.SWAP, mc.player);
			return;
		}
	}

	private boolean matches(ItemStack s) {
		if (s.isEmpty()) return false;
		String m = item.get();
		if ((m.equals("Pearl") || m.equals("Both")) && s.isOf(Items.ENDER_PEARL)) return true;
		return (m.equals("Totem") || m.equals("Both")) && s.isOf(Items.TOTEM_OF_UNDYING);
	}
}