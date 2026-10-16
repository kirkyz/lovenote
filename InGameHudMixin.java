package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.CustomCrosshair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the vanilla crosshair while our own is enabled. */
@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$crosshair(DrawContext context, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		CustomCrosshair c = LovenoteClient.modules.get(CustomCrosshair.class);
		if (c != null && c.enabled) ci.cancel();
	}
}
