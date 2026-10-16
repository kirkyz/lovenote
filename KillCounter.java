package com.lovenote.module.impl;

import com.google.gson.JsonObject;
import com.lovenote.module.Category;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** Counts kills in this session. */
public class KillCounter extends HudWidget {
	private int kills;
	private LivingEntity target;
	private float lastHp;
	private long lastHit;

	public KillCounter() {
		super("KillCounter", 0.02f, 0.62f);
		describe("Counts the kills you get this session.");
	}

	@Override
	public void onAttack(Entity e) {
		if (!(e instanceof LivingEntity le)) return;
		target = le;
		lastHp = le.getHealth();
		lastHit = System.currentTimeMillis();
	}

	@Override
	public void onTick() {
		if (target == null) return;
		if (System.currentTimeMillis() - lastHit > 4000) { target = null; return; }
		float hp = target.getHealth();
		if (hp <= 0f && lastHp > 0f) { kills++; target = null; return; }
		lastHp = hp;
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		int bw = 92, bh = 18;
		box(ctx, 0, 0, bw, bh);
		RenderUtil.skeetBar(ctx, 4, 0, bw - 8, 1);
		ctx.drawTextWithShadow(mc.textRenderer, "kills", 6, 5, Colors.dim());
		String k = String.valueOf(kills);
		ctx.drawTextWithShadow(mc.textRenderer, k, bw - 8 - mc.textRenderer.getWidth(k), 5, Colors.wave(0f));
		w = bw;
		h = bh;
	}

	@Override
	public void saveExtra(JsonObject o) { o.addProperty("kills", kills); }

	@Override
	public void loadExtra(JsonObject o) { if (o.has("kills")) kills = o.get("kills").getAsInt(); }
}