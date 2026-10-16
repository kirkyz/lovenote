package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

/** Animated crosshair in your theme colour. */
public class CustomCrosshair extends Module {
	private final ModeSetting style = add(new ModeSetting("Style", "Cross", "Cross", "Dot", "Circle", "Arrows"));
	private final NumberSetting size = add(new NumberSetting("Size", 6, 2, 16, 1));
	private final NumberSetting thickness = add(new NumberSetting("Thickness", 1, 1, 3, 1));
	private final BoolSetting centreGap = add(new BoolSetting("Centre gap", true));
	private final BoolSetting expand = add(new BoolSetting("Expand on hit", true));

	private float pop;

	public CustomCrosshair() {
		super("CustomCrosshair", Category.VISUALS);
		describe("Animated crosshair in your theme colour.");
	}

	@Override
	public void onAttack(Entity target) {
		if (expand.get()) pop = 1f;
	}

	@Override
	public void onTick() {
		if (pop > 0) pop = Math.max(0f, pop - 0.08f);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null || mc.options.hudHidden) return;
		if (mc.currentScreen != null) return;
		if (!mc.options.getPerspective().isFirstPerson()) return;

		int cx = ctx.getScaledWindowWidth() / 2;
		int cy = ctx.getScaledWindowHeight() / 2;
		int s = size.getI() + (int) (pop * 5);
		int t = thickness.getI();
		int c = Colors.wave(0f);
		int inner = centreGap.get() ? Math.max(2, s / 3) : 0;

		switch (style.get()) {
			case "Dot" -> ctx.fill(cx - t, cy - t, cx + t, cy + t, c);
			case "Circle" -> {
				int r = s;
				for (int a = 0; a < 64; a++) {
					double rad = a / 64.0 * Math.PI * 2.0;
					int x = cx + (int) Math.round(Math.cos(rad) * r);
					int y = cy + (int) Math.round(Math.sin(rad) * r);
					ctx.fill(x, y, x + t, y + t, c);
				}
			}
			default -> {
				ctx.fill(cx - inner - s, cy, cx - inner, cy + t, c);
				ctx.fill(cx + inner, cy, cx + inner + s, cy + t, c);
				ctx.fill(cx, cy - inner - s, cx + t, cy - inner, c);
				ctx.fill(cx, cy + inner, cx + t, cy + inner + s, c);
			}
		}
	}
}