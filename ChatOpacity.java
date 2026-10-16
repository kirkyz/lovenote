package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;

public class ChatOpacity extends Module {
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 0, 0, 100, 5));

	public ChatOpacity() {
		super("ChatOpacity", Category.RENDER);
		describe("Chat background transparency.");
	}

	@Override
	public void onTick() {
		double v = opacity.get() / 100.0;
		if (mc.options.getChatOpacity().getValue() != v) mc.options.getChatOpacity().setValue(v);
	}
}
