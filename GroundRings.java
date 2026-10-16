package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.Fx;
import com.mojang.blaze3d.systems.RenderSystem;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Expanding rings on the ground beneath you. */
public class GroundRings extends Module {

	private record Ring(double x, double y, double z, long start) {}

	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "Static"));
	private final NumberSetting maxRadius = add(new NumberSetting("Max radius", 2.2, 0.5, 6.0, 0.1));
	private final NumberSetting life = add(new NumberSetting("Lifetime", 1.4, 0.4, 4.0, 0.1));
	private final NumberSetting thickness = add(new NumberSetting("Thickness", 0.06, 0.01, 0.4, 0.01));
	private final NumberSetting rate = add(new NumberSetting("Rate", 0.55, 0.1, 3.0, 0.05));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 160, 10, 255, 5));
	private final BoolSetting follow = add(new BoolSetting("Follow", false));

	private final List<Ring> rings = new ArrayList<>();
	private double acc;

	public GroundRings() {
		super("GroundRings", Category.VISUALS);
		describe("Expanding rings on the floor under you.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) { rings.clear(); return; }
		acc += rate.get();
		while (acc >= 1.0) {
			acc -= 1.0;
			rings.add(new Ring(mc.player.getX(), mc.player.getY(), mc.player.getZ(), System.currentTimeMillis()));
			if (rings.size() > 24) rings.remove(0);
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null || rings.isEmpty()) return;
		Matrix4f mat = m.peek().getPositionMatrix();
		long now = System.currentTimeMillis();
		long lifeMs = (long) (life.get() * 1000);

		Fx.additive();

		rings.removeIf(r -> now - r.start > lifeMs);
		int i = 0;
		for (Ring r : rings) {
			float t = (now - r.start) / (float) lifeMs;
			float ease = 1f - (1f - t) * (1f - t);
			float rad = maxRadius.getF() * ease;
			int alpha = (int) (opacity.getI() * (1f - t));
			int col = Colors.alpha(switch (colour.get()) {
				case "Rainbow" -> Colors.rainbow(i * 0.12f);
				case "Static" -> 0xFF4DF0FF;
				default -> Colors.wave(i * 0.15f);
			}, alpha);

			double cx = follow.get() ? mc.player.getX() : r.x;
			double cz = follow.get() ? mc.player.getZ() : r.z;
			double cy = (follow.get() ? mc.player.getY() : r.y) + 0.03;
			annulus(mat, cx, cy, cz, rad, thickness.getF(), col);
			i++;
		}
		Fx.end();
	}

	private void annulus(Matrix4f m, double cx, double cy, double cz, float r, float th, int col) {
		if (r <= th) return;
		final int SEG = 64;
		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i <= SEG; i++) {
			double ang = i * (Math.PI * 2.0 / SEG);
			float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
			Fx.vc(b, m, (float) (cx + ca * (r - th)), (float) cy, (float) (cz + sa * (r - th)), col);
			Fx.vc(b, m, (float) (cx + ca * (r + th)), (float) cy, (float) (cz + sa * (r + th)), col);
		}
		Fx.draw();
	}
}
