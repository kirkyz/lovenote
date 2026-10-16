package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.Fx;
import com.lovenote.util.Tex;
import com.mojang.blaze3d.systems.RenderSystem;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

/** Glowing column plus a rising spiral of sparks around you. */
public class Halo extends Module {

	private final ModeSetting style = add(new ModeSetting("Style", "Both", "Beam", "Spiral", "Both"));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Health", "Rainbow"));
	private final NumberSetting radius = add(new NumberSetting("Radius", 0.85, 0.2, 3.0, 0.05));
	private final NumberSetting height = add(new NumberSetting("Height", 2.6, 0.5, 6.0, 0.1));
	private final NumberSetting spin = add(new NumberSetting("Spin", 1.0, 0.0, 4.0, 0.05));
	private final NumberSetting sparks = add(new NumberSetting("Sparks", 34, 0, 120, 1));
	private final NumberSetting sparkSize = add(new NumberSetting("Spark size", 0.35, 0.1, 1.5, 0.05));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 90, 10, 255, 5));
	private final BoolSetting pulse = add(new BoolSetting("Pulse", true));

	public Halo() {
		super("Halo", Category.VISUALS);
		describe("Glowing column and rising spark spiral.");
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		PlayerEntity p = mc.player;
		if (p == null) return;
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());

		float td = ctx.tickDelta();
		double x = MathHelper.lerp(td, p.lastX, p.getX());
		double y = MathHelper.lerp(td, p.lastY, p.getY());
		double z = MathHelper.lerp(td, p.lastZ, p.getZ());

		float time = (System.currentTimeMillis() % 100000L) / 1000f;
		Matrix4f mat = m.peek().getPositionMatrix();
		float r = radius.getF();
		float h = height.getF();
		int base = base();
		int a = opacity.getI();

		Fx.additive();
		Fx.texture(Tex.SOFT);

		if (!style.is("Spiral")) beam(mat, x, y, z, r, h, time, base, a);
		if (!style.is("Beam")) spiral(mat, x, y, z, r, h, time, base, a);

		Fx.end();
	}

	private void beam(Matrix4f m, double x, double y, double z, float r, float h, float time, int base, int a) {
		final int SEG = 26;
		float pulseF = pulse.get() ? 1f + 0.12f * (float) Math.sin(time * 3.4f) : 1f;
		float rr = r * pulseF;
		float rot = time * spin.getF() * 0.3f;
		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < SEG; i++) {
			double a0 = i * (Math.PI * 2.0 / SEG) + rot;
			double a1 = (i + 1) * (Math.PI * 2.0 / SEG) + rot;
			float x0 = (float) (x + Math.cos(a0) * rr), z0 = (float) (z + Math.sin(a0) * rr);
			float x1 = (float) (x + Math.cos(a1) * rr), z1 = (float) (z + Math.sin(a1) * rr);
			int bot = Colors.alpha(base, a);
			int top = Colors.alpha(base, 0);
			Fx.vt(b, m, x0, (float) y, z0, 0f, 1f, bot);
			Fx.vt(b, m, x1, (float) y, z1, 1f, 1f, bot);
			Fx.vt(b, m, x1, (float) (y + h), z1, 1f, 0f, top);
			Fx.vt(b, m, x0, (float) (y + h), z0, 0f, 0f, top);
		}
		Fx.draw();
	}

	private void spiral(Matrix4f m, double x, double y, double z, float r, float h, float time, int base, int a) {
		int n = Math.max(1, sparks.getI());
		float sz = sparkSize.getF();
		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < n; i++) {
			float f = i / (float) n;
			float yy = (f * h + time * 0.6f * spin.getF()) % h;
			float ang = (float) (f * Math.PI * 4.0 + time * spin.getF() * 1.6f);
			float rad = r * (0.85f + 0.25f * (float) Math.sin(f * 9.0 + time * 1.3f));
			int c = Colors.alpha(shift(base, f, time), (int) (a * (1f - yy / h) * 0.9f));
			Fx.billboard(b, m,
				(float) (x + Math.cos(ang) * rad),
				(float) (y + yy),
				(float) (z + Math.sin(ang) * rad), sz, c);
		}
		Fx.draw();
	}

	private int base() {
		return switch (colour.get()) {
			case "Health" -> Colors.health(mc.player == null ? 1f : mc.player.getHealth() / mc.player.getMaxHealth());
			case "Rainbow" -> Colors.rainbow(0f);
			default -> Colors.wave(0f);
		};
	}

	private int shift(int base, float f, float time) {
		return switch (colour.get()) {
			case "Health" -> base;
			case "Rainbow" -> Colors.rainbow(f * 0.5f + time * 0.05f);
			default -> Colors.skeet(f);
		};
	}
}
