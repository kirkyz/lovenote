package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;

public class Particles extends Module {
	private final ModeSetting mode = add(new ModeSetting("Mode", "Decreased", "All", "Decreased", "Minimal"));

	public Particles() {
		super("Particles", Category.RENDER);
		describe("Cuts particle spam for more fps.");
	}

	@Override
	public void onTick() {
		var opt = mc.options.getParticles();
		int want = mode.is("All") ? 0 : (mode.is("Minimal") ? 2 : 1);
		int guard = 0;
		while (opt.getValue().ordinal() != want && guard++ < 4) opt.cycle(1);
	}
}
