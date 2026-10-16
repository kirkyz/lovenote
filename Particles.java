package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import net.minecraft.particle.ParticlesMode;

public class Particles extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Decreased", "All", "Decreased", "Minimal"));

	public Particles() {
		super("Particles", Category.RENDER);
		describe("Cuts particle spam for more fps.");
	}

	@Override
	public void onTick() {
		ParticlesMode want = switch (mode.get()) {
			case "All" -> ParticlesMode.ALL;
			case "Minimal" -> ParticlesMode.MINIMAL;
			default -> ParticlesMode.DECREASED;
		};
		if (mc.options.getParticles().getValue() != want) mc.options.getParticles().setValue(want);
	}
}
