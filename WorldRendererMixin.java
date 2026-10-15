package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.SkyShader;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(
		method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
		at = @At("HEAD"),
		cancellable = true)
	private void lovenote$renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta,
									Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		SkyShader sky = LovenoteClient.modules.get(SkyShader.class);
		if (sky != null && sky.render(matrices, projectionMatrix, tickDelta, camera, fogCallback)) ci.cancel();
	}
}
