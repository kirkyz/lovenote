package com.lovenote.util;

import net.minecraft.client.gui.DrawContext;
import java.util.HashMap;
import java.util.Map;

/** Drain Gang heart-face, rasterised at runtime. Pure ctx.fill() - no assets, no textures. */
public final class DrainLogo {
	private static final Map<Integer, boolean[][]> CACHE = new HashMap<>();

	private DrainLogo() {}

	private static boolean[][] mask(int size) {
		return CACHE.computeIfAbsent(size, s -> {
			boolean[][] m = new boolean[s][s];
			for (int py = 0; py < s; py++) {
				for (int px = 0; px < s; px++) {
					double x = (px + 0.5) / s * 2.4 - 1.2;
					double y = 1.05 - (py + 0.5) / s * 2.4;
					boolean ink = ring(x, y) || face(x, y);
					boolean hole = eye(x, y) || mouth(x, y);
					m[py][px] = ink && !hole;
				}
			}
			return m;
		});
	}

	private static double heart(double x, double y) {
		double a = x * x + y * y - 1.0;
		return a * a * a - x * x * y * y * y;
	}

	private static boolean ring(double x, double y) {
		if (heart(x, y) >= 0) return false;
		return heart(x * 1.389, y * 1.389) >= 0;
	}

	private static boolean face(double x, double y) {
		double dx = x / 0.36, dy = (y + 0.16) / 0.30;
		return dx * dx + dy * dy <= 1.0;
	}

	private static boolean eye(double x, double y) {
		double dx = (Math.abs(x) - 0.13) / 0.05, dy = (y + 0.09) / 0.14;
		return dx * dx + dy * dy <= 1.0;
	}

	private static boolean mouth(double x, double y) {
		double dx = (x - 0.01) / 0.15, dy = (y + 0.29) / 0.085;
		return dx * dx + dy * dy <= 1.0;
	}

	/** Draws the mark, top-left at (x,y). Horizontal runs are merged into single fills. */
	public static void draw(DrawContext ctx, int x, int y, int size, int color) {
		if (size <= 0 || (color >>> 24) == 0) return;
		boolean[][] m = mask(size);
		for (int py = 0; py < size; py++) {
			int run = -1;
			for (int px = 0; px <= size; px++) {
				boolean on = px < size && m[py][px];
				if (on && run < 0) run = px;
				else if (!on && run >= 0) {
					ctx.fill(x + run, y + py, x + px, y + py + 1, color);
					run = -1;
				}
			}
		}
	}

	public static void drawCentered(DrawContext ctx, int cx, int y, int size, int color) {
		draw(ctx, cx - size / 2, y, size, color);
	}
}