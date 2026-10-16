package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.SkyShader;
import com.lovenote.util.WorldRender;
import com.lovenote.util.WorldRenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(method = "render", at = @At("HEAD"), require = 0)
	private void lovenote$renderStart(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera,
			GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		WorldRender.fire(WorldRender.START, context(tickCounter, camera, positionMatrix));
	}

	@Inject(method = "render", at = @At("RETURN"), require = 0)
	private void lovenote$renderEnd(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera,
			GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		WorldRenderContext ctx = context(tickCounter, camera, positionMatrix);
		WorldRender.fire(WorldRender.LAST, ctx);
		WorldRender.fire(WorldRender.END, ctx);
	}

	private static WorldRenderContext context(RenderTickCounter tickCounter, Camera camera, Matrix4f positionMatrix) {
		MatrixStack ms = new MatrixStack();
		ms.multiplyPositionMatrix(positionMatrix);
		return new WorldRenderContext(ms, camera, tickCounter.getTickDelta(false));
	}

	@Inject(
		method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
		at = @At("HEAD"),
		cancellable = true,
		require = 0)
	private void lovenote$renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta,
			Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		SkyShader sky = LovenoteClient.modules.get(SkyShader.class);
		if (sky != null && sky.enabled && sky.render(matrices, projectionMatrix, tickDelta, camera, fogCallback)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderClouds", at = @At("HEAD"), require = 0)
	private void lovenote$cloudTint(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta,
			double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		SkyShader sky = LovenoteClient.modules.get(SkyShader.class);
		if (sky == null || !sky.enabled) return;
		int c = sky.cloudTint();
		if (c < 0) return;
		RenderSystem.setShaderColor(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, 1f);
	}

	@Inject(method = "renderClouds", at = @At("RETURN"), require = 0)
	private void lovenote$cloudTintEnd(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta,
			double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
}
