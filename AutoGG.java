package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

public class AutoGG extends Module {
	private final ModeSetting text = add(new ModeSetting("Text", "gg", "gg", "GG", "good game", "gf"));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 40, 5, 200, 5));

	private boolean pending;
	private int timer;

	public AutoGG() {
		super("AutoGG", Category.DONUT);
		describe("Says gg when a game ends.");
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay || !enabled) return;
			String s = message.getString().toLowerCase();
			if (s.contains("won") || s.contains("victory") || s.contains("game over")) {
				pending = true;
				timer = delay.getI();
			}
		});
	}

	@Override
	public void onTick() {
		if (!pending) return;
		if (mc.getNetworkHandler() == null) return;
		if (timer > 0) { timer--; return; }
		mc.getNetworkHandler().sendChatMessage(text.get());
		pending = false;
	}
}