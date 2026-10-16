package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.Chams;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	@Invoker("getTexture")
	public abstract Identifier lovenote$texture(Entity entity);

	private static Chams chams() {
		return LovenoteClient.modules == null ? null : LovenoteClient.modules.get(Chams.class);
	}

	private static boolean on(LivingEntity e) {
		Chams c = chams();
		return c != null && c.enabled && c.should(e);
	}

	@Inject(method = "getRenderLayer", at = @At("RETURN"), cancellable = true, require = 0)
	private void lovenote$chamsLayer(LivingEntity entity, CallbackInfoReturnable<RenderLayer> cir) {
		if (on(entity)) cir.setReturnValue(RenderLayer.getEntityTranslucentEmissive(lovenote$texture(entity)));
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void lovenote$chamsHead(LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		Chams c = chams();
		if (c == null || !c.enabled || !c.should(entity)) return;

		if (c.throughWalls()) {
			RenderSystem.depthMask(true);
			RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
		}
		int t = c.tint(entity);
		RenderSystem.setShaderColor(((t >> 16) & 255) / 255f, ((t >> 8) & 255) / 255f, (t & 255) / 255f,
				((t >>> 24) & 255) / 255f);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void lovenote$chamsTail(LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
}
