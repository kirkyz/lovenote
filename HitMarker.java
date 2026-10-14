package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class HitMarker extends Module {
	private long last;

	public HitMarker() {
		super("HitMarker", Category.VISUALS);
	}

	@Override
	public void onAttack(Entity t) {
		if (t instanceof LivingEntity) last = System.currentTimeMillis();
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		long age = System.currentTimeMillis() - last;
		if (age > 260) return;
		int a = (int) (255 * (1f - age / 260f));
		int col = Colors.alpha(Colors.wave(0f), a);
		int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
		for (int i = 3; i <= 7; i++) {
			ctx.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, col);
			ctx.fill(cx - i - 1, cy + i, cx - i, cy + i + 1, col);
			ctx.fill(cx + i, cy - i - 1, cx + i + 1, cy - i, col);
			ctx.fill(cx - i - 1, cy - i - 1, cx - i, cy - i, col);
		}
	}
}
