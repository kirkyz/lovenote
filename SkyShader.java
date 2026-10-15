package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.Tex;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** Replaces the vanilla sky with a configurable one. Driven by WorldRendererMixin. */
public class SkyShader extends Module {

private static final Identifier SUN = new Identifier("textures/environment/sun.png");
private static final Identifier MOON = new Identifier("textures/environment/moon_phases.png");
private static final int SEGS = 28;

// preset -> {top, horizon, nebulaA, nebulaB, aurora}
private static final Map<String, int[]> PRESETS = new LinkedHashMap<>();

static {
PRESETS.put("Nebula",     new int[]{0xFF0A0620, 0xFF241246, 0xFF7A4DFF, 0xFFFF4FA3, 0xFF46E0FF});
PRESETS.put("Aurora",     new int[]{0xFF03101F, 0xFF0B3A44, 0xFF26F0B0, 0xFF2E8CFF, 0xFF7CFF6B});
PRESETS.put("Sunset",     new int[]{0xFF1B1035, 0xFFFF7A45, 0xFFFF4E8A, 0xFFFFC24B, 0xFFFF7A45});
PRESETS.put("Deep Space", new int[]{0xFF02030A, 0xFF070C1E, 0xFF3B5BFF, 0xFF9A4DFF, 0xFF4DD9FF});
PRESETS.put("Cyberpunk",  new int[]{0xFF0B0417, 0xFF2A0A4A, 0xFFFF2D95, 0xFF00E5FF, 0xFFFFE94D});
PRESETS.put("Sakura",     new int[]{0xFF241226, 0xFFFFB3D1, 0xFFFF7FB0, 0xFFFFD9E8, 0xFFFFC1D9});
PRESETS.put("Void",       new int[]{0xFF000000, 0xFF0A0A0F, 0xFF6B4DFF, 0xFF2B2B55, 0xFF8A7DFF});
PRESETS.put("Storm",      new int[]{0xFF0D1116, 0xFF2A3340, 0xFF6E86A8, 0xFF9FB6D4, 0xFFB9C8DD});
PRESETS.put("Mint",       new int[]{0xFF04201C, 0xFF0E4A3E, 0xFF3BFFB0, 0xFF46D9FF, 0xFF9BFF6B});
PRESETS.put("Candy",      new int[]{0xFF2A0F3A, 0xFFFF6FD8, 0xFF6FE8FF, 0xFFFFE066, 0xFFFF8AE0});
PRESETS.put("Inferno",    new int[]{0xFF1A0600, 0xFF7A1B00, 0xFFFF4D00, 0xFFFFB300, 0xFFFF2E00});
PRESETS.put("Ice",        new int[]{0xFF03121F, 0xFF0E3C5C, 0xFF7FE3FF, 0xFFBFF3FF, 0xFF5FB8FF});
}

public final ModeSetting worlds = add(new ModeSetting("Worlds", "Overworld", "Overworld", "All", "Overworld + End"));
public final ModeSetting preset = add(new ModeSetting("Preset", "Nebula", PRESETS.keySet().toArray(new String[0])));
public final BoolSetting useTheme = add(new BoolSetting("Theme colours", false));
public final BoolSetting stars = add(new BoolSetting("Stars", true));
public final NumberSetting starCount = add(new NumberSetting("Star count", 450, 0, 1500, 50));
public final NumberSetting starSize = add(new NumberSetting("Star size", 0.38, 0.1, 1.5, 0.05));
public final NumberSetting twinkle = add(new NumberSetting("Twinkle", 1.2, 0, 4, 0.1));
public final BoolSetting shooting = add(new BoolSetting("Shooting stars", true));
public final BoolSetting nebula = add(new BoolSetting("Nebula clouds", true));
public final NumberSetting nebulaAlpha = add(new NumberSetting("Nebula opacity", 60, 0, 200, 5));
public final BoolSetting aurora = add(new BoolSetting("Aurora ribbons", true));
public final BoolSetting horizonGlow = add(new BoolSetting("Horizon glow", true));
public final BoolSetting sunMoon = add(new BoolSetting("Sun & moon", true));
public final BoolSetting daylight = add(new BoolSetting("Blend daylight", true));
public final NumberSetting daylightAmount = add(new NumberSetting("Daylight blend", 45, 0, 100, 5));
public final BoolSetting blendFog = add(new BoolSetting("Fog blending", true));

private final Random rnd = new Random(1337L);
private float[] sx, sy, sz, sphase, sbright;
private int starSeed = -1;
private float[] shootX, shootY, shootZ, shootOff;
private final Vector3f right = new Vector3f();
private final Vector3f up = new Vector3f();

public SkyShader() {
super("SkyShader", Category.VISUALS);
}

/** @return true when vanilla's sky should be cancelled. */
public boolean render(MatrixStack matrices, Matrix4f projection, float tickDelta, Camera camera, Runnable fogCallback) {
if (!enabled || mc.world == null) return false;

switch (worlds.get()) {
case "Overworld" -> { if (!mc.world.getRegistryKey().equals(World.OVERWORLD)) return false; }
case "Overworld + End" -> { if (mc.world.getRegistryKey().equals(World.NETHER)) return false; }
default -> { }
}

Tex.ensure();

int[] p = PRESETS.get(preset.get());
if (p == null) p = PRESETS.get("Nebula");
int top = p[0], horizon = p[1], nebA = p[2], nebB = p[3], aur = p[4];

if (useTheme.get()) {
nebA = Colors.C1;
nebB = Colors.C3;
aur = Colors.C2;
top = Colors.lerp(top, Colors.C3, 0.35f);
horizon = Colors.lerp(horizon, Colors.C2, 0.35f);
}

if (daylight.get()) {
Vec3d sky = mc.world.getSkyColor(camera.getPos(), tickDelta);
int sc = 0xFF000000 | ((int) (MathHelper.clamp(sky.x, 0, 1) * 255) << 16)
| ((int) (MathHelper.clamp(sky.y, 0, 1) * 255) << 8)
| (int) (MathHelper.clamp(sky.z, 0, 1) * 255);
float amt = daylightAmount.getI() / 100f;
top = Colors.lerp(top, sc, amt);
horizon = Colors.lerp(horizon, sc, amt * 0.85f);
}

float time = (System.currentTimeMillis() % 600000L) / 1000f;

right.set(1, 0, 0);
up.set(0, 1, 0);
camera.getRotation().transform(right);
camera.getRotation().transform(up);

RenderSystem.depthMask(false);
RenderSystem.disableCull();
RenderSystem.enableBlend();
RenderSystem.defaultBlendFunc();

if (fogCallback != null) {
fogCallback.run();
if (!blendFog.get()) {
RenderSystem.setShaderFogStart(2048f);
RenderSystem.setShaderFogEnd(4096f);
}
}

drawDome(matrices, top, horizon);
if (horizonGlow.get()) drawHorizonGlow(matrices, horizon);
if (nebula.get()) drawNebula(matrices, nebA, nebB, time);
if (aurora.get()) drawAurora(matrices, aur, time);
if (stars.get()) drawStars(matrices, time);
if (shooting.get()) drawShootingStars(matrices, time);
if (sunMoon.get()) drawSunMoon(matrices, tickDelta);

RenderSystem.defaultBlendFunc();
RenderSystem.disableBlend();
RenderSystem.enableCull();
RenderSystem.depthMask(true);
return true;
}

// ------------------------------------------------------------------ geometry

private static void vc(BufferBuilder b, Matrix4f m, float x, float y, float z, int c) {
b.vertex(m, x, y, z)
.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255)
.next();
}

