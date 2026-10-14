package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.text.Text;

/** Disconnects when your health drops to the threshold. */
public class AutoLog extends Module {
	private final NumberSetting health = add(new NumberSetting("Health", 6, 1, 19, 1));

	public AutoLog() {
		super("AutoLog", Category.MISC);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.getNetworkHandler() == null) return;
		float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
		if (hp <= health.get()) {
			mc.getNetworkHandler().getConnection().disconnect(Text.literal("[Lovenote] AutoLog at " + (int) hp + " hp"));
			setEnabled(false);
		}
	}
}
