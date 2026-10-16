package com.lovenote.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor("jumpingCooldown")
	int lovenote$getJumpingCooldown();

	@Accessor("jumpingCooldown")
	void lovenote$setJumpingCooldown(int value);
}
