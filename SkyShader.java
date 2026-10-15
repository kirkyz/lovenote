
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

/** Replaces the vanilla sky with a configurable sky. Driven by WorldRendererMixin. */
public class SkyShader extends Module {

    private static final Identifier SUN =
            new Identifier("minecraft", "textures/environment/sun.png");
    private static final Identifier MOON =
            new Identifier("minecraft", "textures/environment/moon_phases.png");

    private static final int SEGS = 28;

    // Preset -> {top, horizon, nebulaA, nebulaB, aurora}
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

    public final ModeSetting worlds = add(
            new ModeSetting("Worlds", "Overworld",
                    "Overworld", "All", "Overworld + End"));

    public final ModeSetting preset = add(
            new ModeSetting("Preset", "Nebula",
                    PRESETS.keySet().toArray(new String[0])));

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

    /** Returns true when vanilla's sky should be cancelled. */
    public boolean render(
            MatrixStack matrices,
            Matrix4f projection,
            float tickDelta,
            Camera camera,
            Runnable fogCallback
    ) {
        if (!enabled || mc.world == null || camera == null) {
            return false;
        }

        switch (worlds.get()) {
            case "Overworld":
                if (!mc.world.getRegistryKey().equals(World.OVERWORLD)) {
                    return false;
                }
                break;
            case "Overworld + End":
                if (mc.world.getRegistryKey().equals(World.NETHER)) {
                    return false;
                }
                break;
            default:
                break;
        }

        Tex.ensure();

        int[] p = PRESETS.get(preset.get());
        if (p == null) {
            p = PRESETS.get("Nebula");
        }

        int top = p[0];
        int horizon = p[1];
        int nebA = p[2];
        int nebB = p[3];
        int aur = p[4];

        if (useTheme.get()) {
            nebA = Colors.C1;
            nebB = Colors.C3;
            aur = Colors.C2;
            top = Colors.lerp(top, Colors.C3, 0.35f);
            horizon = Colors.lerp(horizon, Colors.C2, 0.35f);
        }

        if (daylight.get()) {
            Vec3d sky = mc.world.getSkyColor(camera.getPos(), tickDelta);

            int skyColor = 0xFF000000
                    | ((int) (MathHelper.clamp(sky.x, 0.0, 1.0) * 255.0) << 16)
                    | ((int) (MathHelper.clamp(sky.y, 0.0, 1.0) * 255.0) << 8)
                    | (int) (MathHelper.clamp(sky.z, 0.0, 1.0) * 255.0);

            float amount = MathHelper.clamp(daylightAmount.getI(), 0, 100) / 100.0f;
            top = Colors.lerp(top, skyColor, amount);
            horizon = Colors.lerp(horizon, skyColor, amount * 0.85f);
        }

        float time = (System.currentTimeMillis() % 600000L) / 1000.0f;

        right.set(1.0f, 0.0f, 0.0f);
        up.set(0.0f, 1.0f, 0.0f);
        camera.getRotation().transform(right);
        camera.getRotation().transform(up);

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        try {
            if (fogCallback != null) {
                fogCallback.run();

                if (!blendFog.get()) {
                    RenderSystem.setShaderFogStart(2048.0f);
                    RenderSystem.setShaderFogEnd(4096.0f);
                }
            }

            drawDome(matrices, top, horizon);

            if (horizonGlow.get()) {
                drawHorizonGlow(matrices, horizon);
            }
            if (nebula.get()) {
                drawNebula(matrices, nebA, nebB, time);
            }
            if (aurora.get()) {
                drawAurora(matrices, aur, time);
            }
            if (stars.get()) {
                drawStars(matrices, time);
            }
            if (shooting.get()) {
                drawShootingStars(matrices, time);
            }
            if (sunMoon.get()) {
                drawSunMoon(matrices, tickDelta);
            }

            return true;
        } finally {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }
    }

    // Geometry helpers

    private static void vc(
            BufferBuilder builder, Matrix4f matrix,
            float x, float y, float z, int color
    ) {
        builder.vertex(matrix, x, y, z)
                .color((color >> 16) & 255, (color >> 8) & 255,
                        color & 255, (color >>> 24) & 255)
                .next();
    }

    private static void vt(
            BufferBuilder builder, Matrix4f matrix,
            float x, float y, float z, float u, float v, int color
    ) {
        builder.vertex(matrix, x, y, z)
                .texture(u, v)
                .color((color >> 16) & 255, (color >> 8) & 255,
                        color & 255, (color >>> 24) & 255)
                .next();
    }

