package com.lovenote.module.impl;
import net.minecraft.client.render.RenderLayer;
import com.lovenote.util.Gl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.Fx;
import com.lovenote.util.Tex;
import com.mojang.blaze3d.systems.RenderSystem;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/** Additive bloom around the sun and moon with a horizon haze band. */
public class SunGlow extends Module {
	private final ModeSetting body = add(new ModeSetting("Body", "Both", "Sun", "Moon", "Both"));
	private final NumberSetting size = add(new NumberSetting("Size", 34, 8, 90, 2));
	private final NumberSetting distance = add(new NumberSetting("Distance", 110, 40, 220, 10));
	private final NumberSetting strength = add(new NumberSetting("Strength", 120, 20, 255, 5));
	private final NumberSetting haze = add(new NumberSetting("Horizon haze", 70, 0, 160, 5));

	public SunGlow() {
		super("SunGlow", Category.VISUALS);
		describe("Additive glow around the sun and moon with a horizon haze.");
	}

	private void disc(Matrix4f mat, double x, double y, double z, float size, int c) {
		BufferBuilder b = Fx.texQuads();
		Fx.billboard(b, mat, (float) x, (float) y, (float) z, size, c);
		Fx.draw();
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null || mc.world == null) return;
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());
		Fx.texture(Tex.SOFT);
		Fx.additive();

		Matrix4f mat = m.peek().getPositionMatrix();
		float ang = Gl.skyAngleRadians(mc.world, ctx.tickDelta());
		double dx = -Math.sin(ang), dy = Math.cos(ang);
		double d = distance.get();
		double cx = mc.player.getX(), cy = mc.player.getY(), cz = mc.player.getZ();
		float sz = (float) size.get();

		if (!body.get().equals("Moon")) {
			disc(mat, cx + dx * d, cy + dy * d, cz, sz, Colors.alpha(Colors.wave(0f), strength.getI()));
			disc(mat, cx + dx * d * 0.7, cy + dy * d * 0.7, cz, sz * 2.1f,
					Colors.alpha(Colors.wave(0.1f), (int) (strength.get() * 0.35f)));
		}
		if (!body.get().equals("Sun")) {
			disc(mat, cx - dx * d, cy - dy * d, cz, sz, Colors.alpha(0xFFBBD8FF, (int) (strength.get() * 0.7f)));
		}
		if (haze.getI() > 0) {
			double hd = d * 0.55;
			disc(mat, cx + dx * hd, cy + 1.0, cz, sz * 5.5f, Colors.alpha(Colors.C2, haze.getI()));
		}
		Fx.end();
	}
}