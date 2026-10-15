package com.lovenote.module.impl;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.spotify.SpotifyManager;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Now-playing widget. Connect and control it from the Lovenote menu's spotify tab. */
public class SpotifyHud extends Module {
	private final ModeSetting position = add(new ModeSetting("Position", "Bottom Left",
			"Bottom Left", "Bottom Right", "Top Left", "Top Right"));
	private final BoolSetting art = add(new BoolSetting("Album art", true));
	private final BoolSetting times = add(new BoolSetting("Show times", true));

	public SpotifyHud() {
		super("Spotify", Category.HUD);
	}

	public static String fmt(long ms) {
		long s = Math.max(0, ms) / 1000;
		return (s / 60) + ":" + String.format("%02d", s % 60);
	}

	public static void marquee(DrawContext ctx, TextRenderer tr, String text, int x, int y, int maxW, int color) {
		if (text == null || maxW <= 0) return;
		int w = tr.getWidth(text);
		if (w <= maxW) {
			ctx.drawTextWithShadow(tr, text, x, y, color);
			return;
		}
		int gap = 24;
		int off = (int) ((System.currentTimeMillis() / 30) % (w + gap));
		ctx.enableScissor(x, y - 1, x + maxW, y + 10);
		ctx.drawTextWithShadow(tr, text, x - off, y, color);
		ctx.drawTextWithShadow(tr, text, x - off + w + gap, y, color);
		ctx.disableScissor();
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		SpotifyManager s = SpotifyManager.I;
		if (!s.connected || !s.hasTrack) return;
		TextRenderer tr = mc.textRenderer;

		int corner = 5;
		InterfaceModule ui = LovenoteClient.modules == null ? null : LovenoteClient.modules.get(InterfaceModule.class);
		if (ui != null) corner = ui.corner.getI();

		int w = 196, h = 54;
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int x = position.is("Bottom Right") || position.is("Top Right") ? sw - w - 8 : 8;
		int y = position.is("Top Left") || position.is("Top Right") ? 40 : sh - h - 8;

		RenderUtil.rrect(ctx, x, y, w, h, corner, 0xE0101015);
		RenderUtil.skeetBar(ctx, x + 3, y, w - 6, 1);

		int tx = x + 6;
		if (art.get()) {
			int ax = x + 6, ay = y + 8, as = 30;
			if (s.artTexture != null) {
				ctx.drawTexture(s.artTexture, ax, ay, as, as, 0, 0, 64, 64, 64, 64);
			} else {
				RenderUtil.rrect(ctx, ax, ay, as, as, 3, 0xFF1F1F1F);
				ctx.drawCenteredTextWithShadow(tr, "\u266A", ax + as / 2, ay + 11, Colors.wave(0));
			}
			tx = ax + as + 6;
		}
		int maxW = x + w - 6 - tx;

		marquee(ctx, tr, s.title, tx, y + 7, maxW, 0xFFFFFFFF);
		marquee(ctx, tr, s.artist, tx, y + 18, maxW, 0xFF9A9A9A);

		// progress bar and the time stamps share one row, so nothing sits on the edge
		long prog = s.progress();
		float f = s.durationMs <= 0 ? 0f : Math.min(1f, prog / (float) s.durationMs);
		int by = y + 32;
		ctx.fill(tx, by, tx + maxW, by + 3, 0xFF2A2A2A);
		RenderUtil.gradientH(ctx, tx, by, tx + (int) (maxW * f), by + 3, Colors.C1, Colors.skeet(f));

		if (times.get()) {
			String el = fmt(prog);
			String du = fmt(s.durationMs);
			ctx.drawText(tr, el, tx, by + 6, 0xFF777777, false);
			ctx.drawText(tr, du, tx + maxW - tr.getWidth(du), by + 6, 0xFF777777, false);
			if (s.playing && maxW > 130) {
				String mid = "playing";
				ctx.drawText(tr, mid, tx + (maxW - tr.getWidth(mid)) / 2, by + 6, 0xFF4E4E5E, false);
			}
		} else if (s.playing) {
			ctx.drawText(tr, "playing", tx, by + 6, 0xFF4E4E5E, false);
		}
	}
}
