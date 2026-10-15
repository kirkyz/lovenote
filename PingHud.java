package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

/** Your connection quality on screen. */
public class PingHud extends Module {
	private int shown = -1;

	public PingHud() {
		super("PingHud", Category.HUD);
		describe("Ping and connection quality.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		int ping = 0;
		if (mc.getNetworkHandler() != null) {
			PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			if (e != null) ping = e.getLatency();
		}
		shown = shown < 0 ? ping : (int) (shown + (ping - shown) * 0.2f);
		int c = shown < 60 ? 0xFF6EE7A0 : shown < 140 ? 0xFFE8C86A : 0xFFFF7A7A;
		String text = shown + "ms";
		int w = mc.textRenderer.getWidth(text) + 14;
		int x = ctx.getScaledWindowWidth() - w - 8;
		int y = ctx.getScaledWindowHeight() - 24;
		RenderUtil.rrect(ctx, x, y, w, 14, 0xB00C0C11);
		ctx.fill(x + 4, y + 4, x + 5, y + 10, c);
		ctx.drawText(mc.textRenderer, text, x + 9, y + 3, Colors.wave(0f), false);
	}
}