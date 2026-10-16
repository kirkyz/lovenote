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

/** Hides the vanilla crosshair and tab list while our replacements are enabled. */
@Mixin(InGameHud.class)
public class InGameHudMixin {

	// crosshair - the DrawContext form, this is the one 1.20.1 uses
	@Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$crosshair(DrawContext context, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		CustomCrosshair c = LovenoteClient.modules.get(CustomCrosshair.class);
		if (c != null && c.enabled) ci.cancel();
	}

	// tab list - DrawContext, one int
	@Inject(method = "renderPlayerList(Lnet/minecraft/client/gui/DrawContext;I)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$playerList(DrawContext context, int scaledWidth, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		TabList t = LovenoteClient.modules.get(TabList.class);
		if (t != null && t.enabled) ci.cancel();
	}

	// tab list - DrawContext, two ints (backup)
	@Inject(method = "renderPlayerList(Lnet/minecraft/client/gui/DrawContext;II)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$playerList2(DrawContext context, int scaledWidth, int scaledHeight, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		TabList t = LovenoteClient.modules.get(TabList.class);
		if (t != null && t.enabled) ci.cancel();
	}

	// tab list - MatrixStack, one int (backup)
	@Inject(method = "renderPlayerList(Lnet/minecraft/client/util/math/MatrixStack;I)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$playerListOld(MatrixStack matrices, int scaledWidth, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		TabList t = LovenoteClient.modules.get(TabList.class);
		if (t != null && t.enabled) ci.cancel();
	}
}
