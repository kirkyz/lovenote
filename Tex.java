
package com.lovenote.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

/**
 * Runtime-generated textures.
 * No texture assets need to be included in the resources folder.
 */
public final class Tex {

    public static final Identifier GLOW = new Identifier("lovenote", "gen/glow");
    public static final Identifier SOFT = new Identifier("lovenote", "gen/soft");
    public static final Identifier STAR = new Identifier("lovenote", "gen/star");

    private static boolean built;

    private Tex() {
    }

    public static void ensure() {
        if (built) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return;
        }

        RenderSystem.assertOnRenderThreadOrInit();

        try {
            put(mc, GLOW, radial(96, 1.05f));
            put(mc, SOFT, radial(96, 2.30f));
            put(mc, STAR, sparkle(96));
            built = true;
        } catch (RuntimeException e) {
            built = false;
            e.printStackTrace();
        }
    }

    private static void put(
            MinecraftClient mc,
            Identifier id,
            NativeImage image
    ) {
        NativeImageBackedTexture texture =
                new NativeImageBackedTexture(image);

        texture.setFilter(true, false);
        mc.getTextureManager().registerTexture(id, texture);
    }

    /**
     * NativeImage pixel layout: 0xAABBGGRR.
     */
    private static int px(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24)
                | ((b & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (r & 0xFF);
    }

    private static NativeImage radial(int size, float power) {
        NativeImage image = new NativeImage(size, size, false);
        float center = (size - 1) / 2.0f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - center) / center;
                float dy = (y - center) / center;

                float distance =
                        (float) Math.sqrt(dx * dx + dy * dy);

                float alpha = Math.max(0.0f, 1.0f - distance);
                alpha = (float) Math.pow(alpha, power);

                image.setColor(
                        x,
                        y,
                        px((int) (alpha * 255.0f), 255, 255, 255)
                );
            }
        }

        return image;
    }

    private static NativeImage sparkle(int size) {
        NativeImage image = new NativeImage(size, size, false);
        float center = (size - 1) / 2.0f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = Math.abs((x - center) / center);
                float dy = Math.abs((y - center) / center);

                float distance =
                        (float) Math.sqrt(dx * dx + dy * dy);

                float core = Math.max(0.0f, 1.0f - distance * 3.4f);

                float armX =
                        (float) Math.pow(Math.max(0.0f, 1.0f - dx), 7.0f)
                        * Math.max(0.0f, 1.0f - dy * 5.0f);

                float armY =
                        (float) Math.pow(Math.max(0.0f, 1.0f - dy), 7.0f)
                        * Math.max(0.0f, 1.0f - dx * 5.0f);

                float alpha = Math.min(1.0f, core + armX + armY);

                image.setColor(
                        x,
                        y,
                        px((int) (alpha * 255.0f), 255, 255, 255)
                );
            }
        }

        return image;
    }
}