    private void billboard(
            BufferBuilder builder, Matrix4f matrix,
            float x, float y, float z, float size, int color
    ) {
        float h = size * 0.5f;

        float rx = right.x * h;
        float ry = right.y * h;
        float rz = right.z * h;

        float ux = up.x * h;
        float uy = up.y * h;
        float uz = up.z * h;

        vt(builder, matrix, x - rx - ux, y - ry - uy, z - rz - uz, 0, 1, color);
        vt(builder, matrix, x + rx - ux, y + ry - uy, z + rz - uz, 1, 1, color);
        vt(builder, matrix, x + rx + ux, y + ry + uy, z + rz + uz, 1, 0, color);
        vt(builder, matrix, x - rx + ux, y - ry + uy, z - rz + uz, 0, 0, color);
    }

    private void drawDome(MatrixStack stack, int top, int horizon) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        builder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        vc(builder, matrix, 0, 100, 0, top);

        for (int i = 0; i <= SEGS; i++) {
            double angle = (Math.PI * 2.0 * i) / SEGS;
            vc(builder, matrix,
                    (float) (Math.cos(angle) * 100.0),
                    0,
                    (float) (Math.sin(angle) * 100.0),
                    horizon);
        }

        Tessellator.getInstance().draw();

        int floor = Colors.lerp(horizon, 0xFF000000, 0.62f);

        builder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        vc(builder, matrix, 0, -100, 0, floor);

        for (int i = 0; i <= SEGS; i++) {
            double angle = (Math.PI * 2.0 * i) / SEGS;
            vc(builder, matrix,
                    (float) (Math.cos(angle) * 100.0),
                    0,
                    (float) (Math.sin(angle) * 100.0),
                    horizon);
        }

