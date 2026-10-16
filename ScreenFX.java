package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/** Full screen post effects: grain, scanlines, damage flash and low health pulse. */
public class ScreenFX extends Module {
	private final BoolSetting grade = add(new BoolSetting("Colour grade", true));
	private final ModeSetting gradeMode = add(new ModeSetting("Grade colour", "Auto", "Auto", "Red", "Cyan", "Violet"));
	private final NumberSetting gradeStrength = add(new NumberSetting("Grade strength", 40, 0, 120, 5));
	private final BoolSetting grain = add(new BoolSetting("Film grain", true));
	private final NumberSetting grainAmount = add(new NumberSetting("Grain amount", 90, 10, 400, 10));
	private final BoolSetting scanlines = add(new BoolSetting("Scanlines", false));
	private final NumberSetting lineGap = add(new NumberSetting("Line gap", 3, 2, 8, 1));
	private final BoolSetting damage = add(new BoolSetting("Damage flash", true));
	private final BoolSetting lowHealth = add(new BoolSetting("Low health pulse", true));
	private final NumberSetting lowAt = add(new NumberSetting("Pulse below", 8, 2, 20, 1));

	private final Random rng = new Random();
	private float flash;
	private float prevHealth = -1;

	public ScreenFX() {
		super("ScreenFX", Category.VISUALS);
		describe("Grain, scanlines, damage flash and low health pulse.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) { prevHealth = -1; return; }
		float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
		if (prevHealth >= 0 && hp < prevHealth) flash = Math.min(1f, flash + (prevHealth - hp) / 8f);
		prevHealth = hp;
		if (flash > 0) flash = Math.max(0f, flash - 0.05f);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null || mc.options.hudHidden) return;
		int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();

		if (lowHealth.get() && mc.player.getHealth() <= lowAt.getI()) {
			float pulse = (float) ((Math.sin(System.currentTimeMillis() / 320.0) + 1.0) * 0.5);
			ctx.fill(0, 0, w, h, Colors.alpha(0xFF3B1E1E, (int) (pulse * 40)));
		}
		if (damage.get() && flash > 0.01f) {
			ctx.fill(0, 0, w, h, Colors.alpha(0xFF2A0A0A, (int) (flash * 120)));
		}
		if (grade.get()) {
			int c = switch (gradeMode.get()) {
				case "Red" -> 0xFFFF3B3B;
				case "Cyan" -> 0xFF3BE8FF;
				case "Violet" -> 0xFF8A4DFF;
				default -> Colors.C2;
			};
			ctx.fill(0, 0, w, h, Colors.alpha(c, gradeStrength.getI()));
		}
		if (scanlines.get()) {
			int gap = lineGap.getI();
			for (int y = 0; y < h; y += gap) ctx.fill(0, y, w, y + 1, 0x14000000);
		}
		if (grain.get()) {
			int n = grainAmount.getI();
			for (int i = 0; i < n; i++) {
				int x = rng.nextInt(Math.max(1, w));
				int y = rng.nextInt(Math.max(1, h));
				ctx.fill(x, y, x + 1, y + 1, rng.nextBoolean() ? 0x14FFFFFF : 0x14000000);
			}
		}
	}
}