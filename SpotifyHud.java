package com.lovenote.module.impl;
import net.minecraft.client.gl.RenderPipelines;

import com.lovenote.setting.BoolSetting;
import com.lovenote.spotify.SpotifyManager;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** "Music" widget: now playing with album art, controls hint and progress. */
public class SpotifyHud extends HudWidget {
	private final BoolSetting art = add(new BoolSetting("Album art", true));
	private final BoolSetting showIdle = add(new BoolSetting("Show when idle", false));

	public SpotifyHud() {
		super("Music", 0.02f, 0.07f);
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
	protected void draw(DrawContext ctx, float td) {
		SpotifyManager s = SpotifyManager.I;
		boolean live = s.connected && s.hasTrack;
		if (!live && !editing && !showIdle.get()) { w = 0; h = 0; return; }
		TextRenderer tr = mc.textRenderer;

		int bw = 150, bh = 46;
		box(ctx, 0, 0, bw, bh);
		RenderUtil.skeetBar(ctx, 3, 0, bw - 6, 1);

		int tx = 8;
		if (art.get()) {
			if (live && s.artTexture != null) {
				ctx.drawTexture(RenderPipelines.GUI_TEXTURED, s.artTexture, 6, 6, 0, 0, 34, 34, 64, 64, 64, 64);
			} else {
				RenderUtil.rrect(ctx, 6, 6, 34, 34, 0xFF22222C);
				ctx.drawCenteredTextWithShadow(tr, "\u266A", 23, 20, Colors.wave(0));
			}
			tx = 46;
		}
		int maxW = bw - 6 - tx;
		String title = live ? s.title : "Not playing";
		String artist = live ? s.artist : "open the spotify tab";
		marquee(ctx, tr, title, tx, 7, maxW, 0xFFFFFFFF);
		marquee(ctx, tr, artist, tx, 18, maxW, 0xFF8A8A9A);

		String controls = "<<    " + (live && s.playing ? "||" : ">") + "    >>";
		ctx.drawTextWithShadow(tr, controls, tx + (maxW - tr.getWidth(controls)) / 2, 29, Colors.wave(0.2f));

		long prog = live ? s.progress() : 0;
		float f = live && s.durationMs > 0 ? Math.min(1f, prog / (float) s.durationMs) : 0f;
		int bx = tx, by = bh - 6, bwid = bw - 6 - tx;
		ctx.fill(bx, by, bx + bwid, by + 2, 0xFF2A2A36);
		RenderUtil.gradientH(ctx, bx, by, bx + (int) (bwid * f), by + 2, Colors.C1, Colors.skeet(f));
		w = bw;
		h = bh;
	}
}
