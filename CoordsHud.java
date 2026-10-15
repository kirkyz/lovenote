package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/** Position, chunk and facing readout. */
public class CoordsHud extends Module {
	public CoordsHud() {
		super("CoordsHud", Category.HUD);
		describe("Position, chunk and facing.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null || mc.world == null) return;
		String dim = mc.world.getRegistryKey().getValue().getPath();
		String[] lines = {
				String.format("xyz  %.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()),
				"chunk  " + (mc.player.getBlockX() >> 4) + " " + (mc.player.getBlockZ() >> 4),
				"face  " + facing() + "  \u00B7  " + dim
		};
		int w = 0;
		for (String l : lines) w = Math.max(w, mc.textRenderer.getWidth(l));
		w += 14;
		int h = lines.length * 11 + 10;
		int x = 8, y = ctx.getScaledWindowHeight() - h - 8;
		RenderUtil.rrect(ctx, x, y, w, h, 0xB00C0C11);
		ctx.fill(x, y + 4, x + 2, y + h - 4, Colors.wave(0.3f));
		for (int i = 0; i < lines.length; i++)
			ctx.drawText(mc.textRenderer, lines[i], x + 8, y + 6 + i * 11, 0xFFD0D0DC, false);
	}

	private String facing() {
		float yaw = ((mc.player.getYaw() % 360) + 360) % 360;
		if (yaw < 45 || yaw >= 315) return "south";
		if (yaw < 135) return "west";
		if (yaw < 225) return "north";
		return "east";
	}
}