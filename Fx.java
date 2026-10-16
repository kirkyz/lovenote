package com.lovenote.util;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Shared world-space effect drawing: camera basis, layer selection, vertex emitters. */
public final class Fx {
	private Fx() {}

	public static final Vector3f RIGHT = new Vector3f();
	public static final Vector3f UP = new Vector3f();

	private static final ThreadLocal<RenderLayer> LAYER = new ThreadLocal<>();
	private static final ThreadLocal<Identifier> TEXTURE = new ThreadLocal<>();
	private static final ThreadLocal<BufferBuilder> BUFFER = new ThreadLocal<>();

	/** Refresh the camera basis so billboards always face the viewer. */
	public static void cameraBasis(Camera cam) {
		RIGHT.set(1, 0, 0);
		UP.set(0, 1, 0);
		cam.getRotation().transform(RIGHT);
		cam.getRotation().transform(UP);
	}

	/** No-op now: 1.21.11 has no RenderSystem blend state. */
	public static void additive() { }

	/** No-op now, same reason. */
	public static void end() { }

	public static void vc(BufferBuilder b, Matrix4f m, float x, float y, float z, int c) {
		b.vertex(m, x, y, z)
				.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255);
	}

	public static void vt(BufferBuilder b, Matrix4f m, float x, float y, float z, float u, float v, int c) {
		b.vertex(m, x, y, z)
				.texture(u, v)
				.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255);
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

	/** Remember the texture for the next texQuads() call. */
	public static void texture(Identifier tex) { TEXTURE.set(tex); }

	/** Open a textured quad batch using the texture last set with Fx.texture(). */
	public static BufferBuilder texQuads() {
		Identifier tex = TEXTURE.get();
		return begin(RenderLayers.blockScreenEffect(tex == null ? Tex.SOFT : tex));
	}

	/** Open a textured quad batch for the given texture. Call Fx.draw() when done. */
	public static BufferBuilder texQuads(Identifier texture) {
		return begin(RenderLayers.blockScreenEffect(texture));
	}

	/** Open an untextured coloured quad batch. Call Fx.draw() when done. */
	public static BufferBuilder colorQuads() {
		return begin(RenderLayers.debugQuads());
	}

	private static BufferBuilder begin(RenderLayer layer) {
		LAYER.set(layer);
		BufferBuilder b = Tessellator.getInstance().begin(layer.getDrawMode(), layer.getVertexFormat());
		BUFFER.set(b);
		return b;
	}

	/** Finish and draw the batch opened by texQuads/colorQuads. */
	public static void draw() {
		BufferBuilder b = BUFFER.get();
		RenderLayer layer = LAYER.get();
		if (b == null || layer == null) return;
		BuiltBuffer built = b.end();
		if (built != null) layer.draw(built);
		BUFFER.remove();
		LAYER.remove();
	}
}
