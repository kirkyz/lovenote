package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.SkyShader;
import com.lovenote.module.impl.WorldTint;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

	@Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
	private void lovenote$skyTint(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		if (LovenoteClient.modules == null) return;
		WorldTint t = LovenoteClient.modules.get(WorldTint.class);
		if (t != null && t.enabled) cir.setReturnValue(t.tint(cir.getReturnValue()));
	}
}
