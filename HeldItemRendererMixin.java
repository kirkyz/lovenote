package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.SwingAnimation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"), require = 0)
	private void lovenote$swing(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress,
								ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
								int light, CallbackInfo ci) {
		if (LovenoteClient.modules == null || hand != Hand.MAIN_HAND) return;
		SwingAnimation sa = LovenoteClient.modules.get(SwingAnimation.class);
		if (sa != null && sa.enabled) sa.apply(matrices, swingProgress);
	}
}
