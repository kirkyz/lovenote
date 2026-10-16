package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.mojang.blaze3d.systems.RenderSystem;

/** Independent fog colour and distance, driven by BackgroundRendererMixin. */
public class Fog extends Module {
	private final ModeSetting colourMode = add(new ModeSetting("Colour", "Accent", "Accent", "Custom", "Black", "White"));
	private final NumberSetting hue = add(new NumberSetting("Custom hue", 275, 0, 360, 1));
	private final ModeSetting preset = add(new ModeSetting("Distance", "Thin", "Off", "Thin", "Thick", "Custom"));
	private final NumberSetting start = add(new NumberSetting("Start", 30, 0, 256, 2));
	private final NumberSetting end = add(new NumberSetting("End", 160, 8, 512, 4));

	public Fog() {
		super("Fog", Category.VISUALS);
		describe("Custom fog colour and distance.");
	}

	/** Called from BackgroundRendererMixin just before terrain is drawn. */
	public void apply() {
		int c = switch (colourMode.get()) {
			case "Custom" -> Colors.hsv(hue.getI() / 360f, 0.7f, 1f);
			case "Black" -> 0xFF000000;
			case "White" -> 0xFFFFFFFF;
			default -> Colors.C2;
		};
		float s;
		float e;
		switch (preset.get()) {
			case "Off" -> { s = 1f; e = 256f; }
			case "Thick" -> { s = 6f; e = 60f; }
			case "Custom" -> { s = start.getI(); e = end.getI(); }
			default -> { s = 20f; e = 130f; }
		}
		RenderSystem.setShaderFogColor(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, 1f);
		RenderSystem.setShaderFogStart(s);
		RenderSystem.setShaderFogEnd(e);
	}
}
