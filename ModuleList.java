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

public class ModuleList extends Module {
	public ModuleList() {
		super("ModuleList", Category.HUD);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		TextRenderer tr = mc.textRenderer;
		List<Module> on = new ArrayList<>();
		for (Module m : LovenoteClient.modules.all()) if (m.enabled && m.category != Category.HUD) on.add(m);
		on.sort(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed());

		int right = ctx.getScaledWindowWidth() - 8;
		int y = 31;
		for (int i = 0; i < on.size(); i++) {
			String n = on.get(i).name;
			int w = tr.getWidth(n) + 10;
			int c = Colors.wave(i * 0.07f);
			RenderUtil.rrect(ctx, right - w, y, w, 12, 0xA0101015);
			ctx.fill(right - 2, y + 3, right, y + 9, c);
			ctx.drawTextWithShadow(tr, n, right - w + 4, y + 2, c);
			y += 13;
		}
	}
}
