package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.WorldTint;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Drives WorldTint's sky + cloud recolour. getSkyColor/getCloudsColor live on ClientWorld in 1.20.1. */
@Mixin(ClientWorld.class)
public class ClientWorldMixin {

	@Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void lovenote$skyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		if (LovenoteClient.modules == null) return;
		WorldTint t = LovenoteClient.modules.get(WorldTint.class);
		if (t != null && t.enabled) cir.setReturnValue(t.tint(cir.getReturnValue()));
	}

	@Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void lovenote$cloudsColor(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		if (LovenoteClient.modules == null) return;
		WorldTint t = LovenoteClient.modules.get(WorldTint.class);
		if (t != null && t.enabled) cir.setReturnValue(t.tint(cir.getReturnValue()));
	}
}
