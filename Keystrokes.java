package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class Keystrokes extends Module {
	public Keystrokes() {
		super("Keystrokes", Category.HUD);
	}

	private void key(DrawContext ctx, int x, int y, int w, String label, boolean down) {
		RenderUtil.rrect(ctx, x, y, w, 18, down ? Colors.alpha(Colors.wave(x * 0.01f), 210) : 0xC0101015);
		ctx.drawCenteredTextWithShadow(mc.textRenderer, label, x + w / 2, y + 5, down ? 0xFF101015 : 0xFFB0B0C0);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		int x = 8, y = ctx.getScaledWindowHeight() / 2 - 30;
		key(ctx, x + 20, y, 18, "W", mc.options.forwardKey.isPressed());
		key(ctx, x, y + 20, 18, "A", mc.options.leftKey.isPressed());
		key(ctx, x + 20, y + 20, 18, "S", mc.options.backKey.isPressed());
		key(ctx, x + 40, y + 20, 18, "D", mc.options.rightKey.isPressed());
		key(ctx, x, y + 40, 28, "LMB", mc.options.attackKey.isPressed());
		key(ctx, x + 30, y + 40, 28, "RMB", mc.options.useKey.isPressed());
	}
}
