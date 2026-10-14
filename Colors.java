package com.lovenote.util;

import net.minecraft.util.math.MathHelper;

public final class Colors {
	// Active theme (three gradient stops). Defaults to Lovenote.
	public static int C1 = 0xFFFF9ACB; // soft pink
	public static int C2 = 0xFFFF5FA2; // rose
	public static int C3 = 0xFFB28DFF; // lavender
	private static String themeName = "Lovenote";

	private Colors() {}

	public static void setTheme(String n) {
		if (n.equals(themeName)) return;
		themeName = n;
		switch (n) {
			case "Sakura" -> { C1 = 0xFFFFC1D9; C2 = 0xFFFF8FB1; C3 = 0xFFFFD6A5; }
			case "Midnight" -> { C1 = 0xFF5B8DEF; C2 = 0xFF8B5CF6; C3 = 0xFF22D3EE; }
			case "Mint" -> { C1 = 0xFF6EE7B7; C2 = 0xFF38BDF8; C3 = 0xFFA78BFA; }
			case "Skeet" -> { C1 = 0xFF37B1DA; C2 = 0xFFCC46CD; C3 = 0xFFCCE335; }
			default -> { C1 = 0xFFFF9ACB; C2 = 0xFFFF5FA2; C3 = 0xFFB28DFF; }
		}
	}

	public static int alpha(int c, int a) {
		return (MathHelper.clamp(a, 0, 255) << 24) | (c & 0xFFFFFF);
	}

	public static int lerp(int a, int b, float t) {
		t = MathHelper.clamp(t, 0f, 1f);
		int aa = (int) (((a >>> 24) & 255) + ((((b >>> 24) & 255) - ((a >>> 24) & 255)) * t));
		int r = (int) (((a >> 16) & 255) + ((((b >> 16) & 255) - ((a >> 16) & 255)) * t));
		int g = (int) (((a >> 8) & 255) + ((((b >> 8) & 255) - ((a >> 8) & 255)) * t));
		int bl = (int) ((a & 255) + (((b & 255) - (a & 255)) * t));
		return (aa << 24) | (r << 16) | (g << 8) | bl;
	}

	public static int darken(int c) {
		return lerp(c, 0xFF000000, 0.45f);
	}

	public static int skeet(float t) {
		t = MathHelper.clamp(t, 0f, 1f);
		return t < 0.5f ? lerp(C1, C2, t * 2f) : lerp(C2, C3, (t - 0.5f) * 2f);
	}

	public static int wave(float offset) {
		float t = (System.currentTimeMillis() % 3000L) / 3000f + offset;
		t = Math.abs((t % 1f) * 2f - 1f);
		return skeet(t);
	}

	public static int health(float f) {
		return lerp(0xFFFF3B3B, 0xFF3BFF6B, f);
	}

	public static int rainbow(float offset) {
		float h = ((System.currentTimeMillis() % 4000L) / 4000f + offset) % 1f;
		return 0xFF000000 | MathHelper.hsvToRgb(h, 0.7f, 1f);
	}

	public static int byMode(String mode, float health01, float offset) {
		return switch (mode) {
			case "Health" -> health(health01);
			case "Rainbow" -> rainbow(offset);
			default -> wave(offset);
		};
	}
}
