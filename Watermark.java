package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class Watermark extends Module {
	public Watermark() {
		super("Watermark", Category.HUD);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		TextRenderer tr = mc.textRenderer;
		int ping = 0;
		if (mc.getNetworkHandler() != null) {
			PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			if (e != null) ping = e.getLatency();
		}
		String brand = "lovenote";
		String rest = "  " + mc.getSession().getUsername() + "  \u00B7  " + mc.getCurrentFps() + " fps  \u00B7  " + ping + "ms";
		int heart = tr.getWidth("\u2665 ");
		int w = heart + tr.getWidth(brand) + tr.getWidth(rest) + 16;
		int h = 17;
		int x = ctx.getScaledWindowWidth() - w - 8, y = 8;

		RenderUtil.rrect(ctx, x, y, w, h, 0xE0101015);
		RenderUtil.skeetBar(ctx, x + 3, y + h - 1, w - 6, 1);

		int cx = x + 8, ty = y + 5;
		ctx.drawTextWithShadow(tr, "\u2665 ", cx, ty, Colors.C2);
		cx += heart;
		for (int i = 0; i < brand.length(); i++) {
			String ch = String.valueOf(brand.charAt(i));
			ctx.drawTextWithShadow(tr, ch, cx, ty, Colors.wave(i * 0.08f));
			cx += tr.getWidth(ch);
		}
		ctx.drawText(tr, rest, cx, ty, 0xFFB8B8C8, false);
	}
}
