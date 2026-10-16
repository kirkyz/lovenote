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
		drawFace(ctx, mask(size), x, y, size, 1f, false, color);
	}

	public static void drawCentered(DrawContext ctx, int cx, int y, int size, int color) {
		draw(ctx, cx - size / 2, y, size, color);
	}

	/**
	 * Spinning fake-3D version. Extrudes backwards along X and squeezes horizontally by |cos|,
	 * so the mark reads as a solid object rotating on its Y axis.
	 */
	public static void draw3D(DrawContext ctx, int cx, int cy, int size, int color, float angle, int depth) {
		if (size <= 0 || (color >>> 24) == 0) return;
		boolean[][] m = mask(size);
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		float sx = Math.max(0.09f, Math.abs(cos));
		boolean mirror = cos < 0f;
		int x0 = cx - size / 2, y0 = cy - size / 2;
		int base = color >>> 24;

		for (int d = depth; d >= 1; d--) {
			int off = Math.round(-d * cos * 1.5f);
			int dy = Math.round(d * sin * 0.5f);
			int a = (int) (base * (0.30f + 0.55f * (1f - d / (float) (depth + 1))));
			drawFace(ctx, m, x0 + off, y0 + dy, size, sx, mirror, Colors.alpha(Colors.darken(color), a));
		}
		drawFace(ctx, m, x0, y0, size, sx, mirror, color);
	}

	/** Continuous angle in radians for a full turn every periodMs. */
	public static float spin(long periodMs) {
		if (periodMs <= 0) return 0f;
		return (System.currentTimeMillis() % periodMs) / (float) periodMs * (float) (Math.PI * 2.0);
	}

	private static void drawFace(DrawContext ctx, boolean[][] m, int x0, int y0, int size, float sx, boolean mirror, int color) {
		if ((color >>> 24) == 0) return;
		int half = size / 2;
		for (int py = 0; py < size; py++) {
			int run = -1;
			for (int px = 0; px <= size; px++) {
				boolean on = px < size && m[py][px];
				if (on && run < 0) run = px;
				else if (!on && run >= 0) {
					int a = run, b = px;
					if (mirror) { int na = size - b, nb = size - a; a = na; b = nb; }
					int x1 = x0 + half + Math.round((a - half) * sx);
					int x2 = x0 + half + Math.round((b - half) * sx);
					if (x2 > x1) ctx.fill(x1, y0 + py, x2, y0 + py + 1, color);
					run = -1;
				}
			}
		}
	}
}
