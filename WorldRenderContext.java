package com.lovenote.util;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

/** Minimal replacement for Fabric's WorldRenderContext. */
public class WorldRenderContext {
	private final MatrixStack matrixStack;
	private final Camera camera;
	private final float tickDelta;

	public WorldRenderContext(MatrixStack matrixStack, Camera camera, float tickDelta) {
		this.matrixStack = matrixStack;
		this.camera = camera;
		this.tickDelta = tickDelta;
	}

	public MatrixStack matrixStack() { return matrixStack; }
	public Camera camera() { return camera; }
	public float tickDelta() { return tickDelta; }
}
