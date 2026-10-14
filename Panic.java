package com.lovenote.module.impl;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;

/** One key to switch every module off. */
public class Panic extends Module {
	public Panic() {
		super("Panic", Category.MISC);
	}

	@Override
	public void onEnable() {
		for (Module m : LovenoteClient.modules.all()) {
			if (m != this && m.category != Category.HUD && m.enabled) m.setEnabled(false);
		}
		enabled = false;
		HitLog.push("Panic: all modules off");
	}
}
