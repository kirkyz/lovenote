package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Armour icons with durability. */
public class ArmorHud extends Module {
	private final BoolSetting offhand = add(new BoolSetting("Off hand", true));

	public ArmorHud() {
		super("ArmorHud", Category.HUD);
		describe("Armour and durability readout.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		int size = 18;
		int count = 4 + (offhand.get() ? 1 : 0);
		int w = count * size + 6;
		int x = ctx.getScaledWindowWidth() / 2 - w / 2;
		int y = ctx.getScaledWindowHeight() - 62;
		RenderUtil.rrect(ctx, x, y, w, size + 6, 0xB00C0C11);
		int cx = x + 3;
		for (int i = 3; i >= 0; i--) {
			ItemStack s = mc.player.getInventory().getStack(36 + i);
			if (!s.isEmpty()) {
				ctx.drawItem(s, cx, y + 3);
				ctx.drawStackOverlay(mc.textRenderer, s, cx, y + 3);
			}
			cx += size;
		}
		if (offhand.get()) {
			ItemStack s = mc.player.getOffHandStack();
			if (!s.isEmpty()) {
				ctx.drawItem(s, cx, y + 3);
				ctx.drawStackOverlay(mc.textRenderer, s, cx, y + 3);
			}
		}
	}
}