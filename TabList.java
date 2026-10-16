package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Clean player list with ping, shown while tab is held. */
public class TabList extends Module {
	private final BoolSetting ping = add(new BoolSetting("Show ping", true));
	private final NumberSetting limit = add(new NumberSetting("Max rows", 20, 5, 80, 1));

	public TabList() {
		super("TabList", Category.HUD);
		describe("Clean player list with ping.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null || mc.getNetworkHandler() == null) return;
		if (!mc.options.playerListKey.isPressed()) return;

		List<PlayerListEntry> list = new ArrayList<>(mc.getNetworkHandler().getPlayerList());
		list.sort(Comparator.comparing(e -> e.getProfile().name().toLowerCase()));
		int max = Math.min(list.size(), limit.getI());

		int w = 0;
		for (int i = 0; i < max; i++) w = Math.max(w, mc.textRenderer.getWidth(label(list.get(i))));
		w += 18;
		int h = max * 11 + 22;
		int x = ctx.getScaledWindowWidth() / 2 - w / 2;
		int y = 8;

		RenderUtil.rrect(ctx, x, y, w, h, 0xE60C0C11);
		RenderUtil.skeetBar(ctx, x + 4, y, w - 8, 1);
		ctx.drawCenteredTextWithShadow(mc.textRenderer, list.size() + " players", x + w / 2, y + 6, Colors.wave(0f));
		for (int i = 0; i < max; i++)
			ctx.drawText(mc.textRenderer, label(list.get(i)), x + 8, y + 20 + i * 11, 0xFFD8D8E4, false);
	}

	private String label(PlayerListEntry e) {
		String n = e.getProfile().name();
		return ping.get() ? n + "  " + e.getLatency() + "ms" : n;
	}
}