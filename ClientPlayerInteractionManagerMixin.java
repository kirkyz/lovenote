package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attackEntity", at = @At("HEAD"))
	private void lovenote$onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
		if (LovenoteClient.modules != null) LovenoteClient.modules.attack(target);
	}
}
