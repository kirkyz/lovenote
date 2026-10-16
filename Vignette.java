package com.lovenote.module.impl;
import net.minecraft.client.gl.RenderPipelines;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Tex;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;

/** Soft darkened screen edges and optional cinematic letterbox bars. */
public class Vignette extends Module {
	private final NumberSetting spread = add(new NumberSetting("Spread", 100, 60, 170, 5));
	private final NumberSetting darkness = add(new NumberSetting("Darkness", 180, 40, 255, 5));
	private final BoolSetting letterbox = add(new BoolSetting("Letterbox", false));
	private final NumberSetting barSize = add(new NumberSetting("Bar height", 22, 4, 80, 2));

	public Vignette() {
		super("Vignette", Category.VISUALS);
		describe("Soft darkened screen edges with optional letterbox bars.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null || mc.options.hudHidden) return;
		int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
		Tex.ensure();
		int m = (int) (Math.max(w, h) * (spread.get() / 100.0) * 0.5);
		ctx.drawTexture(RenderPipelines.GUI_TEXTURED, Tex.VIGNETTE, w / 2 - m, h / 2 - m, 0f, 0f, m * 2, m * 2, 256, 256, 256, 256);
		if (letterbox.get()) {
			int b = barSize.getI();
			ctx.fill(0, 0, w, b, 0xFF000000);
			ctx.fill(0, h - b, w, h, 0xFF000000);
		}
	}
}