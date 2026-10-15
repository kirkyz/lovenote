package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

/** Top-down radar of nearby players. */
public class Radar extends HudWidget {
	private final NumberSetting range = add(new NumberSetting("Range", 32, 8, 96, 4));

	public Radar() {
		super("Radar", 0.01f, 0.30f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null || mc.world == null) return;
		int s = 84;
		box(ctx, 0, 0, s, s);
		int cx = s / 2, cy = s / 2;
		ctx.fill(cx, 3, cx + 1, s - 3, 0x30FFFFFF);
		ctx.fill(3, cy, s - 3, cy + 1, 0x30FFFFFF);
		RenderUtil.rrect(ctx, cx - 2, cy - 2, 5, 5, Colors.wave(0f));

		double rad = Math.toRadians(mc.player.getYaw());
		double sin = Math.sin(rad), cos = Math.cos(rad);
		double scale = (s / 2.0 - 5) / range.get();
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player) continue;
			double dx = p.getX() - mc.player.getX(), dz = p.getZ() - mc.player.getZ();
			double fwd = dx * -sin + dz * cos;
			double right = dx * -cos + dz * -sin;
			int x = cx + (int) Math.round(right * scale), y = cy - (int) Math.round(fwd * scale);
			if (x < 3 || x > s - 5 || y < 3 || y > s - 5) continue;
			ctx.fill(x - 1, y - 1, x + 2, y + 2, 0xFFFF6B6B);
		}
		w = s;
		h = s;
	}
}
