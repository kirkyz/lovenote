package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

public class Velocity extends Module {
	public final NumberSetting horizontal = add(new NumberSetting("Horizontal %", 0, 0, 100, 1));
	public final NumberSetting vertical = add(new NumberSetting("Vertical %", 0, 0, 100, 1));

	public Velocity() {
		super("Velocity", Category.COMBAT);
	}
}
