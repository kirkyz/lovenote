package com.lovenote.util;

import net.minecraft.util.math.MathHelper;

public final class Colors {
	// Accent gradient (primary -> mid -> secondary). Set by the Interface module.
	public static int C1 = 0xFFFF9ACB, C2 = 0xFFD97BC0, C3 = 0xFFB28DFF;
	public static boolean animated = true;
	public static boolean dark = true;

	public static final String[] PRESET_NAMES = {
			"Lovenote", "Purple", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Pink",
			"Sunset", "Dawn", "Mint", "Forest", "Ocean", "Ice", "Sky", "Lavender", "Magenta",
			"Rose", "Coral", "Amber", "Gold", "Lime", "Emerald", "Teal", "Aqua", "Azure",
			"Indigo", "Violet", "Plum", "Crimson", "Ember", "Neon", "Vapor", "Matrix", "Candy",
			"Midnight", "Blood", "Frost", "Haze", "Sakura", "Toxic"
	};

	public static final int[][] PRESETS = {
			{0xFF9ACB, 0xB28DFF}, {0xA855F7, 0x9647FF}, {0xEF4444, 0xF97316}, {0xF97316, 0xFACC15},
			{0xFACC15, 0xA3E635}, {0x22C55E, 0x10B981}, {0x06B6D4, 0x3B82F6}, {0x3B82F6, 0x6366F1},
			{0xEC4899, 0xF472B6},
			{0xFF7A45, 0xFFB84D}, {0xFFC48A, 0xFF7AB0}, {0x3BE8B0, 0x2BD9C4}, {0x2E7D32, 0x66BB6A},
			{0x0E7490, 0x22D3EE}, {0xA5F3FC, 0x67E8F9}, {0x38BDF8, 0x818CF8}, {0xC4B5FD, 0xE9D5FF},
			{0xE935C1, 0xFF5FD2},
			{0xFB7185, 0xF472B6}, {0xFF7F50, 0xFFA36B}, {0xFFB020, 0xFFD166}, {0xFFD700, 0xFFB800},
			{0xA3E635, 0x84CC16}, {0x10B981, 0x34D399}, {0x14B8A6, 0x0D9488}, {0x22D3EE, 0x38BDF8},
			{0x2563EB, 0x60A5FA},
			{0x4F46E5, 0x7C3AED}, {0x8B5CF6, 0xA78BFA}, {0xA21CAF, 0xD946EF}, {0xDC143C, 0xFF4D6D},
			{0xFF4500, 0xFF8C00}, {0x39FF14, 0x00E5FF}, {0xFF71CE, 0x01CDFE}, {0x00FF41, 0x008F11},
			{0xFF9ACB, 0x9BF6FF},
			{0x191970, 0x4B0082}, {0x8A0303, 0xDC143C}, {0xB3E5FC, 0x4FC3F7}, {0x9D8DF1, 0x6FD1E8},
			{0xFFB7C5, 0xFF8FAB}, {0xB6FF00, 0x00FF9C}
	};

	private Colors() {}

	public static void setAccent(int primary, int secondary) {
		C1 = 0xFF000000 | primary;
		C3 = 0xFF000000 | secondary;
		C2 = lerp(C1, C3, 0.5f);
	}

	// ---- GUI palette (light / dark)
	public static int bg() { return dark ? 0x101015 : 0xF2F2F6; }
	public static int card() { return dark ? 0xFF17171E : 0xFFFFFFFF; }
	public static int cardHover() { return dark ? 0xFF1D1D26 : 0xFFF4F4F9; }
	public static int text() { return dark ? 0xFFE8E8F0 : 0xFF1A1A22; }
	public static int dim() { return dark ? 0xFF8A8A9A : 0xFF6A6A78; }
	public static int faint() { return dark ? 0xFF5E5E70 : 0xFF9A9AA8; }
	public static int track() { return dark ? 0xFF2A2A36 : 0xFFDADAE4; }
	public static int chip() { return dark ? 0xFF24242E : 0xFFE6E6EE; }

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
		if (!animated) return skeet(MathHelper.clamp(0.25f + offset * 0.5f, 0f, 1f));
		float t = (System.currentTimeMillis() % 3000L) / 3000f + offset;
		t = Math.abs((t % 1f) * 2f - 1f);
		return skeet(t);
	}

	public static int health(float f) {
		return lerp(0xFFFF3B3B, 0xFF3BFF6B, f);
	}

	public static int hsv(float hue, float saturation, float value) {
		return 0xFF000000 | MathHelper.hsvToRgb(
				hue - (float) Math.floor(hue),
				MathHelper.clamp(saturation, 0f, 1f),
				MathHelper.clamp(value, 0f, 1f)
		);
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
