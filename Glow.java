package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.entity.player.PlayerEntity;

/** Vanilla glowing outline on other players (visible through walls). */
public class Glow extends Module {
	public Glow() {
		super("Glow", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.world == null) return;
		for (PlayerEntity p : mc.world.getPlayers()) if (p != mc.player) p.setGlowing(true);
	}

	@Override
	public void onDisable() {
		if (mc.world == null) return;
		for (PlayerEntity p : mc.world.getPlayers()) if (p != mc.player) p.setGlowing(false);
	}
}
