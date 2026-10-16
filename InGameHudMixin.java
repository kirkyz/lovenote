package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.CustomCrosshair;
import com.lovenote.module.impl.TabList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the vanilla crosshair and tab list while our own versions are enabled. */
@Mixin(InGameHud.class)
public class InGameHudMixin {

	// crosshair - 1.20 DrawContext form
	@Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$crosshair(DrawContext context, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		CustomCrosshair c = LovenoteClient.modules.get(CustomCrosshair.class);
		if (c != null && c.enabled) ci.cancel();
	}

	// crosshair - older MatrixStack form
	@Inject(method = "renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$crosshairOld(MatrixStack matrices, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		CustomCrosshair c = LovenoteClient.modules.get(CustomCrosshair.class);
		if (c != null && c.enabled) ci.cancel();
	}

	// tab list - 1.20 DrawContext form
	@Inject(method = "renderPlayerList(Lnet/minecraft/client/gui/DrawContext;FII)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$playerList(DrawContext context, float tickDelta, int scaledWidth,
			int scaledHeight, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		TabList t = LovenoteClient.modules.get(TabList.class);
		if (t != null && t.enabled) ci.cancel();
	}

	// tab list - older MatrixStack form
	@Inject(method = "renderPlayerList(Lnet/minecraft/client/util/math/MatrixStack;FII)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$playerListOld(MatrixStack matrices, float tickDelta, int scaledWidth,
			int scaledHeight, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		TabList t = LovenoteClient.modules.get(TabList.class);
		if (t != null && t.enabled) ci.cancel();
	}
}
