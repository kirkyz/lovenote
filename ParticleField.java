package com.lovenote.module.impl;
import net.minecraft.client.render.RenderLayer;
import com.lovenote.util.Gl;

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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Random;

/** Ambient particle field drifting around you. */
public class ParticleField extends Module {

	private final ModeSetting mode = add(new ModeSetting("Mode", "Ambient",
			"Ambient", "Rising", "Snow", "Embers", "Orbit", "Bubbles"));
	private final ModeSetting sprite = add(new ModeSetting("Sprite", "Soft", "Soft", "Glow", "Star"));
	private final ModeSetting colourMode = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "Static", "White"));

	private final NumberSetting count = add(new NumberSetting("Count", 260, 10, 1200, 10));
	private final NumberSetting size = add(new NumberSetting("Size", 0.22, 0.03, 1.2, 0.01));
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.45, 0.0, 3.0, 0.05));
	private final NumberSetting spread = add(new NumberSetting("Spread", 22, 4, 60, 1));
	private final NumberSetting height = add(new NumberSetting("Height", 16, 3, 48, 1));
	private final NumberSetting wind = add(new NumberSetting("Wind", 0.25, -2.0, 2.0, 0.05));
	private final NumberSetting turbulence = add(new NumberSetting("Turbulence", 0.35, 0.0, 2.0, 0.05));
	private final NumberSetting twinkle = add(new NumberSetting("Twinkle", 0.8, 0.0, 3.0, 0.1));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 150, 10, 255, 5));
	private final NumberSetting fadeNear = add(new NumberSetting("Fade near", 3, 0, 20, 1));
	private final BoolSetting walls = add(new BoolSetting("Through walls", true));

	private float[] px, py, pz, ph, ps;
	private int built = -1;
	private long last;

	public ParticleField() {
		super("ParticleField", Category.VISUALS);
		describe("Ambient particle field drifting around you.");
	}

	private void build(int n) {
		if (built == n && px != null) return;
		built = n;
		px = new float[n]; py = new float[n]; pz = new float[n];
		ph = new float[n]; ps = new float[n];
		Random r = new Random(0xC0FFEE);
		float sp = spread.getF();
		for (int i = 0; i < n; i++) {
			px[i] = (r.nextFloat() * 2f - 1f) * sp;
			py[i] = (r.nextFloat() * 2f - 1f) * 6f;
			pz[i] = (r.nextFloat() * 2f - 1f) * sp;
			ph[i] = r.nextFloat() * 6.283f;
			ps[i] = 0.4f + r.nextFloat() * 0.9f;
		}
	}

	private static float sq(float v) { return v * v; }

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
		String md = mode.get();

		if (md.equals("Orbit")) {
			for (int i = 0; i < n; i++) {
				float ang = ph[i] + time * 0.5f * speed.getF();
				float rad = sp * (0.30f + 0.70f * ((i % 7) / 7f));
				px[i] = (float) (cx + Math.cos(ang) * rad);
				pz[i] = (float) (cz + Math.sin(ang) * rad);
				py[i] = (float) (cy + 0.3 + ((i % 9) / 9f) * h * 0.5);
			}
			return;
		}

		float up = switch (md) {
			case "Snow" -> -1f;
			case "Rising", "Embers" -> 2.0f;
			case "Bubbles" -> 1.4f;
			default -> 0.6f;
		};
		float turb = turbulence.getF();
		float wnd = wind.getF();

		for (int i = 0; i < n; i++) {
			float s = speed.getF() * ps[i];
			py[i] += up * s * dt;
			px[i] += (float) Math.sin(time * 0.7f + ph[i]) * turb * dt + wnd * dt;
			pz[i] += (float) Math.cos(time * 0.6f + ph[i] * 1.3f) * turb * dt;
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
		Vec3d cam = ctx.camera().getCameraPos();
		float time = (System.currentTimeMillis() % 100000L) / 1000f;
		float base = size.getF();
		int op = opacity.getI();
		float near = fadeNear.getF();
		float twk = twinkle.getF();

		Identifier tex = sprite.is("Star") ? Tex.STAR : sprite.is("Glow") ? Tex.GLOW : Tex.SOFT;

		Fx.additive();
		Fx.texture(tex);

		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < n; i++) {
			float tw = twk > 0f ? 0.55f + 0.45f * (float) Math.sin(time * twk * 2f + ph[i] * 3f) : 1f;
			float d = (float) Math.sqrt(sq(px[i] - (float) cam.x) + sq(py[i] - (float) cam.y) + sq(pz[i] - (float) cam.z));
			float depth = MathHelper.clamp(1f - (d - near) / 26f, 0.05f, 1f);
			int a = (int) (op * tw * depth);
			if (a < 3) continue;
			float sz = base * (0.55f + 0.75f * ps[i]) * (0.6f + 0.4f * depth);
			Fx.billboard(b, mat, px[i], py[i], pz[i], sz, Colors.alpha(colour(i, time), a));
		}
		Fx.draw();

		Fx.end();
	}

	private int colour(int i, float time) {
		return switch (colourMode.get()) {
			case "Rainbow" -> Colors.rainbow(i * 0.03f + time * 0.02f);
			case "White" -> 0xFFFFFFFF;
			case "Static" -> 0xFFB18CFF;
			default -> Colors.skeet((i % 12) / 12f);
		};
	}
}