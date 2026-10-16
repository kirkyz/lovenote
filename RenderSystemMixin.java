package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.Fog;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Patches the fog state setters so the Fog module wins wherever fog is set from. */
@Mixin(RenderSystem.class)
public class RenderSystemMixin {

	private static Fog fog() {
		if (!Fog.active) return null;
		if (LovenoteClient.modules == null) return null;
		Fog f = LovenoteClient.modules.get(Fog.class);
		return f != null && f.enabled ? f : null;
	}

	private static float channel(int idx, float value) {
		Fog f = fog();
		if (f == null || !f.recolourOn()) return value;
		int c = f.colour();
		return switch (idx) {
			case 0 -> ((c >> 16) & 255) / 255f;
			case 1 -> ((c >> 8) & 255) / 255f;
			default -> (c & 255) / 255f;
		};
	}

	@ModifyVariable(method = "setShaderFogStart", at = @At("HEAD"), argsOnly = true, require = 0)
	private static float lovenote$fogStart(float value) {
		if (value >= Fog.UI_SENTINEL) return value;
		Fog f = fog();
		return f == null ? value : f.fogStart(value);
	}

	@ModifyVariable(method = "setShaderFogEnd", at = @At("HEAD"), argsOnly = true, require = 0)
	private static float lovenote$fogEnd(float value) {
		if (value >= Fog.UI_SENTINEL) return value;
		Fog f = fog();
		return f == null ? value : f.fogEnd(value);
	}

	@ModifyVariable(method = "setShaderFogColor", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
	private static float lovenote$fogRed(float value) {
		return channel(0, value);
	}

	@ModifyVariable(method = "setShaderFogColor", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
	private static float lovenote$fogGreen(float value) {
		return channel(1, value);
	}

	@ModifyVariable(method = "setShaderFogColor", at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
	private static float lovenote$fogBlue(float value) {
		return channel(2, value);
	}
}
