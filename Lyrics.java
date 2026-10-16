package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.spotify.LyricsManager;
import com.lovenote.spotify.SpotifyManager;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Live synced lyrics pulled from lrclib.net. Toggle it off to hide. */
public class Lyrics extends HudWidget {
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "White"));
	private final NumberSetting lines = add(new NumberSetting("Lines", 3, 1, 7, 1));
	private final NumberSetting offset = add(new NumberSetting("Offset (ms)", 0, -3000, 3000, 50));
	private final BoolSetting centre = add(new BoolSetting("Centre text", true));
	private final BoolSetting backdrop = add(new BoolSetting("Background", true));
	private final BoolSetting dimOthers = add(new BoolSetting("Dim other lines", true));
	private final BoolSetting hidePaused = add(new BoolSetting("Hide when paused", true));
	private final BoolSetting showIdle = add(new BoolSetting("Show when idle", false));

	public Lyrics() {
		super("Lyrics", 0.5f, 0.82f);
		alsoIn.add(Category.SPOTIFY);
		describe("Live synced lyrics from lrclib.net.");
	}

	@Override
	public void onTick() {
		SpotifyManager s = SpotifyManager.I;
		LyricsManager.I.tick(s.progress() + (long) offset.get());
	}

	private int lineColour() {
		return switch (colour.get()) {
			case "Rainbow" -> Colors.rainbow(0f);
			case "White" -> 0xFFFFFFFF;
			default -> Colors.wave(0f);
		};
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		SpotifyManager s = SpotifyManager.I;
		LyricsManager L = LyricsManager.I;
		boolean live = s.connected && s.hasTrack;
		if (!live && !editing && !showIdle.get()) { w = 0; h = 0; return; }
		if (live && !s.playing && hidePaused.get() && !editing) { w = 0; h = 0; return; }

		TextRenderer tr = mc.textRenderer;
		int n = Math.max(1, lines.getI());
		int half = n / 2;

		if (!L.hasLyrics()) {
			String msg = live ? L.status : "not playing";
			int bw = tr.getWidth(msg) + 16;
			if (backdrop.get()) box(ctx, 0, 0, bw, 16);
			ctx.drawTextWithShadow(tr, msg, 8, 4, Colors.faint());
			w = bw;
			h = 16;
			return;
		}

		int maxW = 120;
		for (int i = -half; i <= half; i++) {
			String t = L.lineAt(i);
			if (!t.isEmpty()) maxW = Math.max(maxW, tr.getWidth(t));
		}
		int bw = maxW + 18;
		int bh = n * 12 + 8;
		if (backdrop.get()) box(ctx, 0, 0, bw, bh);
		RenderUtil.skeetBar(ctx, 4, 0, bw - 8, 1);

		int y = 5;
		for (int i = -half; i <= half; i++) {
			String t = L.lineAt(i);
			if (t.isEmpty()) { y += 12; continue; }
			boolean cur = i == 0;
			int col = cur ? lineColour() : (i < 0 ? Colors.faint() : Colors.dim());
			if (!cur && !dimOthers.get()) col = Colors.dim();
			String shown = tr.trimToWidth(t, bw - 18);
			int x = centre.get() ? (bw - tr.getWidth(shown)) / 2 : 9;
			ctx.drawTextWithShadow(tr, shown, x, y, col);
			y += 12;
		}
		w = bw;
		h = bh;
	}
}
