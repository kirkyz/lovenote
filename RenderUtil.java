package com.lovenote.util;
import net.minecraft.client.render.RenderLayers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class RenderUtil {
	private RenderUtil() {}

	private static VertexConsumerProvider.Immediate provider() {
		return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
	}

	// ---------------------------------------------------------------- 3D

	public static void begin3D(float lineWidth) {
		// blend / depth / cull state now lives in the RenderLayer
	}

	public static void end3D() {
	}

	private static void v(VertexConsumer vc, Matrix4f m, double x, double y, double z, int c) {
		vc.vertex(m, (float) x, (float) y, (float) z).color(c);
	}

	public static void line(MatrixStack s, Vec3d a, Vec3d b, int c) {
		Matrix4f m = s.peek().getPositionMatrix();
		VertexConsumer vc = provider().getBuffer(RenderLayers.lines());
		v(vc, m, a.x, a.y, a.z, c);
		v(vc, m, b.x, b.y, b.z, c);
		provider().draw();
	}

	public static void box(MatrixStack s, Box b, int fill, int line) {
		Matrix4f m = s.peek().getPositionMatrix();
		double x1 = b.minX, y1 = b.minY, z1 = b.minZ, x2 = b.maxX, y2 = b.maxY, z2 = b.maxZ;

		if ((fill >>> 24) != 0) {
			VertexConsumer vc = provider().getBuffer(RenderLayers.debugQuads());
			v(vc, m, x1, y1, z1, fill); v(vc, m, x2, y1, z1, fill); v(vc, m, x2, y1, z2, fill); v(vc, m, x1, y1, z2, fill);
			v(vc, m, x1, y2, z1, fill); v(vc, m, x1, y2, z2, fill); v(vc, m, x2, y2, z2, fill); v(vc, m, x2, y2, z1, fill);
			v(vc, m, x1, y1, z1, fill); v(vc, m, x1, y2, z1, fill); v(vc, m, x2, y2, z1, fill); v(vc, m, x2, y1, z1, fill);
			v(vc, m, x1, y1, z2, fill); v(vc, m, x2, y1, z2, fill); v(vc, m, x2, y2, z2, fill); v(vc, m, x1, y2, z2, fill);
			v(vc, m, x1, y1, z1, fill); v(vc, m, x1, y1, z2, fill); v(vc, m, x1, y2, z2, fill); v(vc, m, x1, y2, z1, fill);
			v(vc, m, x2, y1, z1, fill); v(vc, m, x2, y2, z1, fill); v(vc, m, x2, y2, z2, fill); v(vc, m, x2, y1, z2, fill);
			provider().draw();
		}

		if ((line >>> 24) != 0) {
			VertexConsumer vc = provider().getBuffer(RenderLayers.lines());
			v(vc, m, x1, y1, z1, line); v(vc, m, x2, y1, z1, line);
			v(vc, m, x2, y1, z1, line); v(vc, m, x2, y1, z2, line);
			v(vc, m, x2, y1, z2, line); v(vc, m, x1, y1, z2, line);
			v(vc, m, x1, y1, z2, line); v(vc, m, x1, y1, z1, line);
			v(vc, m, x1, y2, z1, line); v(vc, m, x2, y2, z1, line);
			v(vc, m, x2, y2, z1, line); v(vc, m, x2, y2, z2, line);
			v(vc, m, x2, y2, z2, line); v(vc, m, x1, y2, z2, line);
			v(vc, m, x1, y2, z2, line); v(vc, m, x1, y2, z1, line);
			v(vc, m, x1, y1, z1, line); v(vc, m, x1, y2, z1, line);
			v(vc, m, x2, y1, z1, line); v(vc, m, x2, y2, z1, line);
			v(vc, m, x2, y1, z2, line); v(vc, m, x2, y2, z2, line);
			v(vc, m, x1, y1, z2, line); v(vc, m, x1, y2, z2, line);
			provider().draw();
		}
	}

	public static void ring(MatrixStack s, Vec3d c, double radius, int color, int segments) {
		Matrix4f m = s.peek().getPositionMatrix();
		VertexConsumer vc = provider().getBuffer(RenderLayers.lines());
		double px = c.x + radius, pz = c.z;
		for (int i = 1; i <= segments; i++) {
			double a = (Math.PI * 2.0 * i) / segments;
			double nx = c.x + Math.cos(a) * radius, nz = c.z + Math.sin(a) * radius;
			v(vc, m, px, c.y, pz, color);
			v(vc, m, nx, c.y, nz, color);
			px = nx; pz = nz;
		}
		provider().draw();
	}

	// ---------------------------------------------------------------- 2D

	public static void gradientH(DrawContext ctx, int x1, int y1, int x2, int y2, int c1, int c2) {
		if (x2 <= x1 || y2 <= y1) return;
		int w = x2 - x1;
		for (int i = 0; i < w; i++) {
			float t = w == 1 ? 0f : i / (float) (w - 1);
			ctx.fill(x1 + i, y1, x1 + i + 1, y2, lerp(c1, c2, t));
		}
	}

	private static int lerp(int a, int b, float t) {
		int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
		int ar = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
		int ag = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
		int ab = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
		return (aa << 24) | (ar << 16) | (ag << 8) | ab;
	}

	public static void skeetBar(DrawContext ctx, int x, int y, int w, int h) {
		int mid = x + w / 2;
		gradientH(ctx, x, y, mid, y + h, Colors.C1, Colors.C2);
		gradientH(ctx, mid, y, x + w, y + h, Colors.C2, Colors.C3);
	}

	public static void rrect(DrawContext ctx, int x, int y, int w, int h, int c) {
		if (w < 5 || h < 5) { ctx.fill(x, y, x + w, y + h, c); return; }
		ctx.fill(x + 2, y, x + w - 2, y + h, c);
		ctx.fill(x + 1, y + 1, x + 2, y + h - 1, c);
		ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, c);
		ctx.fill(x, y + 2, x + 1, y + h - 2, c);
		ctx.fill(x + w - 1, y + 2, x + w, y + h - 2, c);
	}
}
