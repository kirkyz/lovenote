package com.lovenote.module.impl;
import org.joml.Matrix3x2fStack;

import com.lovenote.setting.BoolSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

/** Shows whoever you last hit: head, name, distance, health ring and armour slots. */
public class TargetHud extends HudWidget {
	private final BoolSetting ring = add(new BoolSetting("Health ring", true));
	private final BoolSetting armor = add(new BoolSetting("Armor slots", true));
	private final BoolSetting headIcon = add(new BoolSetting("Head", true));

	private LivingEntity target;
	private long last;
	private float shown = 1f;

	public TargetHud() {
		super("TargetHud", 0.18f, 0.145f);
		enabled = true;
	}

	@Override
	public void onAttack(Entity t) {
		if (t instanceof LivingEntity le) {
			if (le != target) shown = MathHelper.clamp(le.getHealth() / le.getMaxHealth(), 0f, 1f);
			target = le;
			last = System.currentTimeMillis();
		}
	}

	private void slot(DrawContext ctx, int x, int y, ItemStack st) {
		RenderUtil.rrect(ctx, x, y, 17, 15, 0xFF1C1C25);
		if (st == null || st.isEmpty()) return;
		Matrix3x2fStack m = ctx.getMatrices();
		m.pushMatrix();
		m.translate(x + 2.5f, y + 1.5f);
		m.scale(0.75f, 0.75f);
		ctx.drawItem(st, 0, 0);
		m.popMatrix();
	}

	@Override
	protected void draw(DrawContext ctx, float td) {
		long age = System.currentTimeMillis() - last;
		if (target != null && (age > 4500 || (!target.isAlive() && age > 1500))) target = null;
		boolean live = target != null;
		if (!live && !editing) { w = 0; h = 0; return; }

		TextRenderer tr = mc.textRenderer;
		String name = live ? target.getName().getString() : "Target";
		float hp = live ? target.getHealth() + target.getAbsorptionAmount() : 20f;
		float max = live ? target.getMaxHealth() : 20f;
		float f = MathHelper.clamp(hp / Math.max(1f, max), 0f, 1f);
		shown += (f - shown) * 0.15f;

		int bw = 132, bh = armor.get() ? 52 : 34;
		box(ctx, 0, 0, bw, bh);
		RenderUtil.skeetBar(ctx, 3, 0, bw - 6, 1);

		int tx = 8;
		if (headIcon.get()) {
			head(ctx, live ? target : null, 6, 6, 22);
			tx = 34;
		}
		ctx.drawTextWithShadow(tr, name, tx, 8, 0xFFFFFFFF);
		String dist = live && mc.player != null ? String.format("Distance: %.1f", mc.player.distanceTo(target)) : "Distance: 0.0";
		ctx.drawText(tr, dist, tx, 19, 0xFF8A8A9A, false);

		if (ring.get()) {
			int cx = bw - 20, cy = 17, r = 9;
			for (int i = 0; i < 48; i++) {
				double a = -Math.PI / 2 + i * (Math.PI * 2 / 48);
				int dx = (int) Math.round(Math.cos(a) * r), dy = (int) Math.round(Math.sin(a) * r);
				ctx.fill(cx + dx - 1, cy + dy - 1, cx + dx + 1, cy + dy + 1, i < shown * 48 ? Colors.wave(i * 0.01f) : 0xFF2A2A36);
			}
			String n = String.valueOf((int) Math.ceil(hp));
			ctx.drawCenteredTextWithShadow(tr, n, cx, cy - 4, 0xFFFFFFFF);
		}

		if (armor.get()) {
			int y = 35;
			EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
			for (int i = 0; i < slots.length; i++) {
				slot(ctx, 6 + i * 20, y, live ? target.getEquippedStack(slots[i]) : ItemStack.EMPTY);
			}
		}
		w = bw;
		h = bh;
	}
}
