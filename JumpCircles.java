package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class JumpCircles extends Module {
	private final NumberSetting radius = add(new NumberSetting("Radius", 2.0, 0.5, 4.0, 0.1));
	private final NumberSetting duration = add(new NumberSetting("Duration (s)", 1.0, 0.3, 2.0, 0.1));

	private record Circle(Vec3d pos, long start) {}

	private final List<Circle> circles = new ArrayList<>();
	private boolean wasGround = true;

	public JumpCircles() {
		super("JumpCircles", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		boolean g = mc.player.isOnGround();
		long now = System.currentTimeMillis();
		if (wasGround && !g && mc.player.getVelocity().y > 0.2) circles.add(new Circle(mc.player.getEntityPos(), now));
		wasGround = g;
		long life = (long) (duration.get() * 1000);
		circles.removeIf(c -> now - c.start > life);
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		long now = System.currentTimeMillis();
		float life = (float) (duration.get() * 1000.0);
		for (Circle c : circles) {
			float age = Math.min(1f, (now - c.start) / life);
			double r = 0.3 + age * radius.get();
			int a = (int) (255 * (1f - age));
			int col = Colors.alpha(Colors.wave(age), a);
			Vec3d p = c.pos.add(0, 0.02, 0);
			RenderUtil.ring(m, p, r, col, 56);
			RenderUtil.ring(m, p, r * 0.9, Colors.alpha(col, a / 2), 56);
			RenderUtil.ring(m, p, r * 0.8, Colors.alpha(col, a / 4), 56);
		}
	}
}
