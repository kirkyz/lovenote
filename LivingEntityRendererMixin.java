package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.Chams;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	private static Chams chams() {
		return LovenoteClient.modules == null ? null : LovenoteClient.modules.get(Chams.class);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void lovenote$chamsHead(LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		Chams c = chams();
		boolean on = c != null && c.enabled && c.should(entity);

		if (on && c.throughWalls()) {
			// glClear ignores the depth mask, so flip it on for the clear and let the layer restore it
		}

		int t = on ? c.tint(entity) : 0xFFFFFFFF;
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void lovenote$chamsTail(LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
	}
}
