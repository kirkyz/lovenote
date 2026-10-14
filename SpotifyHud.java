package com.lovenote.module.impl;

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
	private final ModeSetting position = add(new ModeSetting("Position", "Bottom Left", "Bottom Left", "Bottom Right", "Top Left"));
	private final BoolSetting art = add(new BoolSetting("Album art", true));

	public SpotifyHud() {
		super("Spotify", Category.HUD);
	}

	public static String fmt(long ms) {
		long s = ms / 1000;
		return (s / 60) + ":" + String.format("%02d", s % 60);
	}

	public static void marquee(DrawContext ctx, TextRenderer tr, String text, int x, int y, int maxW, int color) {
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

		int w = 190, h = 46;
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int x = position.is("Bottom Right") ? sw - w - 8 : 8;
		int y = position.is("Top Left") ? 40 : sh - h - 8;

		RenderUtil.rrect(ctx, x, y, w, h, 0xE0101015);
		RenderUtil.skeetBar(ctx, x + 3, y, w - 6, 1);

		int tx = x + 6;
		if (art.get()) {
			int ax = x + 6, ay = y + 7, as = 32;
			if (s.artTexture != null) {
				ctx.drawTexture(s.artTexture, ax, ay, as, as, 0, 0, 64, 64, 64, 64);
			} else {
				ctx.fill(ax, ay, ax + as, ay + as, 0xFF1F1F1F);
				ctx.drawCenteredTextWithShadow(tr, "\u266A", ax + as / 2, ay + 12, Colors.wave(0));
			}
			tx = ax + as + 6;
		}
		int maxW = x + w - 6 - tx;
		marquee(ctx, tr, s.title, tx, y + 8, maxW, 0xFFFFFFFF);
		marquee(ctx, tr, s.artist, tx, y + 19, maxW, 0xFF9A9A9A);

		long prog = s.progress();
		float f = s.durationMs <= 0 ? 0f : Math.min(1f, prog / (float) s.durationMs);
		int bx = tx, by = y + 33, bw = x + w - 6 - tx;
		ctx.fill(bx, by, bx + bw, by + 3, 0xFF2A2A2A);
		RenderUtil.gradientH(ctx, bx, by, bx + (int) (bw * f), by + 3, Colors.C1, Colors.skeet(f));
		String time = fmt(prog) + " / " + fmt(s.durationMs) + (s.playing ? "" : "  (paused)");
		ctx.drawText(tr, time, bx, by + 6, 0xFF777777, false);
	}
}
