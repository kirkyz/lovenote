package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Toast notifications when modules are toggled. */
public class Notifications extends HudWidget {
	private record Toast(String text, boolean on, long time) {}

	private static final List<Toast> LIST = new ArrayList<>();

	public Notifications() {
		super("Notifications", 0.80f, 0.86f);
		enabled = true;
	}

	public static synchronized void push(String name, boolean on) {
		LIST.add(new Toast(name + (on ? " enabled" : " disabled"), on, System.currentTimeMillis()));
		while (LIST.size() > 5) LIST.remove(0);
	}

	@Override
	protected synchronized void draw(DrawContext ctx, float td) {
		long now = System.currentTimeMillis();
		LIST.removeIf(t -> now - t.time > 2600);
		List<Toast> list = new ArrayList<>(LIST);
		if (editing && list.isEmpty()) list.add(new Toast("Notifications preview", true, now));
		int bw = 140;
		int y = 0;
		for (int i = list.size() - 1; i >= 0; i--) {
			Toast t = list.get(i);
			long age = now - t.time;
			float in = Math.min(1f, age / 220f);
			float out = age > 2300 ? (age - 2300) / 300f : 0f;
			float ease = 1f - (1f - in) * (1f - in);
			int x = (int) ((1f - (ease - out)) * (bw + 10));
			box(ctx, x, y, bw, 18);
			ctx.fill(x + 3, y + 4, x + 5, y + 14, t.on ? Colors.wave(i * 0.1f) : 0xFF5A5A6A);
			ctx.drawTextWithShadow(mc.textRenderer, t.text, x + 10, y + 5, 0xFFE8E8F0);
			y += 21;
		}
		w = bw;
		h = Math.max(18, y);
	}
}
