package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class AutoReconnect extends Module {
	private final NumberSetting delay = add(new NumberSetting("Delay (s)", 2, 1, 30, 1));

	private String address;
	private ServerInfo info;
	private int timer = -1;

	public AutoReconnect() {
		super("AutoReconnect", Category.DONUT);
		describe("Rejoins the server after a disconnect.");
	}

	@Override
	public void onTick() {
		if (mc.getCurrentServerEntry() != null) {
			info = mc.getCurrentServerEntry();
			address = info.address;
		}
		if (address == null || info == null) return;

		if (mc.currentScreen instanceof DisconnectedScreen) {
			if (timer < 0) timer = delay.getI() * 20;
			if (timer > 0) { timer--; return; }
			timer = -1;
			ConnectScreen.connect(mc.currentScreen, mc, ServerAddress.parse(address), info, false, null);
			return;
		}
		timer = -1;
	}
}