private static void vt(BufferBuilder b, Matrix4f m, float x, float y, float z, float u, float v, int c) {
b.vertex(m, x, y, z)
.texture(u, v)
.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255)
.next();
}

private void billboard(BufferBuilder b, Matrix4f m, float x, float y, float z, float size, int color) {
float h = size * 0.5f;
float rx = right.x * h, ry = right.y * h, rz = right.z * h;
float ux = up.x * h, uy = up.y * h, uz = up.z * h;
vt(b, m, x - rx - ux, y - ry - uy, z - rz - uz, 0f, 1f, color);
vt(b, m, x + rx - ux, y + ry - uy, z + rz - uz, 1f, 1f, color);
vt(b, m, x + rx + ux, y + ry + uy, z + rz + uz, 1f, 0f, color);
vt(b, m, x - rx + ux, y - ry + uy, z - rz + uz, 0f, 0f, color);
}

private void drawDome(MatrixStack s, int top, int horizon) {
RenderSystem.setShader(GameRenderer::getPositionColorProgram);
Matrix4f m = s.peek().getPositionMatrix();
BufferBuilder b = Tessellator.getInstance().getBuffer();

b.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
vc(b, m, 0f, 100f, 0f, top);
for (int i = 0; i <= SEGS; i++) {
double a = (Math.PI * 2 * i) / SEGS;
vc(b, m, (float) (Math.cos(a) * 100), 0f, (float) (Math.sin(a) * 100), horizon);
}
Tessellator.getInstance().draw();

int floor = Colors.lerp(horizon, 0xFF000000, 0.62f);
b.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
vc(b, m, 0f, -100f, 0f, floor);
for (int i = 0; i <= SEGS; i++) {
double a = (Math.PI * 2 * i) / SEGS;
vc(b, m, (float) (Math.cos(a) * 100), 0f, (float) (Math.sin(a) * 100), horizon);
}
Tessellator.getInstance().draw();
}

