package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;

/** Counts consecutive hits without taking damage. */
public class ComboCounter extends Module {
	private final NumberSetting window = add(new NumberSetting("Reset after (s)", 5, 1, 20, 1));
	private final BoolSetting sound = add(new BoolSetting("Sound", true));

	private int combo;
	private long last;
	private float pop;
	private float prevHealth = -1;

	public ComboCounter() {
		super("ComboCounter", Category.HUD);
		describe("Counts consecutive hits without taking damage.");
	}

	@Override
	public void onAttack(Entity target) {
		combo++;
		last = System.currentTimeMillis();
		pop = 1f;
		if (sound.get() && combo % 5 == 0 && mc.player != null)
			mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		float hp = mc.player.getHealth();
		if (prevHealth >= 0 && hp < prevHealth) combo = 0;
		prevHealth = hp;
		if (combo > 0 && System.currentTimeMillis() - last > window.getI() * 1000L) combo = 0;
		if (pop > 0) pop = Math.max(0f, pop - 0.06f);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (combo < 2) return;
		String text = combo + "x";
		int w = mc.textRenderer.getWidth(text);
		var ms = ctx.getMatrices();
		ms.pushMatrix();
		float s = 1f + pop * 0.35f;
		ms.translate(ctx.getScaledWindowWidth() / 2, ctx.getScaledWindowHeight() / 2 - 42);
		ms.scale(s, s);
		ctx.drawTextWithShadow(mc.textRenderer, text, -w / 2, 0, Colors.wave(0f));
		ms.popMatrix();
	}
}
