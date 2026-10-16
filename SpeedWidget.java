package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class SpeedWidget extends HudWidget {
	private float shown;

	public SpeedWidget() {
		super("Speed Display", 0.02f, 0.20f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null) return;
		double bps = Math.hypot(mc.player.getX() - mc.player.lastX, mc.player.getZ() - mc.player.lastZ) * 20.0;
		shown += ((float) Math.min(1.0, bps / 12.0) - shown) * 0.2f;
		int bw = 78, bh = 27;
		box(ctx, 0, 0, bw, bh);
		RenderUtil.rrect(ctx, 6, 5, 6, 6, Colors.wave(0f));
		ctx.drawTextWithShadow(mc.textRenderer, "Speed", 16, 4, 0xFFFFFFFF);
		ctx.drawText(mc.textRenderer, String.format("%.2f bps", bps), 6, 14, 0xFFB0B0C0, false);
		ctx.fill(6, bh - 5, bw - 6, bh - 3, 0xFF2A2A36);
		RenderUtil.gradientH(ctx, 6, bh - 5, 6 + (int) ((bw - 12) * shown), bh - 3, Colors.C1, Colors.skeet(shown));
		w = bw;
		h = bh;
	}
}
