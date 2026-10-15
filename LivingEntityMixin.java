package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.SwingAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true, require = 0)
	private void lovenote$swingDuration(CallbackInfoReturnable<Integer> cir) {
		if (LovenoteClient.modules == null || (Object) this != MinecraftClient.getInstance().player) return;
		SwingAnimation sa = LovenoteClient.modules.get(SwingAnimation.class);
		if (sa != null && sa.enabled) cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValue() / sa.speed.getF())));
	}
}
