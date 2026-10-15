package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

/** Server, ping and player count. */
public class ServerInfo extends Module {
	public ServerInfo() {
		super("ServerInfo", Category.HUD);
		describe("Server, ping and player count.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		String server = mc.getCurrentServerEntry() == null ? "singleplayer" : mc.getCurrentServerEntry().address;
		int ping = 0;
		if (mc.getNetworkHandler() != null) {
			PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			if (e != null) ping = e.getLatency();
		}
		int players = mc.getNetworkHandler() == null ? 1 : mc.getNetworkHandler().getPlayerList().size();
		String sub = ping + "ms  \u00B7  " + players + " players";
		int w = Math.max(mc.textRenderer.getWidth(server), mc.textRenderer.getWidth(sub)) + 14;
		int x = 8, y = 8;
		RenderUtil.rrect(ctx, x, y, w, 28, 0xB00C0C11);
		RenderUtil.skeetBar(ctx, x + 3, y + 26, w - 6, 1);
		ctx.drawTextWithShadow(mc.textRenderer, server, x + 7, y + 5, Colors.wave(0f));
		ctx.drawText(mc.textRenderer, sub, x + 7, y + 16, 0xFFB0B0C0, false);
	}
}