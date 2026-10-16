package com.lovenote.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

/** Runtime-generated textures. Nothing needs to ship in assets/. */
public final class Tex {
    public static final Identifier GLOW =
            Identifier.of("lovenote", "gen/glow");
    public static final Identifier SOFT =
            Identifier.of("lovenote", "gen/soft");
    public static final Identifier STAR =
            Identifier.of("lovenote", "gen/star");
    public static final Identifier VIGNETTE =
            Identifier.of("lovenote", "gen/vignette");

    private static boolean built;

    private Tex() {}

    public static void ensure() {
        if (built) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        try {
            RenderSystem.assertOnRenderThread();

            put(mc, GLOW, radial(96, 1.05f));
            put(mc, SOFT, radial(96, 2.30f));
            put(mc, STAR, sparkle(96));
            put(mc, VIGNETTE, vignette(256));

            built = true;
        } catch (Throwable t) {
            built = false;
            t.printStackTrace();
        }
    }

    private static void put(
            MinecraftClient mc,
            Identifier id,
            NativeImage img
    ) {
        NativeImageBackedTexture tex =
                new NativeImageBackedTexture(() -> "lovenote_gen", img);
        mc.getTextureManager().registerTexture(id, tex);
    }

    /** 0xAABBGGRR, matching NativeImage's channel order. */
    private static int px(int a, int r, int g, int b) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static NativeImage radial(int size, float power) {
        NativeImage img = new NativeImage(size, size, false);
        float c = (size - 1) / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - c) / c;
                float dy = (y - c) / c;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float a = Math.max(0f, 1f - d);
                a = (float) Math.pow(a, power);

                img.setColor(
                        x, y,
                        px((int) (a * 255f), 255, 255, 255)
                );
            }
        }

        return img;
    }

    private static NativeImage sparkle(int size) {
        NativeImage img = new NativeImage(size, size, false);
        float c = (size - 1) / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - c) / c;
                float dy = (y - c) / c;
                float d = (float) Math.sqrt(dx * dx + dy * dy);

                float ray = Math.max(
                        0f,
                        1f - Math.min(
                                Math.abs(dx) * 12f,
                                Math.abs(dy) * 12f
                        )
                );

                float core = Math.max(0f, 1f - d * 5f);
                float a = Math.max(ray * 0.65f, core);

                img.setColor(
                        x, y,
                        px((int) (a * 255f), 255, 255, 255)
                );
            }
        }

        return img;
    }

    private static NativeImage vignette(int size) {
        NativeImage img = new NativeImage(size, size, false);
        float c = (size - 1) / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - c) / c;
                float dy = (y - c) / c;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float a = Math.min(1f, d * d * 0.8f);

                img.setColor(
                        x, y,
                        px((int) (a * 180f), 0, 0, 0)
                );
            }
        }

        return img;
    }
}