private void drawHorizonGlow(MatrixStack s, int horizon) {
RenderSystem.setShader(GameRenderer::getPositionColorProgram);
RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
Matrix4f m = s.peek().getPositionMatrix();
BufferBuilder b = Tessellator.getInstance().getBuffer();
b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
int col = Colors.alpha(horizon, 150);
int clear = Colors.alpha(horizon, 0);
for (int i = 0; i <= SEGS; i++) {
double a = (Math.PI * 2 * i) / SEGS;
float x = (float) (Math.cos(a) * 100), z = (float) (Math.sin(a) * 100);
vc(b, m, x, 0f, z, col);
vc(b, m, x, 26f, z, clear);
}
Tessellator.getInstance().draw();
RenderSystem.defaultBlendFunc();
}

private void drawNebula(MatrixStack s, int a, int b2, float time) {
RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
RenderSystem.setShaderTexture(0, Tex.GLOW);
RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
Matrix4f m = s.peek().getPositionMatrix();
BufferBuilder b = Tessellator.getInstance().getBuffer();
b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
for (int i = 0; i < 14; i++) {
float ang = i * 0.4488f + time * 0.012f;
float elev = ((i % 5) - 2) * 0.30f;
float r = 88f;
float x = (float) (Math.cos(ang) * Math.cos(elev)) * r;
float y = (float) Math.sin(elev) * r * 0.85f + 12f;
float z = (float) (Math.sin(ang) * Math.cos(elev)) * r;
float size = 60f + (i % 4) * 26f;
int col = (i % 2 == 0) ? a : b2;
int al = (int) (nebulaAlpha.getI() * (0.45f + (i % 3) * 0.28f));
billboard(b, m, x, y, z, size, Colors.alpha(col, MathHelper.clamp(al, 0, 255)));
}
Tessellator.getInstance().draw();
RenderSystem.defaultBlendFunc();
}

private void drawAurora(MatrixStack s, int color, float time) {
RenderSystem.setShader(GameRenderer::getPositionColorProgram);
RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
Matrix4f m = s.peek().getPositionMatrix();
BufferBuilder b = Tessellator.getInstance().getBuffer();
for (int c = 0; c < 3; c++) {
float base = c * 2.1f + time * 0.03f;
float radius = 84f + c * 6f;
float span = 1.9f;
b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
for (int i = 0; i <= 48; i++) {
float f = i / 48f;
float ang = base + (f - 0.5f) * span;
float x = (float) Math.cos(ang) * radius;
float z = (float) Math.sin(ang) * radius;
float h = 26f + (float) Math.sin(f * 7f + time * 0.7f + c * 2f) * 10f;
float edge = (float) Math.sin(f * Math.PI);
vc(b, m, x, 2f, z, Colors.alpha(color, (int) (150 * edge)));
vc(b, m, x * 0.98f, h, z * 0.98f, Colors.alpha(color, (int) (16 * edge)));
}
Tessellator.getInstance().draw();
}
RenderSystem.defaultBlendFunc();
}

private void buildStars(int n) {
if (sx != null && sx.length == n && starSeed == n) return;
starSeed = n;
sx = new float[n]; sy = new float[n]; sz = new float[n];
sphase = new float[n]; sbright = new float[n];
Random r = new Random(0xBEEF);
for (int i = 0; i < n; i++) {
double u = r.nextDouble() * 2 - 1;
double th = r.nextDouble() * Math.PI * 2;
double r2 = Math.sqrt(1 - u * u);
sx[i] = (float) (r2 * Math.cos(th));
sy[i] = (float) u;
sz[i] = (float) (r2 * Math.sin(th));
sphase[i] = r.nextFloat() * 6.283f;
sbright[i] = 0.35f + r.nextFloat() * 0.65f;
}
}

