package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/** Animated rings that sweep up and down the player you last hit. */
public class TargetESP extends Module {
	private final NumberSetting rings = add(new NumberSetting("Rings", 3, 1, 5, 1));
	private final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.3, 3.0, 0.1));
	private LivingEntity target;
	private long last;

	public TargetESP() {
		super("TargetESP", Category.VISUALS);
	}

	@Override
	public void onAttack(Entity t) {
		if (t instanceof LivingEntity le) { target = le; last = System.currentTimeMillis(); }
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (target == null) return;
		long age = System.currentTimeMillis() - last;
		if (age > 4500 || !target.isAlive()) { target = null; return; }
		float td = ctx.tickDelta();
		Vec3d p = target.getLerpedPos(td);
		double h = target.getHeight();
		double t = (System.currentTimeMillis() % 100000L) / 1000.0 * speed.get();
		for (int i = 0; i < rings.getI(); i++) {
			double k = (Math.sin(t * 2.0 + i * 0.9) + 1.0) / 2.0;
			int c = Colors.alpha(Colors.wave(i * 0.15f), 230);
			RenderUtil.ring(m, p.add(0, k * h, 0), target.getWidth() * 0.9, c, 40);
			RenderUtil.ring(m, p.add(0, k * h, 0), target.getWidth() * 0.9 + 0.05, Colors.alpha(c, 110), 40);
		}
	}
}
