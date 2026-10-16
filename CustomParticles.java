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
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.Random;

/** Fully configurable shaped particles: sprites plus camera-facing geometry shapes. */
public class CustomParticles extends Module {

	private final ModeSetting shape = add(new ModeSetting("Shape", "Star", "Glow", "Soft", "Star", "Plus", "Diamond", "Ring"));
	private final ModeSetting motion = add(new ModeSetting("Motion", "Float", "Float", "Rise", "Fall", "Orbit", "Vortex", "Rain"));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "Health", "White", "Static"));
	private final NumberSetting count = add(new NumberSetting("Count", 140, 5, 900, 5));
	private final NumberSetting size = add(new NumberSetting("Size", 0.42, 0.04, 2.0, 0.02));
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.6, 0.0, 4.0, 0.05));
	private final NumberSetting spread = add(new NumberSetting("Spread", 18, 3, 60, 1));
	private final NumberSetting height = add(new NumberSetting("Height", 14, 3, 48, 1));
	private final NumberSetting spin = add(new NumberSetting("Spin", 1.0, 0.0, 6.0, 0.1));
	private final NumberSetting twinkle = add(new NumberSetting("Twinkle", 0.9, 0.0, 3.0, 0.1));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 170, 10, 255, 5));
	private final BoolSetting halo = add(new BoolSetting("Glow halo", true));
	private final BoolSetting walls = add(new BoolSetting("Through walls", true));

	private float[] px, py, pz, ph, ps;
	private int built = -1;
	private long last;

	public CustomParticles() {
		super("CustomParticles", Category.VISUALS);
		describe("Configurable particle shapes drifting around you.");
	}

	private void build(int n) {
		if (built == n && px != null) return;
		built = n;
		px = new float[n]; py = new float[n]; pz = new float[n];
		ph = new float[n]; ps = new float[n];
		Random r = new Random(0x5EEDCAFE);
		float sp = spread.getF();
		for (int i = 0; i < n; i++) {
			px[i] = (r.nextFloat() * 2f - 1f) * sp;
			py[i] = (r.nextFloat() * 2f - 1f) * 6f;
			pz[i] = (r.nextFloat() * 2f - 1f) * sp;
			ph[i] = r.nextFloat() * 6.283f;
			ps[i] = 0.4f + r.nextFloat() * 0.9f;
		}
	}

	private void wrap(int i, double cx, double cy, double cz, float sp, float h) {
		float dx = px[i] - (float) cx;
		if (dx > sp) px[i] -= sp * 2f; else if (dx < -sp) px[i] += sp * 2f;
		float dz = pz[i] - (float) cz;
		if (dz > sp) pz[i] -= sp * 2f; else if (dz < -sp) pz[i] += sp * 2f;
		float dy = py[i] - (float) cy;
		if (dy > h) py[i] -= h * 2f; else if (dy < -h) py[i] += h * 2f;
	}

	private void update(float dt, double cx, double cy, double cz, float sp, float h) {
		int n = count.getI();
		float time = (System.currentTimeMillis() % 100000L) / 1000f;
		String md = motion.get();
		float spd = speed.getF();
		float spinV = spin.getF();

		if (md.equals("Orbit")) {
			for (int i = 0; i < n; i++) {
				float ang = ph[i] + time * 0.5f * spd * spinV;
				float rad = sp * (0.25f + 0.75f * ((i % 9) / 9f));
				px[i] = (float) (cx + Math.cos(ang) * rad);
				pz[i] = (float) (cz + Math.sin(ang) * rad);
				py[i] = (float) (cy + 0.4 + ((i % 11) / 11f) * h * 0.6);
			}
			return;
		}

		if (md.equals("Vortex")) {
			for (int i = 0; i < n; i++) {
				float f = (time * 0.25f * spd + ps[i]) % 1f;
				float ang = ph[i] + time * 1.4f * spd * spinV;
				float rad = sp * (0.15f + 0.85f * (1f - f));
				px[i] = (float) (cx + Math.cos(ang) * rad);
				pz[i] = (float) (cz + Math.sin(ang) * rad);
				py[i] = (float) (cy + f * h);
			}
			return;
		}

		float up = switch (md) {
			case "Rise" -> 1.6f;
			case "Fall" -> -1.6f;
			case "Rain" -> -3.2f;
			default -> 0.25f;
		};
		float wind = md.equals("Rain") ? 0.5f : 0f;

		for (int i = 0; i < n; i++) {
			float s = spd * ps[i];
			py[i] += up * s * dt;
			px[i] += (float) Math.sin(time * 0.8f + ph[i]) * 0.6f * dt + wind * dt;
			pz[i] += (float) Math.cos(time * 0.7f + ph[i] * 1.4f) * 0.6f * dt;
			wrap(i, cx, cy, cz, sp, h);
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		PlayerEntity p = mc.player;
		if (p == null) return;
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());

		int n = count.getI();
		build(n);
		float sp = spread.getF();
		float h = height.getF();

		long now = System.nanoTime();
		float dt = last == 0 ? 0.016f : Math.min(0.1f, (now - last) / 1_000_000_000f);
		last = now;

		float td = ctx.tickDelta();
		double cx = MathHelper.lerp(td, p.lastX, p.getX());
		double cy = MathHelper.lerp(td, p.lastY, p.getY());
		double cz = MathHelper.lerp(td, p.lastZ, p.getZ());
		update(dt, cx, cy, cz, sp, h);

		Matrix4f mat = m.peek().getPositionMatrix();
		float time = (System.currentTimeMillis() % 100000L) / 1000f;
		float base = size.getF();
		int op = opacity.getI();
		float twk = twinkle.getF();
		String sh = shape.get();
		boolean geom = sh.equals("Star") || sh.equals("Plus") || sh.equals("Diamond") || sh.equals("Ring");

		Fx.additive();

		if (!geom || halo.get()) {
			Fx.texture(sh.equals("Soft") ? Tex.SOFT : Tex.GLOW);
			BufferBuilder b = Fx.texQuads();
			for (int i = 0; i < n; i++) {
				float tw = twk > 0f ? 0.5f + 0.5f * (float) Math.sin(time * twk * 2f + ph[i] * 3f) : 1f;
				int a = (int) (op * tw);
				if (a < 3) continue;
				float sz = base * (0.5f + 0.8f * ps[i]) * (geom ? 1.7f : 1f);
				Fx.billboard(b, mat, px[i], py[i], pz[i], sz, Colors.alpha(colour(i, time), a));
			}
			Fx.draw();
		}

		if (geom) {
			BufferBuilder b = Fx.texQuads();
			for (int i = 0; i < n; i++) {
				float tw = twk > 0f ? 0.5f + 0.5f * (float) Math.sin(time * twk * 2f + ph[i] * 3f) : 1f;
				int a = (int) (op * tw);
				if (a < 3) continue;
				float sz = base * (0.5f + 0.8f * ps[i]);
				drawShape(b, mat, sh, px[i], py[i], pz[i], sz, Colors.alpha(colour(i, time), a));
			}
			Fx.draw();
		}

		Fx.end();
	}

	private int colour(int i, float time) {
		return switch (colour.get()) {
			case "Rainbow" -> Colors.rainbow(i * 0.03f + time * 0.02f);
			case "Health" -> Colors.health(mc.player == null ? 1f : mc.player.getHealth() / mc.player.getMaxHealth());
			case "White" -> 0xFFFFFFFF;
			case "Static" -> 0xFFB18CFF;
			default -> Colors.skeet((i % 12) / 12f);
		};
	}

	private static void bb(BufferBuilder b, Matrix4f m, float x, float y, float z, float u, float v, float size, int c) {
		Fx.vc(b, m,
				x + Fx.RIGHT.x * u * size + Fx.UP.x * v * size,
				y + Fx.RIGHT.y * u * size + Fx.UP.y * v * size,
				z + Fx.RIGHT.z * u * size + Fx.UP.z * v * size, c);
	}

	private static void drawShape(BufferBuilder b, Matrix4f m, String s, float x, float y, float z, float size, int c) {
		switch (s) {
			case "Plus" -> {
				bb(b, m, x, y, z, -0.26f, -1f, size, c); bb(b, m, x, y, z, 0.26f, -1f, size, c);
				bb(b, m, x, y, z, 0.26f, 1f, size, c); bb(b, m, x, y, z, -0.26f, 1f, size, c);
				bb(b, m, x, y, z, -1f, -0.26f, size, c); bb(b, m, x, y, z, -1f, 0.26f, size, c);
				bb(b, m, x, y, z, 1f, 0.26f, size, c); bb(b, m, x, y, z, 1f, -0.26f, size, c);
			}
			case "Diamond" -> {
				bb(b, m, x, y, z, 0f, -1f, size, c); bb(b, m, x, y, z, 1f, 0f, size, c);
				bb(b, m, x, y, z, 0f, 1f, size, c); bb(b, m, x, y, z, -1f, 0f, size, c);
			}
			case "Ring" -> {
				int seg = 18;
				float in = 0.52f;
				for (int i = 0; i < seg; i++) {
					float a0 = (float) (i * Math.PI * 2.0 / seg);
					float a1 = (float) ((i + 1) * Math.PI * 2.0 / seg);
					bb(b, m, x, y, z, MathHelper.cos(a0) * in, MathHelper.sin(a0) * in, size, c);
					bb(b, m, x, y, z, MathHelper.cos(a0), MathHelper.sin(a0), size, c);
					bb(b, m, x, y, z, MathHelper.cos(a1), MathHelper.sin(a1), size, c);
					bb(b, m, x, y, z, MathHelper.cos(a1) * in, MathHelper.sin(a1) * in, size, c);
				}
			}
			case "Star" -> {
				bb(b, m, x, y, z, 0f, -1f, size, c); bb(b, m, x, y, z, 1f, 0f, size, c);
				bb(b, m, x, y, z, 0f, 1f, size, c); bb(b, m, x, y, z, -1f, 0f, size, c);
				float k = 0.7071f;
				bb(b, m, x, y, z, k, -k, size, c); bb(b, m, x, y, z, k, k, size, c);
				bb(b, m, x, y, z, -k, k, size, c); bb(b, m, x, y, z, -k, -k, size, c);
			}
			default -> {}
		}
	}
}
