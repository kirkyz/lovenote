package com.lovenote.util;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * 1.21.11 render shim.
 *
 * RenderSystem no longer holds ANY gl state - blend, depth test, cull, shader and
 * texture all come from the RenderLayer used for the draw call now.
 * This class picks the right layer and keeps the Tessellator plumbing in one place.
 */
public final class Gl {

    private Gl() {}

    /** Untextured coloured quads (POSITION_COLOR). */
    public static RenderLayer quads() {
        return RenderLayers.debugQuads();
    }

    /** Lines (POSITION_COLOR_NORMAL) - write a normal on every vertex. */
    public static RenderLayer lines() {
        return RenderLayers.lines();
    }

    /** Translucent textured quads (POSITION_COLOR_TEXTURE), depth test off. */
    public static RenderLayer tex(Identifier texture) {
        return RenderLayers.blockScreenEffect(texture);
    }

    /** Textured quads that are depth tested (glows hidden behind terrain). */
    public static RenderLayer texDepth(Identifier texture) {
        return RenderLayers.entityTranslucentEmissive(texture);
    }

    public static BufferBuilder begin(RenderLayer layer) {
        return Tessellator.getInstance().begin(layer.getDrawMode(), layer.getVertexFormat());
    }

    public static void draw(RenderLayer layer, BufferBuilder buffer) {
        BuiltBuffer built = buffer.end();
        if (built != null) {
            layer.draw(built);
        }
    }

    /** ClientWorld.getSkyAngle / getSkyAngleRadians were both removed. */
    public static float skyAngle(World world, float tickDelta) {
        if (world == null) return 0.0f;
        double d = MathHelper.fractionalPart(world.getTimeOfDay() / 24000.0D);
        double e = 0.25D - Math.cos(d * Math.PI * 2.0D) / 2.0D;
        return (float) ((d * 2.0D + e) / 3.0D);
    }

    public static float skyAngleRadians(World world, float tickDelta) {
        return skyAngle(world, tickDelta) * (float) (Math.PI * 2.0D);
    }
}
