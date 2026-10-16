package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;

/** Floating name and health readout over whatever you last hit. */
public class DamageIndicator extends Module {

	private final ModeSetting style = add(new ModeSetting("Style", "Name", "Name", "Health", "Both"));
	private final NumberSetting rise = add(new NumberSetting("Rise", 1.4, 0.2, 4.0, 0.1));
	private final NumberSetting duration = add(new NumberSetting("Duration (ms)", 1100, 300, 3000, 50));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Health", "Rainbow"));

	private static final class Tag {
		double x, y, z;
		String text;
		long start;
		float hp;
	}

	private final ArrayList<Tag> tags = new ArrayList<>();

	public DamageIndicator() {
		super("DamageIndicator", Category.VISUALS);
		describe("Floating target names and health on hit.");
	}

	@Override
	public void onAttack(Entity target) {
		if (target == null) return;
		float hp = target instanceof LivingEntity le ? le.getHealth() : 0f;
		String name = target.getName().getString();
		String text = switch (style.get()) {
			case "Health" -> String.format("%.1f", hp);
			case "Both" -> name + "  " + String.format("%.1f", hp);
			default -> name;
		};
		Tag t = new Tag();
		t.x = target.getX();
		t.y = target.getY() + target.getHeight() + 0.4;
		t.z = target.getZ();
		t.text = text;
		t.start = System.currentTimeMillis();
		t.hp = hp;
		tags.add(t);
		if (tags.size() > 24) tags.remove(0);
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (tags.isEmpty() || mc.player == null) return;
		long now = System.currentTimeMillis();
		int dur = duration.getI();
		tags.removeIf(t -> now - t.start > dur);
		if (tags.isEmpty()) return;

		var cam = mc.gameRenderer.getCamera();
		Matrix4f view = new Matrix4f();
		view.rotateX((float) Math.toRadians(cam.getPitch()));
		view.rotateY((float) Math.toRadians(cam.getYaw() + 180f));
		view.translate((float) -cam.getCameraPos().x, (float) -cam.getCameraPos().y, (float) -cam.getCameraPos().z);
		Matrix4f proj = mc.gameRenderer.getBasicProjectionMatrix(mc.options.getFov().getValue());
		Matrix4f mvp = new Matrix4f(proj).mul(view);

		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		for (Tag t : tags) {
			float f = (now - t.start) / (float) dur;
			Vector4f v = new Vector4f((float) t.x, (float) (t.y + rise.getF() * f), (float) t.z, 1f);
			v.mul(mvp);
			if (v.w <= 0.01f) continue;
			float sx = (v.x / v.w * 0.5f + 0.5f) * sw;
			float sy = (1f - (v.y / v.w * 0.5f + 0.5f)) * sh;
			if (sx < -120 || sx > sw + 120 || sy < -40 || sy > sh + 40) continue;
			int a = (int) (255 * (1f - f));
			if (a < 8) continue;
			String s = t.text;
			int w = mc.textRenderer.getWidth(s);
			ctx.drawTextWithShadow(mc.textRenderer, s, (int) (sx - w / 2f), (int) sy, Colors.alpha(colour(t.hp), a));
		}
	}

	private int colour(float hp) {
		return switch (colour.get()) {
			case "Health" -> Colors.health(Math.min(1f, hp / 20f));
			case "Rainbow" -> Colors.rainbow(0f);
			default -> Colors.wave(0f);
		};
	}
}