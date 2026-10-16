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
import org.joml.Matrix4f;

import java.util.Random;

/** Twinkling stars spread across the sky dome. */
public class Starfield extends Module {
	private final NumberSetting amount = add(new NumberSetting("Stars", 220, 20, 800, 10));
	private final NumberSetting radius = add(new NumberSetting("Radius", 90, 30, 220, 5));
	private final NumberSetting size = add(new NumberSetting("Size", 1.2, 0.4, 4.0, 0.1));
	private final NumberSetting height = add(new NumberSetting("Min height", 20, 0, 80, 2));
	private final ModeSetting colour = add(new ModeSetting("Colour", "White", "White", "Ice", "Gold", "Theme", "Rainbow"));
	private final BoolSetting twinkle = add(new BoolSetting("Twinkle", true));
	private final NumberSetting drift = add(new NumberSetting("Drift", 100, 0, 400, 20));

	private final Random rnd = new Random();
	private double[] xs, ys, zs;
	private int built = -1;

	public Starfield() {
		super("Starfield", Category.VISUALS);
		describe("Twinkling stars spread across the sky.");
	}

	private void build(int n) {
		xs = new double[n];
		ys = new double[n];
		zs = new double[n];
		for (int i = 0; i < n; i++) {
			double a = rnd.nextDouble() * Math.PI * 2;
			double el = 0.08 + rnd.nextDouble() * 1.3;
			double r = 0.35 + rnd.nextDouble() * 0.65;
			xs[i] = Math.cos(a) * Math.cos(el) * r;
			ys[i] = Math.sin(el) * r;
			zs[i] = Math.sin(a) * Math.cos(el) * r;
		}
		built = n;
	}

	private int pick(int i) {
		return switch (colour.get()) {
			case "Ice" -> Colors.lerp(0xFFFFFFFF, 0xFF8AD8FF, (i % 4) / 3f);
			case "Gold" -> Colors.lerp(0xFFFFFFFF, 0xFFFFD27A, (i % 4) / 3f);
			case "Theme" -> Colors.wave(i * 0.02f);
			case "Rainbow" -> Colors.rainbow(i * 0.02f);
			default -> 0xFFFFFFFF;
		};
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null) return;
		int n = amount.getI();
		if (xs == null || built != n) build(n);
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());
		Fx.texture(Tex.STAR);
		Fx.additive();

		Matrix4f mat = m.peek().getPositionMatrix();
		double t = System.currentTimeMillis() / 1000.0;
		double spin = t * (drift.get() / 100.0) * 0.05;
		double cs = Math.cos(spin), sn = Math.sin(spin);
		double R = radius.get();
		double baseY = mc.player.getY() + height.get();
		double cx = mc.player.getX(), cz = mc.player.getZ();
		float sz = (float) size.get();

		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < n; i++) {
			double x = xs[i] * R, y = ys[i] * R, z = zs[i] * R;
			double rx = x * cs - z * sn;
			double rz = x * sn + z * cs;
			float a = twinkle.get() ? 0.45f + 0.55f * (float) Math.abs(Math.sin(t * 1.7 + i * 0.7)) : 1f;
			int c = Colors.alpha(pick(i), (int) (a * 220));
			Fx.billboard(b, mat, (float) (cx + rx), (float) (baseY + y), (float) (cz + rz), sz, c);
		}
		Fx.draw();
		Fx.end();
	}
}