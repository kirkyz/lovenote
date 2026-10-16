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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.Random;

/** Huge soft colour clouds drifting high above you, with optional sparkles. */
public class Nebula extends Module {

	private final NumberSetting blobs = add(new NumberSetting("Blobs", 46, 6, 160, 2));
	private final NumberSetting size = add(new NumberSetting("Size", 90, 20, 260, 5));
	private final NumberSetting distance = add(new NumberSetting("Distance", 150, 60, 320, 5));
	private final NumberSetting height = add(new NumberSetting("Height", 95, 20, 200, 5));
	private final NumberSetting drift = add(new NumberSetting("Drift", 0.6, 0.0, 3.0, 0.05));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 70, 5, 200, 5));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "Nebula", "White"));
	private final BoolSetting sparkles = add(new BoolSetting("Sparkles", true));

	private float[] bx, bz, by, bp, bs;
	private int built = -1;

	public Nebula() {
		super("Nebula", Category.RENDER);
		describe("Drifting colour clouds high in the sky.");
	}

	private void build(int n) {
		if (built == n && bx != null) return;
		built = n;
		bx = new float[n]; bz = new float[n]; by = new float[n]; bp = new float[n]; bs = new float[n];
		Random r = new Random(0xBEEF77);
		for (int i = 0; i < n; i++) {
			float a = r.nextFloat() * 6.283f;
			float rad = 0.35f + r.nextFloat() * 0.65f;
			bx[i] = MathHelper.cos(a) * rad;
			bz[i] = MathHelper.sin(a) * rad;
			by[i] = 0.2f + r.nextFloat() * 0.8f;
			bp[i] = r.nextFloat() * 6.283f;
			bs[i] = 0.5f + r.nextFloat() * 0.9f;
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		PlayerEntity p = mc.player;
		if (p == null) return;
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());

		int n = blobs.getI();
		build(n);
		float dist = distance.getF();
		float hgt = height.getF();
		float time = (System.currentTimeMillis() % 100000L) / 1000f;
		float td = ctx.tickDelta();
		double cx = MathHelper.lerp(td, p.lastX, p.getX());
		double cy = MathHelper.lerp(td, p.lastY, p.getY());
		double cz = MathHelper.lerp(td, p.lastZ, p.getZ());
		Matrix4f mat = m.peek().getPositionMatrix();
		int op = opacity.getI();

		Fx.additive();
		Fx.texture(Tex.SOFT);

		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < n; i++) {
			float ang = time * drift.getF() * 0.05f * ((i % 2 == 0) ? 1f : -1f);
			float ca = MathHelper.cos(ang), sa = MathHelper.sin(ang);
			float ox = bx[i] * ca - bz[i] * sa;
			float oz = bx[i] * sa + bz[i] * ca;
			int a = (int) (op * (0.6f + 0.4f * MathHelper.sin(time * 0.4f + bp[i])));
			if (a < 3) continue;
			Fx.billboard(b, mat, (float) cx + ox * dist, (float) cy + hgt * by[i], (float) cz + oz * dist,
					size.getF() * bs[i], Colors.alpha(colour(i, time), a));
		}
		Fx.draw();

		if (sparkles.get()) {
			Fx.texture(Tex.STAR);
			BufferBuilder s = Fx.texQuads();
			for (int i = 0; i < n; i++) {
				float ang = time * drift.getF() * 0.05f * ((i % 2 == 0) ? 1f : -1f);
				float ca = MathHelper.cos(ang), sa = MathHelper.sin(ang);
				float ox = bx[i] * ca - bz[i] * sa;
				float oz = bx[i] * sa + bz[i] * ca;
				float tw = 0.4f + 0.6f * MathHelper.sin(time * 1.3f + bp[i] * 2f);
				int a = (int) (200 * tw);
				if (a < 4) continue;
				Fx.billboard(s, mat, (float) cx + ox * dist, (float) cy + hgt * by[i] + 30f, (float) cz + oz * dist,
						6f * bs[i], Colors.alpha(0xFFFFFFFF, a));
			}
			Fx.draw();
		}

		Fx.end();
	}

	private int colour(int i, float time) {
		return switch (colour.get()) {
			case "Rainbow" -> Colors.rainbow(i * 0.02f + time * 0.01f);
			case "White" -> 0xFFFFFFFF;
			case "Nebula" -> Colors.hsv(0.75f + (i % 7) * 0.02f + time * 0.005f, 0.75f, 1f);
			default -> Colors.skeet((i % 10) / 10f);
		};
	}
}