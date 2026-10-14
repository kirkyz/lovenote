package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Small toasts when you toggle a module. */
public class Notifications extends Module {
	private record Toast(String text, boolean on, long time) {}

	private static final List<Toast> LIST = new ArrayList<>();

	public Notifications() {
		super("Notifications", Category.HUD);
	}

	public static synchronized void push(String name, boolean on) {
		LIST.add(new Toast(name + (on ? " enabled" : " disabled"), on, System.currentTimeMillis()));
		while (LIST.size() > 5) LIST.remove(0);
	}

	@Override
	public synchronized void onHud(DrawContext ctx, float td) {
		long now = System.currentTimeMillis();
		LIST.removeIf(t -> now - t.time > 2600);
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int y = sh - 44;
		for (int i = LIST.size() - 1; i >= 0; i--) {
			Toast t = LIST.get(i);
			long age = now - t.time;
			int w = mc.textRenderer.getWidth(t.text) + 22, h = 18;
			float in = Math.min(1f, age / 220f);
			float out = age > 2300 ? (age - 2300) / 300f : 0f;
			float ease = 1f - (1f - in) * (1f - in);
			int x = sw - 8 - (int) (w * (ease - out));
			RenderUtil.rrect(ctx, x, y, w, h, 0xE0101015);
			ctx.fill(x + 3, y + 4, x + 5, y + h - 4, t.on ? Colors.wave(i * 0.1f) : 0xFF5A5A6A);
			ctx.drawTextWithShadow(mc.textRenderer, t.text, x + 10, y + 5, 0xFFE8E8F0);
			y -= 22;
		}
	}
}
