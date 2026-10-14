package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/** Shows the player you last hit: name, health bar, distance. */
public class TargetHud extends Module {
	private LivingEntity target;
	private long last;
	private float shown = 1f;

	public TargetHud() {
		super("TargetHud", Category.HUD);
	}

	@Override
	public void onAttack(Entity t) {
		if (t instanceof LivingEntity le) {
			if (le != target) shown = MathHelper.clamp(le.getHealth() / le.getMaxHealth(), 0f, 1f);
			target = le;
			last = System.currentTimeMillis();
		}
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (target == null || mc.player == null) return;
		long age = System.currentTimeMillis() - last;
		if (age > 4500 || (!target.isAlive() && age > 1500)) { target = null; return; }

		float f = MathHelper.clamp(target.getHealth() / target.getMaxHealth(), 0f, 1f);
		shown += (f - shown) * 0.15f;

		int w = 140, h = 36;
		int x = ctx.getScaledWindowWidth() / 2 + 24, y = ctx.getScaledWindowHeight() / 2 + 14;
		RenderUtil.rrect(ctx, x, y, w, h, 0xE0101015);
		RenderUtil.skeetBar(ctx, x + 3, y, w - 6, 1);
		ctx.drawTextWithShadow(mc.textRenderer, target.getName().getString(), x + 8, y + 7, 0xFFFFFFFF);
		String hp = String.format("%.1f", Math.max(0f, target.getHealth() + target.getAbsorptionAmount()));
		ctx.drawText(mc.textRenderer, hp, x + w - 8 - mc.textRenderer.getWidth(hp), y + 7, 0xFFB0B0C0, false);

		int bx = x + 8, by = y + 21, bw = w - 16;
		ctx.fill(bx, by, bx + bw, by + 4, 0xFF24242E);
		RenderUtil.gradientH(ctx, bx, by, bx + (int) (bw * shown), by + 4, Colors.C1, Colors.skeet(shown));
		String d = String.format("%.1fm", mc.player.distanceTo(target));
		ctx.drawText(mc.textRenderer, d, bx, by + 7, 0xFF70708A, false);
	}
}
