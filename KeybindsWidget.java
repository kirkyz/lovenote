package com.lovenote.module.impl;
import net.minecraft.client.input.KeyInput;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public class KeybindsWidget extends HudWidget {
	private final BoolSetting onlyEnabled = add(new BoolSetting("Only enabled", false));

	public KeybindsWidget() {
		super("Keybinds", 0.30f, 0.07f);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		TextRenderer tr = mc.textRenderer;
		List<String[]> rows = new ArrayList<>();
		rows.add(new String[]{"Click GUI", LovenoteClient.openGui.getBoundKeyLocalizedText().getString()});
		for (Module m : LovenoteClient.modules.all()) {
			if (m.key == -1 || m.hidden) continue;
			if (onlyEnabled.get() && !m.enabled) continue;
			rows.add(new String[]{m.name, InputUtil.fromKeyCode(new KeyInput(m.key, 0, 0)).getLocalizedText().getString()});
		}
		int bw = tr.getWidth("Keybinds") + 36;
		for (String[] r : rows) bw = Math.max(bw, tr.getWidth(r[0]) + tr.getWidth(r[1]) + 28);

		box(ctx, 0, 0, bw, 17);
		RenderUtil.rrect(ctx, 6, 5, 8, 7, Colors.wave(0f));
		ctx.drawTextWithShadow(tr, "Keybinds", 20, 5, 0xFFFFFFFF);
		int y = 19;
		for (String[] r : rows) {
			box(ctx, 0, y, bw, 14);
			ctx.drawTextWithShadow(tr, r[0], 7, y + 3, 0xFFE8E8F0);
			ctx.drawTextWithShadow(tr, r[1], bw - 7 - tr.getWidth(r[1]), y + 3, Colors.wave(y * 0.01f));
			y += 15;
		}
		w = bw;
		h = y;
	}
}
