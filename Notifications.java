package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.DrainLogo;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Compact toasts when you toggle a module. */
public class Notifications extends Module {
	private record Toast(String text, boolean on, long time) {}

	private static final List<Toast> LIST = new ArrayList<>();

	public Notifications() {
		super("Notifications", Category.HUD);
		describe("Toast popups on every toggle.");
	}

	public static synchronized void push(String name, boolean on) {
		LIST.add(new Toast(name + (on ? "  on" : "  off"), on, System.currentTimeMillis()));
		while (LIST.size() > 6) LIST.remove(0);
	}

	public static synchronized void push(String text) {
		LIST.add(new Toast(text, true, System.currentTimeMillis()));
		while (LIST.size() > 6) LIST.remove(0);
	}

	@Override
	public synchronized void onHud(DrawContext ctx, float td) {
		long now = System.currentTimeMillis();
		LIST.removeIf(t -> now - t.time > 2400);
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int y = sh - 40;
		for (int i = LIST.size() - 1; i >= 0; i--) {
			Toast t = LIST.get(i);
			long age = now - t.time;
			int w = mc.textRenderer.getWidth(t.text) + 34, h = 17;
			float in = Math.min(1f, age / 200f);
			float out = age > 2150 ? (age - 2150) / 250f : 0f;
			float ease = 1f - (1f - in) * (1f - in);
			int x = sw - 8 - (int) (w * (ease - out));
			RenderUtil.rrect(ctx, x, y, w, h, 4, 0xE60C0C11);
			ctx.fill(x + 4, y + 3, x + 5, y + h - 3, t.on ? Colors.wave(i * 0.1f) : 0xFF5A5A6A);
			DrainLogo.draw(ctx, x + 8, y + 3, 11, t.on ? Colors.wave(i * 0.1f) : 0xFF5A5A6A);
			ctx.drawTextWithShadow(mc.textRenderer, t.text, x + 23, y + 5, 0xFFE8E8F0);
			y -= 20;
		}
	}
}