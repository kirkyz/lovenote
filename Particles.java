package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import net.minecraft.client.option.ParticleMode;

public class Particles extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Decreased", "All", "Decreased", "Minimal"));

	public Particles() {
		super("Particles", Category.RENDER);
		describe("Cuts particle spam for more fps.");
	}

	@Override
	public void onTick() {
		ParticleMode want = switch (mode.get()) {
			case "All" -> ParticleMode.ALL;
			case "Minimal" -> ParticleMode.MINIMAL;
			default -> ParticleMode.DECREASED;
		};
		if (mc.options.getParticles().getValue() != want) mc.options.getParticles().setValue(want);
	}
}