        Tessellator.getInstance().draw();
    }

    private void drawHorizonGlow(MatrixStack stack, int horizon) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        builder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int color = Colors.alpha(horizon, 150);
        int clear = Colors.alpha(horizon, 0);

        for (int i = 0; i <= SEGS; i++) {
            double angle = (Math.PI * 2.0 * i) / SEGS;
            float x = (float) (Math.cos(angle) * 100.0);
            float z = (float) (Math.sin(angle) * 100.0);

            vc(builder, matrix, x, 0, z, color);
            vc(builder, matrix, x, 26, z, clear);
        }

        Tessellator.getInstance().draw();
        RenderSystem.defaultBlendFunc();
    }

    private void drawNebula(MatrixStack stack, int colorA, int colorB, float time) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, Tex.GLOW);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int i = 0; i < 14; i++) {
            float angle = i * 0.4488f + time * 0.012f;
            float elevation = ((i % 5) - 2) * 0.30f;
            float radius = 88.0f;

            float x = (float) (Math.cos(angle) * Math.cos(elevation)) * radius;
            float y = (float) Math.sin(elevation) * radius * 0.85f + 12.0f;
            float z = (float) (Math.sin(angle) * Math.cos(elevation)) * radius;

            float size = 60.0f + (i % 4) * 26.0f;
            int color = (i % 2 == 0) ? colorA : colorB;

            int alpha = (int) (nebulaAlpha.getI() * (0.45f + (i % 3) * 0.28f));
            alpha = MathHelper.clamp(alpha, 0, 255);

            billboard(builder, matrix, x, y, z, size, Colors.alpha(color, alpha));
        }

        Tessellator.getInstance().draw();
        RenderSystem.defaultBlendFunc();
    }

    private void drawAurora(MatrixStack stack, int color, float time) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        for (int c = 0; c < 3; c++) {
            float base = c * 2.1f + time * 0.03f;
            float radius = 84.0f + c * 6.0f;
            float span = 1.9f;

            builder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            for (int i = 0; i <= 48; i++) {
                float f = i / 48.0f;
                float angle = base + (f - 0.5f) * span;

                float x = (float) Math.cos(angle) * radius;
                float z = (float) Math.sin(angle) * radius;
                float height = 26.0f
                        + (float) Math.sin(f * 7.0f + time * 0.7f + c * 2.0f) * 10.0f;

                float edge = (float) Math.sin(f * Math.PI);

                vc(builder, matrix, x, 2, z,
                        Colors.alpha(color, (int) (150 * edge)));
                vc(builder, matrix, x * 0.98f, height, z * 0.98f,
                        Colors.alpha(color, (int) (16 * edge)));
            }

            Tessellator.getInstance().draw();
        }

        RenderSystem.defaultBlendFunc();
    }

    private void buildStars(int count) {
        if (sx != null && sx.length == count && starSeed == count) {
            return;
        }

        starSeed = count;

        sx = new float[count];
        sy = new float[count];
        sz = new float[count];
        sphase = new float[count];
        sbright = new float[count];

        Random random = new Random(0xBEEF);

        for (int i = 0; i < count; i++) {
            double u = random.nextDouble() * 2.0 - 1.0;
            double theta = random.nextDouble() * Math.PI * 2.0;
            double radius = Math.sqrt(1.0 - u * u);

            sx[i] = (float) (radius * Math.cos(theta));
            sy[i] = (float) u;
            sz[i] = (float) (radius * Math.sin(theta));

            sphase[i] = random.nextFloat() * 6.283f;
            sbright[i] = 0.35f + random.nextFloat() * 0.65f;
        }
    }

    private void drawStars(MatrixStack stack, float time) {
        int count = MathHelper.clamp(starCount.getI(), 0, 1500);

        if (count <= 0) {
            return;
        }

        buildStars(count);

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, Tex.STAR);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int i = 0; i < count; i++) {
            float twinkleAmount = 1.0f;

            if (twinkle.get() > 0.0f) {
                twinkleAmount = 0.5f + 0.5f
                        * (float) Math.sin(time * twinkle.get() + sphase[i]);
            }

            int alpha = (int) (
                    255.0f
                            * MathHelper.clamp(twinkleAmount, 0.0f, 1.0f)
                            * sbright[i]
            );

            float size = (float) (
                    starSize.get() * (0.7f + sbright[i] * 0.7f)
            );

            billboard(builder, matrix,
                    sx[i] * 98.0f,
                    sy[i] * 98.0f,
                    sz[i] * 98.0f,
                    size,
                    Colors.alpha(0xFFFFFFFF, alpha));
        }

        Tessellator.getInstance().draw();
        RenderSystem.defaultBlendFunc();
    }

    private void drawShootingStars(MatrixStack stack, float time) {
        if (shootX == null) {
            int count = 5;

            shootX = new float[count];
            shootY = new float[count];
            shootZ = new float[count];
            shootOff = new float[count];

            for (int i = 0; i < count; i++) {
                shootX[i] = rnd.nextFloat() * 2.0f - 1.0f;
                shootY[i] = 0.35f + rnd.nextFloat() * 0.6f;
                shootZ[i] = rnd.nextFloat() * 2.0f - 1.0f;
                shootOff[i] = rnd.nextFloat() * 7.0f;
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        builder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < shootX.length; i++) {
            float t = ((time + shootOff[i]) % 7.0f) / 7.0f;

            if (t > 0.35f) {
                continue;
            }

            float f = t / 0.35f;
            float fade = 1.0f - f;

            float x = shootX[i] * 90.0f + f * 70.0f;
            float y = shootY[i] * 90.0f + f * 10.0f;
            float z = shootZ[i] * 90.0f - f * 60.0f;

            int head = Colors.alpha(0xFFFFFFFF, (int) (230 * fade));
            int tail = Colors.alpha(0xFFFFFFFF, 0);

            vc(builder, matrix, x, y, z, head);
            vc(builder, matrix,
                    x - 14.0f * fade,
                    y - 2.0f * fade,
                    z + 12.0f * fade,
                    tail);
        }

        Tessellator.getInstance().draw();
        RenderSystem.defaultBlendFunc();
    }

    private void drawSunMoon(MatrixStack stack, float tickDelta) {
        float angle = mc.world.getSkyAngle(tickDelta) * 360.0f;

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);

        stack.push();

        try {
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle));

            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder builder = Tessellator.getInstance().getBuffer();

            RenderSystem.setShaderTexture(0, SUN);

            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            float size = 30.0f;

            vt(builder, matrix, -size, 100, -size, 0, 0, 0xFFFFFFFF);
            vt(builder, matrix, size, 100, -size, 1, 0, 0xFFFFFFFF);
            vt(builder, matrix, size, 100, size, 1, 1, 0xFFFFFFFF);
            vt(builder, matrix, -size, 100, size, 0, 1, 0xFFFFFFFF);

            Tessellator.getInstance().draw();

            RenderSystem.setShaderTexture(0, MOON);

            int phase = mc.world.getMoonPhase();
            float u0 = (phase % 4) / 4.0f;
            float v0 = (phase / 4) / 2.0f;

            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            float moonSize = 20.0f;

            vt(builder, matrix, -moonSize, -100, -moonSize, u0, v0, 0xFFFFFFFF);
            vt(builder, matrix, moonSize, -100, -moonSize, u0 + 0.25f, v0, 0xFFFFFFFF);
            vt(builder, matrix, moonSize, -100, moonSize, u0 + 0.25f, v0 + 0.5f, 0xFFFFFFFF);
            vt(builder, matrix, -moonSize, -100, moonSize, u0, v0 + 0.5f, 0xFFFFFFFF);

            Tessellator.getInstance().draw();
        } finally {
            stack.pop();
        }
    }
}
