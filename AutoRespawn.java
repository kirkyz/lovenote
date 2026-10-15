package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module {
	public AutoRespawn() {
		super("AutoRespawn", Category.MISC);
	}

	@Override
	public void onTick() {
		if (mc.player != null && mc.currentScreen instanceof DeathScreen) mc.player.requestRespawn();
	}
}
