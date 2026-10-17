package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.SkyShader;
import com.lovenote.util.WorldRender;
import com.lovenote.util.WorldRenderContext;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	private static final String RENDER =
		"render(Lnet/minecraft/client/util/ObjectAllocator;"
		+ "Lnet/minecraft/client/render/RenderTickCounter;Z"
		+ "Lnet/minecraft/client/render/Camera;"
		+ "Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;"
		+ "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
		+ "Lorg/joml/Vector4f;Z)V";

	private static final String SKY =
		"renderSky(Lnet/minecraft/client/render/FrameGraphBuilder;"
		+ "Lnet/minecraft/client/render/state/CameraRenderState;"
		+ "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Matrix4f;)V";

	@Inject(method = RENDER, at = @At("HEAD"), require = 0)
	private void lovenote$renderStart(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
			Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix, Matrix4f projectionMatrix,
			GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		WorldRender.fire(WorldRender.START, context(tickCounter, camera, positionMatrix));
	}

	@Inject(method = RENDER, at = @At("RETURN"), require = 0)
	private void lovenote$renderEnd(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
			Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix, Matrix4f projectionMatrix,
			GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		WorldRenderContext ctx = context(tickCounter, camera, positionMatrix);
		WorldRender.fire(WorldRender.LAST, ctx);
		WorldRender.fire(WorldRender.END, ctx);
	}

	private static WorldRenderContext context(RenderTickCounter tickCounter, Camera camera, Matrix4f positionMatrix) {
		MatrixStack ms = new MatrixStack();
		ms.multiplyPositionMatrix(positionMatrix);
		return new WorldRenderContext(ms, camera, tickCounter.getTickProgress(false));
	}

	@Inject(method = SKY, at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$renderSky(FrameGraphBuilder frameGraphBuilder, CameraRenderState cameraRenderState,
			GpuBufferSlice fogBuffer, Matrix4f matrix, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		SkyShader sky = LovenoteClient.modules.get(SkyShader.class);
		if (sky == null || !sky.enabled) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		float tickProgress = mc.getRenderTickCounter().getTickProgress(false);
		if (sky.render(new MatrixStack(), matrix, tickProgress, mc.gameRenderer.getCamera(), () -> {})) {
			ci.cancel();
		}
	}
}
