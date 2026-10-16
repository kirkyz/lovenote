package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Nearby players with distance and health. */
public class PlayerTracker extends HudWidget {
	private final NumberSetting range = add(new NumberSetting("Range", 64, 16, 256, 8));
	private final NumberSetting max = add(new NumberSetting("Max rows", 6, 1, 16, 1));
	private final BoolSetting health = add(new BoolSetting("Show health", true));

	public PlayerTracker() {
		super("PlayerTracker", 0.02f, 0.40f);
		describe("Nearby players with distance and health.");
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		if (mc.player == null || mc.world == null) return;
		List<PlayerEntity> list = new ArrayList<>();
		double r = range.getF();
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player) continue;
			if (p.squaredDistanceTo(mc.player) > r * r) continue;
			list.add(p);
		}
		list.sort(Comparator.comparingDouble(p -> p.squaredDistanceTo(mc.player)));
		int n = Math.min(list.size(), max.getI());
		if (n == 0 && !editing) { w = 0; h = 0; return; }

		int rows = Math.max(1, n);
		int rowH = health.get() ? 13 : 11;
		int bw = 104, bh = 18 + rows * rowH + 2;
		box(ctx, 0, 0, bw, bh);
		RenderUtil.skeetBar(ctx, 4, 0, bw - 8, 1);
		ctx.drawTextWithShadow(mc.textRenderer, "players " + list.size(), 6, 5, Colors.wave(0f));

		int y = 18;
		for (int i = 0; i < rows; i++) {
			if (i >= n) { ctx.drawTextWithShadow(mc.textRenderer, "nobody nearby", 6, y, Colors.faint()); break; }
			PlayerEntity p = list.get(i);
			String name = mc.textRenderer.trimToWidth(p.getName().getString(), 56);
			ctx.drawTextWithShadow(mc.textRenderer, name, 6, y, Colors.text());
			String d = String.format("%.0f", Math.sqrt(p.squaredDistanceTo(mc.player)));
			ctx.drawTextWithShadow(mc.textRenderer, d, bw - 8 - mc.textRenderer.getWidth(d), y, Colors.dim());
			if (health.get()) {
				float hp = p.getHealth() / Math.max(1f, p.getMaxHealth());
				ctx.fill(6, y + 9, bw - 6, y + 11, Colors.track());
				ctx.fill(6, y + 9, 6 + (int) ((bw - 12) * hp), y + 11, Colors.health(hp));
			}
			y += rowH;
		}
		w = bw;
		h = bh;
	}
}