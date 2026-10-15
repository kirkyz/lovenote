package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.WorldShaders;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
	@Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void lovenote$sky(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		if (LovenoteClient.modules == null) return;
		WorldShaders ws = LovenoteClient.modules.get(WorldShaders.class);
		if (ws != null && ws.enabled) cir.setReturnValue(ws.sky(cir.getReturnValue()));
	}

	@Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void lovenote$clouds(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		if (LovenoteClient.modules == null) return;
		WorldShaders ws = LovenoteClient.modules.get(WorldShaders.class);
		if (ws != null && ws.enabled) cir.setReturnValue(ws.clouds(cir.getReturnValue()));
	}
}
