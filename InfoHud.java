package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class InfoHud extends Module {
	public InfoHud() {
		super("Info", Category.HUD);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		double bps = Math.hypot(mc.player.getX() - mc.player.lastX, mc.player.getZ() - mc.player.lastZ) * 20.0;
		String[] lines = {
				String.format("xyz  %.0f  %.0f  %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()),
				String.format("speed  %.2f b/s", bps)
		};
		int w = 0;
		for (String l : lines) w = Math.max(w, mc.textRenderer.getWidth(l));
		w += 16;
		int h = lines.length * 11 + 8;
		int x = ctx.getScaledWindowWidth() - w - 8, y = ctx.getScaledWindowHeight() / 2 - h / 2;
		RenderUtil.rrect(ctx, x, y, w, h, 0xC0101015);
		ctx.fill(x + 3, y + 4, x + 5, y + h - 4, Colors.wave(0.2f));
		for (int i = 0; i < lines.length; i++) ctx.drawText(mc.textRenderer, lines[i], x + 10, y + 5 + i * 11, 0xFFD0D0DC, false);
	}
}
