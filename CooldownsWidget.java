package com.lovenote.module.impl;
import org.joml.Matrix3x2fStack;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class CooldownsWidget extends HudWidget {
	private static final Item[] ITEMS = {Items.ENDER_PEARL, Items.CHORUS_FRUIT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.SHIELD, Items.FIREWORK_ROCKET};

	public CooldownsWidget() {
		super("Cooldowns", 0.47f, 0.79f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null) return;
		int bw = 100;
		box(ctx, 0, 0, bw, 17);
		RenderUtil.rrect(ctx, 6, 5, 7, 7, Colors.wave(0f));
		ctx.drawTextWithShadow(mc.textRenderer, "Cooldowns", 19, 5, 0xFFFFFFFF);
		int y = 19;
		for (Item it : ITEMS) {
			float p = mc.player.getItemCooldownManager().getCooldownProgress(new ItemStack(it), td);
			if (p <= 0f) continue;
			box(ctx, 0, y, bw, 16);
			Matrix3x2fStack m = ctx.getMatrices();
			m.pushMatrix();
			m.translate(5, y + 2);
			m.scale(0.75f, 0.75f);
			ctx.drawItem(new ItemStack(it), 0, 0);
			m.popMatrix();
			int bx = 22;
			ctx.fill(bx, y + 7, bw - 7, y + 9, 0xFF2A2A36);
			RenderUtil.gradientH(ctx, bx, y + 7, bx + (int) ((bw - 7 - bx) * p), y + 9, Colors.C1, Colors.skeet(p));
			y += 17;
		}
		w = bw;
		h = y;
	}
}
