package com.lovenote.mixin;
import net.minecraft.util.math.Vec3d;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

	@Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
	private void lovenote$velocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (LovenoteClient.modules == null || mc.player == null || packet.getEntityId() != mc.player.getId()) return;
		Velocity v = LovenoteClient.modules.get(Velocity.class);
		if (v == null || !v.enabled) return;

		ci.cancel();
		double h = v.horizontal.get() / 100.0, vv = v.vertical.get() / 100.0;
		if (h == 0 && vv == 0) return;
		double x = packet.getVelocity().x / 8000.0, y = packet.getVelocity().y / 8000.0, z = packet.getVelocity().z / 8000.0;
		mc.execute(() -> {
			if (mc.player != null) mc.player.setVelocityClient(new Vec3d(x * h, y * vv, z * h));
		});
	}

	@Inject(method = "onEntityStatus", at = @At("HEAD"))
	private void lovenote$status(EntityStatusS2CPacket packet, CallbackInfo ci) {
		if (packet.getStatus() != 35) return; // 35 = totem of undying used
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.execute(() -> {
			if (mc.world == null || LovenoteClient.modules == null) return;
			Entity e = packet.getEntity(mc.world);
			if (e != null) LovenoteClient.modules.totemPop(e);
		});
	}
}
