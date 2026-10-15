package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/** Clicks per second counter. */
public class CpsHud extends Module {
	private final boolean[] left = new boolean[100];
	private final boolean[] right = new boolean[100];
	private int tick;
	private boolean lastL, lastR;

	public CpsHud() {
		super("CpsHud", Category.HUD);
		describe("Clicks per second counter.");
	}

	@Override
	public void onTick() {
		if (mc.options == null) return;
		boolean l = mc.options.attackKey.isPressed();
		boolean r = mc.options.useKey.isPressed();
		left[tick] = l && !lastL;
		right[tick] = r && !lastR;
		lastL = l;
		lastR = r;
		tick = (tick + 1) % 100;
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		int l = 0, r = 0;
		for (int i = 0; i < 20; i++) {
			int idx = (tick - 1 - i + 100) % 100;
			if (left[idx]) l++;
			if (right[idx]) r++;
		}
		String text = l + "  |  " + r + " cps";
		int w = mc.textRenderer.getWidth(text) + 12;
		int x = ctx.getScaledWindowWidth() / 2 - w / 2;
		int y = ctx.getScaledWindowHeight() - 40;
		RenderUtil.rrect(ctx, x, y, w, 13, 0xB00C0C11);
		ctx.drawText(mc.textRenderer, text, x + 6, y + 3, Colors.wave(0f), false);
	}
}