package com.lovenote.module.impl;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/** Treats players that are not in the tab list as bots (anti-cheat decoys). */
public class AntiBot extends Module {
	public AntiBot() {
		super("AntiBot", Category.COMBAT);
	}

	public static boolean isBot(PlayerEntity p) {
		AntiBot a = LovenoteClient.modules.get(AntiBot.class);
		if (a == null || !a.enabled) return false;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.getNetworkHandler() == null) return false;
		if (p.getName().getString().equals("FakePlayer")) return false;
		return mc.getNetworkHandler().getPlayerListEntry(p.getUuid()) == null;
	}
}
