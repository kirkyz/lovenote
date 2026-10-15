package com.lovenote.module.impl;

import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.MultiSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Watermark extends HudWidget {
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

	private final MultiSetting elements = add(new MultiSetting("Elements", new String[]{"User", "FPS", "Time", "IP"}, "User", "FPS", "Time", "IP"));
	private final ModeSetting logoSide = add(new ModeSetting("Logo side", "Left", "Left", "Right"));
	private final BoolSetting logo = add(new BoolSetting("Logo", true));

	public Watermark() {
		super("Watermark", 0.30f, 0.012f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null) return;
		TextRenderer tr = mc.textRenderer;
		List<String[]> segs = new ArrayList<>();
		if (elements.is("User")) segs.add(new String[]{"user", mc.getSession().getUsername(), ""});
		if (elements.is("FPS")) segs.add(new String[]{"t", String.valueOf(mc.getCurrentFps()), " fps"});
		if (elements.is("Time")) segs.add(new String[]{"t", LocalTime.now().format(FMT), ""});
		if (elements.is("IP")) segs.add(new String[]{"t", mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "singleplayer", ""});

		int pad = 7, hh = 17;
		int total = pad;
		for (String[] s : segs) {
			int ww = tr.getWidth(s[1]) + tr.getWidth(s[2]);
			if (s[0].equals("user")) ww += 12;
			total += ww + pad;
		}
		if (segs.isEmpty()) total = 30;

		boolean left = logoSide.is("Left");
		int logoW = logo.get() ? hh + 4 : 0;
		int pillX = left ? logoW : 0;
		int logoX = left ? 0 : total + 4;

		box(ctx, pillX, 0, total, hh);
		RenderUtil.skeetBar(ctx, pillX + 3, hh - 1, total - 6, 1);
		int x = pillX + pad;
		for (int i = 0; i < segs.size(); i++) {
			String[] s = segs.get(i);
			if (s[0].equals("user")) {
				head(ctx, mc.player, x, 4, 9);
				x += 12;
			}
			ctx.drawTextWithShadow(tr, s[1], x, 5, 0xFFFFFFFF);
			x += tr.getWidth(s[1]);
			if (!s[2].isEmpty()) {
				ctx.drawTextWithShadow(tr, s[2], x, 5, Colors.wave(i * 0.15f));
				x += tr.getWidth(s[2]);
			}
			if (i < segs.size() - 1) ctx.fill(x + pad / 2, 4, x + pad / 2 + 1, hh - 4, 0xFF3A3A46);
			x += pad;
		}

		if (logo.get()) {
			box(ctx, logoX, 0, hh, hh);
			ctx.drawCenteredTextWithShadow(tr, "\u2665", logoX + hh / 2, 5, Colors.wave(0f));
		}
		w = pillX + total + (logo.get() && !left ? hh + 4 : 0);
		h = hh;
	}
}
