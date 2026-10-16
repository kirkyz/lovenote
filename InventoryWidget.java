package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class InventoryWidget extends HudWidget {
	public InventoryWidget() {
		super("Inventory", 0.62f, 0.73f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null) return;
		int bw = 9 * 18 + 12, bh = 3 * 18 + 26;
		box(ctx, 0, 0, bw, bh);
		ctx.drawTextWithShadow(mc.textRenderer, "Inventory", 7, 6, 0xFFFFFFFF);
		RenderUtil.rrect(ctx, bw - 16, 6, 8, 8, Colors.wave(0f));
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 9; c++) {
				ItemStack st = mc.player.getInventory().getStack(9 + r * 9 + c);
				int x = 6 + c * 18, y = 20 + r * 18;
				ctx.fill(x, y, x + 17, y + 17, 0x40000000);
				if (!st.isEmpty()) {
					ctx.drawItem(st, x + 1, y + 1);
					ctx.drawStackOverlay(mc.textRenderer, st, x + 1, y + 1);
				}
			}
		}
		w = bw;
		h = bh;
	}
}
