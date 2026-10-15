package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/** Event log: hits, totem pops, panic... */
public class HitLog extends HudWidget {
	private record Entry(String text, long time) {}

	private static final List<Entry> ENTRIES = new ArrayList<>();

	public HitLog() {
		super("HitLog", 0.01f, 0.62f);
		enabled = true;
	}

	public static synchronized void push(String s) {
		ENTRIES.add(new Entry(s, System.currentTimeMillis()));
		while (ENTRIES.size() > 7) ENTRIES.remove(0);
	}

	@Override
	public void onAttack(Entity t) {
		if (mc.player == null || !(t instanceof LivingEntity)) return;
		push("Hit " + t.getName().getString() + " (" + String.format("%.1f", mc.player.distanceTo(t)) + "m)");
	}

	@Override
	protected synchronized void draw(DrawContext ctx, float td) {
		long now = System.currentTimeMillis();
		ENTRIES.removeIf(e -> now - e.time > 4000);
		List<Entry> list = new ArrayList<>(ENTRIES);
		if (editing && list.isEmpty()) list.add(new Entry("HitLog preview", now));
		int y = 0, bw = 120;
		for (Entry e : list) {
			long age = now - e.time;
			int a = age > 3000 ? (int) (255 * (1 - (age - 3000) / 1000f)) : 255;
			if (a < 8) continue;
			int ew = mc.textRenderer.getWidth(e.text) + 14;
			bw = Math.max(bw, ew);
			com.lovenote.util.RenderUtil.rrect(ctx, 0, y, ew, 13, Colors.alpha(0x101015, a * 215 / 255));
			ctx.fill(2, y + 3, 4, y + 10, Colors.alpha(Colors.wave(y * 0.01f), a));
			ctx.drawTextWithShadow(mc.textRenderer, e.text, 8, y + 3, Colors.alpha(0xFFFFFF, a));
			y += 15;
		}
		w = bw;
		h = Math.max(13, y);
	}
}
