package com.lovenote.module.impl;

import com.google.gson.JsonObject;
import com.lovenote.module.Category;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

/** Remembers where you last died. */
public class DeathCoords extends HudWidget {
	private BlockPos death;
	private boolean wasAlive = true;

	public DeathCoords() {
		super("DeathCoords", 0.02f, 0.70f);
		describe("Remembers your last death position.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		boolean alive = mc.player.getHealth() > 0f;
		if (wasAlive && !alive) death = mc.player.getBlockPos();
		wasAlive = alive;
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		int bw = 138, bh = 18;
		box(ctx, 0, 0, bw, bh);
		RenderUtil.skeetBar(ctx, 4, 0, bw - 8, 1);
		if (death == null) {
			ctx.drawTextWithShadow(mc.textRenderer, "no death yet", 6, 5, Colors.faint());
		} else {
			ctx.drawTextWithShadow(mc.textRenderer, "died", 6, 5, Colors.dim());
			String c = death.getX() + " " + death.getY() + " " + death.getZ();
			ctx.drawTextWithShadow(mc.textRenderer, c, bw - 8 - mc.textRenderer.getWidth(c), 5, Colors.wave(0f));
		}
		w = bw;
		h = bh;
	}

	@Override
	public void saveExtra(JsonObject o) {
		if (death == null) return;
		o.addProperty("dx", death.getX());
		o.addProperty("dy", death.getY());
		o.addProperty("dz", death.getZ());
	}

	@Override
	public void loadExtra(JsonObject o) {
		if (o.has("dx")) death = new BlockPos(o.get("dx").getAsInt(), o.get("dy").getAsInt(), o.get("dz").getAsInt());
	}
}