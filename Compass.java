package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/** Horizontal compass strip at the top of the screen. */
public class Compass extends Module {
	private static final String[] DIRS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

	private final NumberSetting width = add(new NumberSetting("Width", 200, 80, 500, 10));

	public Compass() {
		super("Compass", Category.HUD);
		describe("Horizontal compass strip.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		int w = width.getI();
		int h = 14;
		int x = ctx.getScaledWindowWidth() / 2 - w / 2;
		int y = 6;
		RenderUtil.rrect(ctx, x, y, w, h, 0x90000000);
		ctx.fill(x + w / 2, y, x + w / 2 + 1, y + h, Colors.wave(0f));

		float yaw = ((mc.player.getYaw() % 360) + 360) % 360;
		for (int deg = 0; deg < 360; deg += 15) {
			float diff = deg - yaw;
			while (diff < -180) diff += 360;
			while (diff > 180) diff -= 360;
			if (Math.abs(diff) > 90) continue;
			int px = x + w / 2 + (int) (diff / 90f * (w / 2f));
			boolean major = deg % 45 == 0;
			ctx.fill(px, y + (major ? 2 : 5), px + 1, y + h - 3, major ? 0xFFFFFFFF : 0x80FFFFFF);
			if (major) ctx.drawCenteredTextWithShadow(mc.textRenderer, DIRS[(deg / 45) % 8], px, y + 15, Colors.text());
		}
	}
}
