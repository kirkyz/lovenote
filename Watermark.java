package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.DrainLogo;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Watermark extends Module {
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final ModeSetting style = add(new ModeSetting("Style", "Glass", "Glass", "Solid", "None"));
	private final BoolSetting logo = add(new BoolSetting("Logo", true));
	private final BoolSetting fps = add(new BoolSetting("FPS", true));
	private final BoolSetting ping = add(new BoolSetting("Ping", true));
	private final BoolSetting server = add(new BoolSetting("Server", true));
	private final BoolSetting clock = add(new BoolSetting("Clock", true));
	private final BoolSetting coords = add(new BoolSetting("Coords", false));

	public Watermark() {
		super("Watermark", Category.HUD);
		describe("Brand chip with fps, ping, server and clock.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		TextRenderer tr = mc.textRenderer;

		StringBuilder info = new StringBuilder();
		if (fps.get()) info.append("  \u00B7  ").append(mc.getCurrentFps()).append(" fps");
		if (ping.get()) {
            int p = 0;
            if (mc.getNetworkHandler() != null) {
                PlayerListEntry e = mc.getNetworkHandler()
                        .getPlayerListEntry(mc.player.getUuid());
                if (e != null) p = e.getLatency();
            }
            info.append("  \u00B7  ").append(p).append("ms");
        }

        if (server.get()) {
            String s = mc.getCurrentServerEntry() == null
                    ? "singleplayer"
                    : mc.getCurrentServerEntry().address;
            info.append("  \u00B7  ").append(s);
        }

        if (coords.get()) {
            info.append("  \u00B7  ").append(
                    String.format("%.0f %.0f %.0f",
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ()));
        }

        if (clock.get()) {
            info.append("  \u00B7  ")
                    .append(LocalTime.now().format(CLOCK));
        }

        String brand = "lovenote";
        int logoW = logo.get() ? 15 : 0;
        int w = 10 + logoW + tr.getWidth(brand)
                + tr.getWidth(info.toString()) + 10;
        int h = 18;
        int x = ctx.getScaledWindowWidth() - w - 8;
        int y = 8;

        if (style.is("Glass")) {
            RenderUtil.rrect(ctx, x, y, w, h, 4, 0x8C0C0C11);
			RenderUtil.rrect(ctx, x, y, w, 1, 0, 0x33FFFFFF);
			ctx.fill(x, y, x + 1, y + h, 0x22FFFFFF);
		} else if (style.is("Solid")) {
			RenderUtil.rrect(ctx, x, y, w, h, 4, 0xE60C0C11);
		}
		if (!style.is("None")) RenderUtil.skeetBar(ctx, x + 4, y + h - 1, w - 8, 1);

		int cx = x + 8, ty = y + 5;
		if (logo.get()) {
			DrainLogo.draw(ctx, cx, ty - 1, 12, Colors.wave(0f));
			cx += 15;
		}
		for (int i = 0; i < brand.length(); i++) {
			String ch = String.valueOf(brand.charAt(i));
			ctx.drawTextWithShadow(tr, ch, cx, ty, Colors.wave(i * 0.08f));
			cx += tr.getWidth(ch);
		}
		ctx.drawText(tr, info.toString(), cx, ty, 0xFFB8B8C8, false);
	}
}
