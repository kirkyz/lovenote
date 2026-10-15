package com.lovenote.module.impl;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleList extends HudWidget {
	public ModuleList() {
		super("ModuleList", 0.90f, 0.12f);
		enabled = true;
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		TextRenderer tr = mc.textRenderer;
		List<Module> on = new ArrayList<>();
		for (Module m : LovenoteClient.modules.all()) if (m.enabled && m.category != Category.HUD && !m.hidden) on.add(m);
		on.sort(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed());

		int bw = 110;
		int y = 0;
		for (int i = 0; i < on.size(); i++) {
			String n = on.get(i).name;
			int rw = tr.getWidth(n) + 10;
			int c = Colors.wave(i * 0.07f);
			RenderUtil.rrect(ctx, bw - rw, y, rw, 12, panel());
			ctx.fill(bw - 2, y + 3, bw, y + 9, c);
			ctx.drawTextWithShadow(tr, n, bw - rw + 4, y + 2, c);
			y += 13;
		}
		if (on.isEmpty() && editing) {
			box(ctx, 20, 0, 90, 12);
			ctx.drawTextWithShadow(tr, "ModuleList", 26, 2, Colors.wave(0f));
			y = 13;
		}
		w = bw;
		h = Math.max(12, y);
	}
}
