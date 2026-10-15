package com.lovenote.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class RenderUtil {
	private RenderUtil() {}

	// ---------------------------------------------------------------- 3D

	public static void begin3D(float lineWidth) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.lineWidth(lineWidth);
	}

	public static void end3D() {
		RenderSystem.lineWidth(1f);
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void v(BufferBuilder b, Matrix4f m, double x, double y, double z, int c) {
		b.vertex(m, (float) x, (float) y, (float) z)
				.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255)
				.next();
	}

	public static void line(MatrixStack s, Vec3d a, Vec3d b, int c) {
		Matrix4f m = s.peek().getPositionMatrix();
		Tessellator t = Tessellator.getInstance();
		BufferBuilder buf = t.getBuffer();
		buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
		v(buf, m, a.x, a.y, a.z, c);
		v(buf, m, b.x, b.y, b.z, c);
		t.draw();
	}

	/** Pass fill = 0 or line = 0 to skip that part. */
	public static void box(MatrixStack s, Box b, int fill, int line) {
		Matrix4f m = s.peek().getPositionMatrix();
		double x1 = b.minX, y1 = b.minY, z1 = b.minZ, x2 = b.maxX, y2 = b.maxY, z2 = b.maxZ;
		Tessellator t = Tessellator.getInstance();
		BufferBuilder buf = t.getBuffer();

		if ((fill >>> 24) != 0) {
			buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			// bottom
			v(buf, m, x1, y1, z1, fill); v(buf, m, x2, y1, z1, fill); v(buf, m, x2, y1, z2, fill); v(buf, m, x1, y1, z2, fill);
			// top
			v(buf, m, x1, y2, z1, fill); v(buf, m, x1, y2, z2, fill); v(buf, m, x2, y2, z2, fill); v(buf, m, x2, y2, z1, fill);
			// north
			v(buf, m, x1, y1, z1, fill); v(buf, m, x1, y2, z1, fill); v(buf, m, x2, y2, z1, fill); v(buf, m, x2, y1, z1, fill);
			// south
			v(buf, m, x1, y1, z2, fill); v(buf, m, x2, y1, z2, fill); v(buf, m, x2, y2, z2, fill); v(buf, m, x1, y2, z2, fill);
			// west
			v(buf, m, x1, y1, z1, fill); v(buf, m, x1, y1, z2, fill); v(buf, m, x1, y2, z2, fill); v(buf, m, x1, y2, z1, fill);
			// east
			v(buf, m, x2, y1, z1, fill); v(buf, m, x2, y2, z1, fill); v(buf, m, x2, y2, z2, fill); v(buf, m, x2, y1, z2, fill);
			t.draw();
		}

		if ((line >>> 24) != 0) {
			buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
			// bottom ring
			v(buf, m, x1, y1, z1, line); v(buf, m, x2, y1, z1, line);
			v(buf, m, x2, y1, z1, line); v(buf, m, x2, y1, z2, line);
			v(buf, m, x2, y1, z2, line); v(buf, m, x1, y1, z2, line);
			v(buf, m, x1, y1, z2, line); v(buf, m, x1, y1, z1, line);
			// top ring
			v(buf, m, x1, y2, z1, line); v(buf, m, x2, y2, z1, line);
			v(buf, m, x2, y2, z1, line); v(buf, m, x2, y2, z2, line);
			v(buf, m, x2, y2, z2, line); v(buf, m, x1, y2, z2, line);
			v(buf, m, x1, y2, z2, line); v(buf, m, x1, y2, z1, line);
			// pillars
			v(buf, m, x1, y1, z1, line); v(buf, m, x1, y2, z1, line);
			v(buf, m, x2, y1, z1, line); v(buf, m, x2, y2, z1, line);
			v(buf, m, x2, y1, z2, line); v(buf, m, x2, y2, z2, line);
			v(buf, m, x1, y1, z2, line); v(buf, m, x1, y2, z2, line);
			t.draw();
		}
	}

	public static void ring(MatrixStack s, Vec3d c, double radius, int color, int segments) {
		Matrix4f m = s.peek().getPositionMatrix();
		Tessellator t = Tessellator.getInstance();
		BufferBuilder buf = t.getBuffer();
		buf.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= segments; i++) {
			double a = (Math.PI * 2.0 * i) / segments;
			v(buf, m, c.x + Math.cos(a) * radius, c.y, c.z + Math.sin(a) * radius, color);
		}
		t.draw();
	}

	// ---------------------------------------------------------------- 2D

	public static void gradientH(DrawContext ctx, int x1, int y1, int x2, int y2, int c1, int c2) {
		if (x2 <= x1 || y2 <= y1) return;
		Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		v(b, m, x1, y1, 0, c1);
		v(b, m, x1, y2, 0, c1);
		v(b, m, x2, y2, 0, c2);
		v(b, m, x2, y1, 0, c2);
		t.draw();
		RenderSystem.disableBlend();
	}

	/** Skeet's blue -> purple -> yellow bar. */
	public static void skeetBar(DrawContext ctx, int x, int y, int w, int h) {
		int mid = x + w / 2;
		gradientH(ctx, x, y, mid, y + h, Colors.C1, Colors.C2);
		gradientH(ctx, mid, y, x + w, y + h, Colors.C2, Colors.C3);
	}

	/** Rounded rectangle (2px corner radius), drawn without overlaps so alpha stays even. */
	/** Rounded rectangle with a custom radius. Drawn without overlaps so alpha stays even. */
	public static void rrect(DrawContext ctx, int x, int y, int w, int h, int radius, int c) {
		if (w <= 0 || h <= 0) return;
		if ((c >>> 24) == 0) return;
		int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
		if (r == 0) {
			ctx.fill(x, y, x + w, y + h, c);
			return;
		}
		ctx.fill(x, y + r, x + w, y + h - r, c);
		for (int i = 0; i < r; i++) {
			double dy = r - 1 - i;
			int inset = (int) Math.ceil(r - Math.sqrt(Math.max(0.0, (double) r * r - dy * dy)));
			ctx.fill(x + inset, y + i, x + w - inset, y + i + 1, c);
			ctx.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, c);
		}
	}
}
