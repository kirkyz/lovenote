package com.lovenote.module.impl;

import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class Keystrokes extends HudWidget {
	public Keystrokes() {
		super("Keystrokes", 0.01f, 0.45f);
	}

	private void key(DrawContext ctx, int x, int y, int kw, String label, boolean down) {
		RenderUtil.rrect(ctx, x, y, kw, 18, down ? Colors.alpha(Colors.wave(x * 0.01f), 215) : panel());
		ctx.drawCenteredTextWithShadow(mc.textRenderer, label, x + kw / 2, y + 5, down ? 0xFF101015 : 0xFFB0B0C0);
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		key(ctx, 20, 0, 18, "W", mc.options.forwardKey.isPressed());
		key(ctx, 0, 20, 18, "A", mc.options.leftKey.isPressed());
		key(ctx, 20, 20, 18, "S", mc.options.backKey.isPressed());
		key(ctx, 40, 20, 18, "D", mc.options.rightKey.isPressed());
		key(ctx, 0, 40, 28, "LMB", mc.options.attackKey.isPressed());
		key(ctx, 30, 40, 28, "RMB", mc.options.useKey.isPressed());
		w = 58;
		h = 58;
	}
}
