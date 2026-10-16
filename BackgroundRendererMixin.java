package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.Fog;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class BackgroundRendererMixin {

	@Inject(method = "render", at = @At("TAIL"), require = 0)
	private static void lovenote$fog(Camera camera, float tickDelta, ClientWorld world, int renderDistance,
			float darkenWorldAmount, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		Fog fog = LovenoteClient.modules.get(Fog.class);
		if (fog == null || !fog.enabled) return;
		fog.apply();
	}
}
