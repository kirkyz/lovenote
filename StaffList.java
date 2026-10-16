package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

/** Lists players in spectator mode (the usual way staff vanish). */
public class StaffList extends HudWidget {
	public StaffList() {
		super("StaffList", 0.18f, 0.07f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		TextRenderer tr = mc.textRenderer;
		List<String> names = new ArrayList<>();
		if (mc.getNetworkHandler() != null) {
			for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
				if (e.getGameMode() == GameMode.SPECTATOR) names.add(e.getProfile().name());
			}
		}
		if (editing && names.isEmpty()) names.add("spectators show here");

		int bw = tr.getWidth("StaffList") + 36;
		for (String n : names) bw = Math.max(bw, tr.getWidth(n) + 16);
		box(ctx, 0, 0, bw, 17);
		ctx.drawTextWithShadow(tr, "StaffList", 7, 5, 0xFFFFFFFF);
		RenderUtil.rrect(ctx, bw - 14, 5, 7, 8, Colors.wave(0f));
		int y = 19;
		for (String n : names) {
			box(ctx, 0, y, bw, 13);
			ctx.drawTextWithShadow(tr, n, 7, y + 3, 0xFFFF8A8A);
			y += 14;
		}
		w = bw;
		h = y;
	}
}
