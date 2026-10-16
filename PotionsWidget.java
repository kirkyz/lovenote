package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;

public class PotionsWidget extends HudWidget {
	public PotionsWidget() {
		super("Potions", 0.47f, 0.74f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null) return;
		TextRenderer tr = mc.textRenderer;
		int bw = 100;
		box(ctx, 0, 0, bw, 17);
		RenderUtil.rrect(ctx, 6, 5, 7, 7, Colors.wave(0f));
		ctx.drawTextWithShadow(tr, "Potions", 19, 5, 0xFFFFFFFF);
		int y = 19;
		for (StatusEffectInstance e : mc.player.getStatusEffects()) {
			String name = e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1);
			String time = StatusEffectUtil.getDurationText(e, 1.0f, 1.0f).getString();
			bw = Math.max(bw, tr.getWidth(name) + tr.getWidth(time) + 24);
			y += 15;
		}
		y = 19;
		for (StatusEffectInstance e : mc.player.getStatusEffects()) {
			String name = e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1);
			String time = StatusEffectUtil.getDurationText(e, 1.0f, 1.0f).getString();
			box(ctx, 0, y, bw, 14);
			ctx.drawTextWithShadow(tr, name, 7, y + 3, e.getEffectType().value().isBeneficial() ? 0xFFE8E8F0 : 0xFFFF8A8A);
			ctx.drawTextWithShadow(tr, time, bw - 7 - tr.getWidth(time), y + 3, Colors.wave(y * 0.01f));
			y += 15;
		}
		w = bw;
		h = y;
	}
}
