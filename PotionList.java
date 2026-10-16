package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;

/** Lists your active potion effects with timers. */
public class PotionList extends Module {
	private final BoolSetting level = add(new BoolSetting("Show level", true));

	public PotionList() {
		super("PotionList", Category.HUD);
		describe("Active effects with timers.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.player == null) return;
		var effects = mc.player.getStatusEffects();
		if (effects.isEmpty()) return;
		int sw = ctx.getScaledWindowWidth();
		int y = ctx.getScaledWindowHeight() / 2 + 40;
		int i = 0;
		for (StatusEffectInstance e : effects) {
			String name = Text.translatable(e.getEffectType().value().getTranslationKey()).getString();
			if (level.get() && e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
			int secs = e.getDuration() / 20;
			String time = e.getDuration() > 3200 ? "**" : String.format("%d:%02d", secs / 60, secs % 60);
			String line = name + "  " + time;
			int w = mc.textRenderer.getWidth(line) + 14;
			RenderUtil.rrect(ctx, sw - w - 8, y, w, 13, 0xB00C0C11);
			ctx.fill(sw - w - 8, y, sw - w - 6, y + 13, Colors.wave(i * 0.1f));
			ctx.drawText(mc.textRenderer, line, sw - w - 1, y + 3, 0xFFD8D8E4, false);
			y += 15;
			i++;
		}
	}
}