private void drawStars(MatrixStack s, float time) {
int n = starCount.getI();
if (n <= 0) return;
buildStars(n);
RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
RenderSystem.setShaderTexture(0, Tex.STAR);
RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
Matrix4f m = s.peek().getPositionMatrix();
BufferBuilder b = Tessellator.getInstance().getBuffer();
b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
for (int i = 0; i < n; i++) {
float tw = 1f;
if (twinkle.get() > 0f) tw = 0.5f + 0.5f * (float) Math.sin(time * twinkle.get() + sphase[i]);
int alpha = (int) (255 * MathHelper.clamp(tw, 0f, 1f) * sbright[i]);
float size = starSize.get() * (0.7f + sbright[i] * 0.7f);
billboard(b, m, sx[i] * 98f, sy[i] * 98f, sz[i] * 98f, size, Colors.alpha(0xFFFFFFFF, alpha));
}
Tessellator.getInstance().draw();
RenderSystem.defaultBlendFunc();
}

private void drawShootingStars(MatrixStack s, float time) {
if (shootX == null) {
int n = 5;
shootX = new float[n]; shootY = new float[n]; shootZ = new float[n]; shootOff = new float[n];
for (int i = 0; i < n; i++) {
shootX[i] = rnd.nextFloat() * 2 - 1;
shootY[i] = 0.35f + rnd.nextFloat() * 0.6f;
shootZ[i] = rnd.nextFloat() * 2 - 1;
shootOff[i] = rnd.nextFloat() * 7f;
}
}
RenderSystem.setShader(GameRenderer::getPositionColorProgram);
RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
Matrix4f m = s.peek().getPositionMatrix();
BufferBuilder b = Tessellator.getInstance().getBuffer();
b.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
for (int i = 0; i < shootX.length; i++) {
float t = ((time + shootOff[i]) % 7f) / 7f;
if (t > 0.35f) continue;
float f = t / 0.35f;
float fade = 1f - f;
float x = shootX[i] * 90f + f * 70f;
float y = shootY[i] * 90f + f * 10f;
float z = shootZ[i] * 90f - f * 60f;
int head = Colors.alpha(0xFFFFFFFF, (int) (230 * fade));
int tail = Colors.alpha(0xFFFFFFFF, 0);
vc(b, m, x, y, z, head);
vc(b, m, x - 14f * fade, y - 2f * fade, z + 12f * fade, tail);
}
Tessellator.getInstance().draw();
RenderSystem.defaultBlendFunc();
}

private void drawSunMoon(MatrixStack s, float tickDelta) {
float angle = mc.world.getSkyAngle(tickDelta) * 360f;
RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
s.push();
s.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
s.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle));
Matrix4f m = s.peek().getPositionMatrix();
BufferBuilder b = Tessellator.getInstance().getBuffer();

RenderSystem.setShaderTexture(0, SUN);
b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
float o = 30f;
vt(b, m, -o, 100f, -o, 0f, 0f, 0xFFFFFFFF);
vt(b, m, o, 100f, -o, 1f, 0f, 0xFFFFFFFF);
vt(b, m, o, 100f, o, 1f, 1f, 0xFFFFFFFF);
vt(b, m, -o, 100f, o, 0f, 1f, 0xFFFFFFFF);
Tessellator.getInstance().draw();

RenderSystem.setShaderTexture(0, MOON);
int phase = mc.world.getMoonPhase();
float u0 = (phase % 4) / 4f, v0 = (phase / 4) / 2f;
b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
float mo = 20f;
vt(b, m, -mo, -100f, -mo, u0, v0, 0xFFFFFFFF);
vt(b, m, mo, -100f, -mo, u0 + 0.25f, v0, 0xFFFFFFFF);
vt(b, m, mo, -100f, mo, u0 + 0.25f, v0 + 0.5f, 0xFFFFFFFF);
vt(b, m, -mo, -100f, mo, u0, v0 + 0.5f, 0xFFFFFFFF);
Tessellator.getInstance().draw();

s.pop();
}
}