package com.lovenote.util;

import net.minecraft.util.math.MathHelper;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Colors {
// Active theme (three gradient stops).
public static int C1 = 0xFFFF9ACB;
public static int C2 = 0xFFFF5FA2;
public static int C3 = 0xFFB28DFF;

// Custom theme state (driven by the Interface module).
public static float hue = 0.92f, sat = 1.0f, bri = 1.0f, spread = 0.18f;
public static boolean chroma = false;
public static float chromaSpeed = 0.12f;

private static String themeName = "Lovenote";

private static final Map<String, int[]> THEMES = new LinkedHashMap<>();

static {
THEMES.put("Lovenote",  new int[]{0xFFFF9ACB, 0xFFFF5FA2, 0xFFB28DFF});
THEMES.put("Sakura",    new int[]{0xFFFFC1D9, 0xFFFF8FB1, 0xFFFFD6A5});
THEMES.put("Midnight",  new int[]{0xFF5B8DEF, 0xFF8B5CF6, 0xFF22D3EE});
THEMES.put("Mint",      new int[]{0xFF6EE7B7, 0xFF38BDF8, 0xFFA78BFA});
THEMES.put("Skeet",     new int[]{0xFF37B1DA, 0xFFCC46CD, 0xFFCCE335});
THEMES.put("Blood",     new int[]{0xFFFF4D4D, 0xFFB3001B, 0xFFFF9E6D});
THEMES.put("Ice",       new int[]{0xFFBFF3FF, 0xFF5FB8FF, 0xFF2E6BFF});
THEMES.put("Gold",      new int[]{0xFFFFE9A3, 0xFFFFB300, 0xFFFF7A00});
THEMES.put("Toxic",     new int[]{0xFFB6FF3B, 0xFF3BFF88, 0xFFB03BFF});
THEMES.put("Cyber",     new int[]{0xFFFF2D95, 0xFF00E5FF, 0xFFFFE94D});
THEMES.put("Vapor",     new int[]{0xFFFF9CE0, 0xFF9C7BFF, 0xFF6FF5FF});
THEMES.put("Mono",      new int[]{0xFFFFFFFF, 0xFFB8B8C4, 0xFF6E6E7A});
THEMES.put("Inferno",   new int[]{0xFFFFB300, 0xFFFF4D00, 0xFFFF1A00});
THEMES.put("Forest",    new int[]{0xFFA8E063, 0xFF56AB2F, 0xFF2E6B3A});
THEMES.put("Ocean",     new int[]{0xFF6FE8FF, 0xFF1E90FF, 0xFF1B3B8F});
THEMES.put("Grape",     new int[]{0xFFD9A7FF, 0xFF9B4DFF, 0xFF5B1FA8});
THEMES.put("Peach",     new int[]{0xFFFFD6B0, 0xFFFF9A76, 0xFFFF6B8B});
THEMES.put("Cotton",    new int[]{0xFFAEE8FF, 0xFFFFB3E6, 0xFFC4B5FF});
THEMES.put("Nebula",    new int[]{0xFF7A4DFF, 0xFFFF4FA3, 0xFF46E0FF});
THEMES.put("Ember",     new int[]{0xFFFF8A3D, 0xFFC0392B, 0xFFFFC857});
THEMES.put("Lime",      new int[]{0xFFC6FF00, 0xFF76FF03, 0xFF00E676});
THEMES.put("Rose",      new int[]{0xFFFFD1DC, 0xFFFF4D6D, 0xFFC9184A});
THEMES.put("Steel",     new int[]{0xFFD6E4F0, 0xFF7B96B2, 0xFF3B5266});
THEMES.put("Amethyst",  new int[]{0xFFE0BBE4, 0xFF957DAD, 0xFF6A4C93});
THEMES.put("Sunset",    new int[]{0xFFFFC24B, 0xFFFF6B35, 0xFFC33149});
THEMES.put("Aurora",    new int[]{0xFF26F0B0, 0xFF2E8CFF, 0xFF7CFF6B});
THEMES.put("Bubble",    new int[]{0xFF9BE7FF, 0xFFB39DFF, 0xFFFF9AD5});
THEMES.put("Sand",      new int[]{0xFFFFE8C2, 0xFFD9A76A, 0xFF8C6239});
THEMES.put("Coffee",    new int[]{0xFFD2B48C, 0xFF8B5E3C, 0xFF4B2E1E});
THEMES.put("Cherry",    new int[]{0xFFFF6B8B, 0xFFD6003F, 0xFFFFA6B8});
THEMES.put("Rainbow",   new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF});
THEMES.put("Custom",    new int[]{0xFFFF9ACB, 0xFFFF5FA2, 0xFFB28DFF});
}

private Colors() {}

public static String[] themeNames() {
return THEMES.keySet().toArray(new String[0]);
}

public static String themeName() {
return themeName;
}

public static boolean isCustom() {
return "Custom".equals(themeName);
}

public static void setTheme(String n) {
if (n == null || !THEMES.containsKey(n)) n = "Lovenote";
themeName = n;
applyCurrent();
}

/** Recomputes the active gradient. Safe to call every frame. */
public static void applyCurrent() {
if (isCustom()) {
applyCustom();
return;
}
if ("Rainbow".equals(themeName)) {
float t = (System.currentTimeMillis() % 12000L) / 12000f;
C1 = hsv(t, 0.85f, 1f);
C2 = hsv(t + 0.33f, 0.85f, 1f);
C3 = hsv(t + 0.66f, 0.85f, 1f);
return;
}
int[] t = THEMES.get(themeName);
C1 = t[0];
C2 = t[1];
C3 = t[2];
}

private static void applyCustom() {
float h = hue;
if (chroma) {
float t = (System.currentTimeMillis() % 60000L) / 60000f;
h += t * chromaSpeed * 20f;
}
h = h - (float) Math.floor(h);
C1 = hsv(h - spread, sat, Math.min(1f, bri * 1.10f));
C2 = hsv(h, sat, bri);
C3 = hsv(h + spread, sat, Math.min(1f, bri * 1.05f));
}

/** Self-contained HSV -> ARGB. */
public static int hsv(float h, float s, float v) {
h = h - (float) Math.floor(h);
s = MathHelper.clamp(s, 0f, 1f);
v = MathHelper.clamp(v, 0f, 1f);
int i = (int) (h * 6f);
float f = h * 6f - i;
float p = v * (1f - s);
float q = v * (1f - f * s);
float t = v * (1f - (1f - f) * s);
float r, g, b;
switch (i % 6) {
case 0 -> { r = v; g = t; b = p; }
case 1 -> { r = q; g = v; b = p; }
case 2 -> { r = p; g = v; b = t; }
case 3 -> { r = p; g = q; b = v; }
case 4 -> { r = t; g = p; b = v; }
default -> { r = v; g = p; b = q; }
}
return 0xFF000000 | ((int) (r * 255f) << 16) | ((int) (g * 255f) << 8) | (int) (b * 255f);
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
return hsv(h, 0.7f, 1f);
}

public static int byMode(String mode, float health01, float offset) {
return switch (mode) {
case "Health" -> health(health01);
case "Rainbow" -> rainbow(offset);
default -> wave(offset);
};
}
}