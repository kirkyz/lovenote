
package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.Tex;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class SkyShader extends Module {

    private static final int SEGS = 64;

    private final BoolSetting planet =
            add(new BoolSetting("Planet", true));

    private final BoolSetting clouds =
            add(new BoolSetting("Clouds", true));

    private final BoolSetting lightning =
            add(new BoolSetting("Lightning", true));

    private final NumberSetting planetSize =
            add(new NumberSetting("Planet Size", 18, 5, 40, 1));

    private final NumberSetting cloudAlpha =
            add(new NumberSetting("Cloud Brightness", 150, 0, 255, 5));

    private final NumberSetting lightningRate =
            add(new NumberSetting("Lightning Rate", 3, 1, 10, 1));

    public SkyShader() {
        super("SkyShader", Category.VISUALS);
    }

    @Override
    public void onRender3D(WorldRenderContext ctx, MatrixStack matrices) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        Tex.ensure();

        float tickDelta = ctx.tickDelta();
        float time = mc.world.getTime() + tickDelta;

        matrices.push();

        if (planet.get()) {
            drawPlanet(matrices, tickDelta);
        }

        if (clouds.get()) {
            drawClouds(matrices, time);
        }

        if (lightning.get()) {
            drawLightning(matrices, time);
        }

        matrices.pop();

        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(1f);
    }

    private void drawPlanet(MatrixStack s, float tickDelta) {
        float angle = mc.world.getSkyAngle(tickDelta) * 360f;

        RenderSystem.enableBlend();
        RenderSystem.setShader(
                GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, Tex.SOFT);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        s.push();
        s.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
        s.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(
                        angle * 0.35f + 25f));

        Matrix4f m = s.peek().getPositionMatrix();
        float r = (float) planetSize.get();
        BufferBuilder b = Tessellator.getInstance().getBuffer();

        b.begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_TEXTURE_COLOR);

        vt(b, m, -r, 90f, 0f, 0f, 0f, 0xFFB08CFF);
        vt(b, m, r, 90f, 0f, 1f, 0f, 0xFFB08CFF);
        vt(b, m, r, 90f + r * 2f, 0f, 1f, 1f, 0xFF3A1F6B);
        vt(b, m, -r, 90f + r * 2f, 0f, 0f, 1f, 0xFF3A1F6B);

        Tessellator.getInstance().draw();

        RenderSystem.setShader(
                GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(2f);

        b.begin(
                VertexFormat.DrawMode.DEBUG_LINE_STRIP,
                VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= SEGS; i++) {
            double a = i * (Math.PI * 2.0 / SEGS);

            vc(
                    b, m,
                    (float) (Math.cos(a) * r * 2.1f),
                    90f + r + (float) (Math.sin(a) * r * 0.55f),
                    0f,
                    Colors.alpha(0xFFE0C8FF, 160));
        }

        Tessellator.getInstance().draw();

        RenderSystem.lineWidth(1f);
        s.pop();
        RenderSystem.defaultBlendFunc();
    }

    private void drawClouds(MatrixStack s, float time) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(
                GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, Tex.SOFT);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Matrix4f m = s.peek().getPositionMatrix();
        BufferBuilder b = Tessellator.getInstance().getBuffer();

        b.begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_TEXTURE_COLOR);

        for (int i = 0; i < 18; i++) {
            float ang = i * 0.349f + time * 0.004f;
            float rr = 78f;

            int a = (int) Math.min(
                    255f,
                    cloudAlpha.getI() *
                            (0.5f + (i % 3) * 0.3f));

            billboard(
                    b, m,
                    (float) Math.cos(ang) * rr,
                    42f + (i % 5) * 9f,
                    (float) Math.sin(ang) * rr,
                    46f + (i % 4) * 22f,
                    Colors.alpha(0xFFFFFFFF, a));
        }

        Tessellator.getInstance().draw();
        RenderSystem.defaultBlendFunc();
    }

    private void drawLightning(MatrixStack s, float time) {
        float period = 60f / Math.max(1, lightningRate.getI());
        float t = (time % period) / period;
        float flash = t < 0.12f ? 1f - t / 0.12f : 0f;

        if (flash <= 0.01f) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShader(
                GameRenderer::getPositionColorProgram);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Matrix4f m = s.peek().getPositionMatrix();
        BufferBuilder b = Tessellator.getInstance().getBuffer();

        b.begin(
                VertexFormat.DrawMode.TRIANGLE_FAN,
                VertexFormats.POSITION_COLOR);

        int c = Colors.alpha(
                0xFFFFFF, (int) (120 * flash));

        vc(b, m, 0f, 100f, 0f, c);

        for (int i = 0; i <= SEGS; i++) {
            double a = i * (Math.PI * 2.0 / SEGS);

            vc(
                    b, m,
                    (float) (Math.cos(a) * 100),
                    0f,
                    (float) (Math.sin(a) * 100),
                    Colors.alpha(0xFFFFFF, 0));
        }

        Tessellator.getInstance().draw();
        RenderSystem.defaultBlendFunc();
    }

    private static void vt(
            BufferBuilder b,
            Matrix4f m,
            float x, float y, float z,
            float u, float v,
            int color) {
        b.vertex(m, x, y, z)
                .texture(u, v)
                .color(color);
    }

    private static void vc(
            BufferBuilder b,
            Matrix4f m,
            float x, float y, float z,
            int color) {
        b.vertex(m, x, y, z).color(color);
    }

    private static void billboard(
            BufferBuilder b,
            Matrix4f m,
            float x, float y, float z,
            float size,
            int color) {
        float h = size / 2f;

        b.vertex(m, x - h, y - h, z)
                .texture(0f, 0f).color(color);
        b.vertex(m, x + h, y - h, z)
                .texture(1f, 0f).color(color);
        b.vertex(m, x + h, y + h, z)
                .texture(1f, 1f).color(color);
        b.vertex(m, x - h, y + h, z)
                .texture(0f, 1f).color(color);
    }
}
