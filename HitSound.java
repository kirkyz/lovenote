package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class HitSound extends Module {
	private final ModeSetting sound = add(new ModeSetting("Sound", "Chime", "Chime", "Orb", "Ding"));
	private final NumberSetting pitch = add(new NumberSetting("Pitch", 1.4, 0.5, 2.0, 0.1));
	private final NumberSetting volume = add(new NumberSetting("Volume", 0.8, 0.1, 1.0, 0.1));

	public HitSound() {
		super("HitSound", Category.VISUALS);
	}

	@Override
	public void onAttack(Entity t) {
		if (!(t instanceof LivingEntity)) return;
		SoundEvent e = switch (sound.get()) {
			case "Orb" -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
			case "Ding" -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
			default -> SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
		};
		mc.getSoundManager().play(PositionedSoundInstance.master(e, pitch.getF(), volume.getF()));
	}
}
