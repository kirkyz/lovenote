package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;

/** Custom fog distance and colour, applied through RenderSystemMixin. */
public class Fog extends Module {

	/** True only while the world is being drawn, never during the HUD/GUI pass. */
	public static boolean active = false;

	/** Vanilla hands the interface a huge fog distance; anything past this is treated as "not world". */
	public static final float UI_SENTINEL = 900f;

	private final ModeSetting preset = add(new ModeSetting("Distance", "Thin", "Off", "Thin", "Thick", "Custom"));
	private final NumberSetting start = add(new NumberSetting("Start", 20, 0, 256, 2));
	private final NumberSetting end = add(new NumberSetting("End", 130, 8, 512, 4));
	private final BoolSetting recolour = add(new BoolSetting("Recolour fog", false));
	private final ModeSetting colourMode = add(new ModeSetting("Colour", "Accent", "Accent", "Custom", "Black", "White"));
	private final NumberSetting hue = add(new NumberSetting("Custom hue", 275, 0, 360, 1));

	public Fog() {
		super("Fog", Category.VISUALS);
		describe("Custom fog distance and colour.");
	}

	@Override
	public void onDisable() {
		active = false;
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		// the HUD pass is drawn after the world: stand down so UI text is never fogged
		active = false;
	}

	public float fogStart(float in) {
		return switch (preset.get()) {
			case "Off" -> 0f;
			case "Thick" -> 6f;
			case "Custom" -> (float) start.getI();
			default -> 20f;
		};
	}

	public float fogEnd(float in) {
		return switch (preset.get()) {
			case "Off" -> 256f;
			case "Thick" -> 60f;
			case "Custom" -> (float) end.getI();
			default -> 130f;
		};
	}

	public boolean recolourOn() {
		return recolour.get();
	}

	public int colour() {
		return switch (colourMode.get()) {
			case "Custom" -> Colors.hsv(hue.getI() / 360f, 0.7f, 1f);
			case "Black" -> 0xFF000000;
			case "White" -> 0xFFFFFFFF;
			default -> Colors.C2;
		};
	}

	/** Fallback path, called from BackgroundRendererMixin at the end of the sky/fog pass. */
	public void apply() {
		active = true;
	}
}
