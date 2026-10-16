package com.lovenote.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

/** Shared world-space effect drawing: camera basis, additive blending, vertex emitters. */
public final class Fx {
	private Fx() {}

	public static final Vector3f RIGHT = new Vector3f();
	public static final Vector3f UP = new Vector3f();

	/** Refresh the camera basis so billboards always face the viewer. */
	public static void cameraBasis(Camera cam) {
		RIGHT.set(1, 0, 0);
		UP.set(0, 1, 0);
		cam.getRotation().transform(RIGHT);
		cam.getRotation().transform(UP);
	}

	/** Additive glow pass. Call Fx.end() when finished. */
	public static void additive() {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
	}

	public static void end() {
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.defaultBlendFunc();
		// hand the plain colour program back so the next module's geometry batch is valid
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
	}

	public static void vc(BufferBuilder b, Matrix4f m, float x, float y, float z, int c) {
		b.vertex(m, x, y, z)
			.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255)
			.next();
	}

	public static void vt(BufferBuilder b, Matrix4f m, float x, float y, float z, float u, float v, int c) {
		b.vertex(m, x, y, z)
			.texture(u, v)
			.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255)
			.next();
	}

	/** Camera-facing quad, centred on (x,y,z). */
	public static void billboard(BufferBuilder b, Matrix4f m, float x, float y, float z, float size, int color) {
		float h = size * 0.5f;
		float rx = RIGHT.x * h, ry = RIGHT.y * h, rz = RIGHT.z * h;
		float ux = UP.x * h, uy = UP.y * h, uz = UP.z * h;
		vt(b, m, x - rx - ux, y - ry - uy, z - rz - uz, 0f, 1f, color);
		vt(b, m, x + rx - ux, y + ry - uy, z + rz - uz, 1f, 1f, color);
		vt(b, m, x + rx + ux, y + ry + uy, z + rz + uz, 1f, 0f, color);
		vt(b, m, x - rx + ux, y - ry + uy, z - rz + uz, 0f, 0f, color);
	}

	/** Open a textured quad batch. Remember to call Tessellator.getInstance().draw(). */
	public static BufferBuilder texQuads() {
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		BufferBuilder b = Tessellator.getInstance().getBuffer();
		b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		return b;
	}
}
