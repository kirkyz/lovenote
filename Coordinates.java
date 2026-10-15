package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Coordinates extends HudWidget {
	public Coordinates() {
		super("Coordinates", 0.20f, 0.39f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null) return;
		TextRenderer tr = mc.textRenderer;
		String[] ax = {"x", "y", "z"};
		String[] val = {String.valueOf((int) Math.floor(mc.player.getX())), String.valueOf((int) Math.floor(mc.player.getY())), String.valueOf((int) Math.floor(mc.player.getZ()))};
		int total = 22;
		for (int i = 0; i < 3; i++) total += tr.getWidth(ax[i]) + 3 + tr.getWidth(val[i]) + 7;
		box(ctx, 0, 0, total, 17);
		RenderUtil.rrect(ctx, 7, 5, 7, 7, Colors.wave(0f));
		int x = 20;
		for (int i = 0; i < 3; i++) {
			ctx.drawTextWithShadow(tr, ax[i], x, 5, Colors.wave(i * 0.15f));
			x += tr.getWidth(ax[i]) + 3;
			ctx.drawTextWithShadow(tr, val[i], x, 5, 0xFFFFFFFF);
			x += tr.getWidth(val[i]) + 7;
		}
		w = total;
		h = 17;
	}
}
