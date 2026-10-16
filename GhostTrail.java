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
import org.joml.Matrix4f;

import java.util.ArrayList;

/** Fading afterimages left behind where you have been. */
public class GhostTrail extends Module {

	private final NumberSetting length = add(new NumberSetting("Length", 16, 2, 60, 1));
	private final NumberSetting interval = add(new NumberSetting("Interval (ms)", 40, 10, 200, 5));
	private final NumberSetting width = add(new NumberSetting("Width", 0.7, 0.1, 2.5, 0.05));
	private final NumberSetting height = add(new NumberSetting("Height", 1.85, 0.3, 3.0, 0.05));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 120, 5, 255, 5));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "Health", "White"));
	private final BoolSetting pulse = add(new BoolSetting("Pulse", true));

	private final ArrayList<double[]> pts = new ArrayList<>();
	private long lastSample;

	public GhostTrail() {
		super("GhostTrail", Category.VISUALS);
		describe("Fading afterimages of where you have been.");
	}

	@Override
	public void onDisable() {
		pts.clear();
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		long now = System.currentTimeMillis();
		if (now - lastSample < interval.getI()) return;
		lastSample = now;
		pts.add(new double[]{mc.player.getX(), mc.player.getY(), mc.player.getZ()});
		while (pts.size() > length.getI()) pts.remove(0);
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (pts.isEmpty()) return;
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());
		Matrix4f mat = m.peek().getPositionMatrix();
		long now = System.currentTimeMillis();
		int n = pts.size();
		int op = opacity.getI();
		float w = width.getF(), h = height.getF();

		Fx.additive();
		Fx.texture(Tex.SOFT);
		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < n; i++) {
			double[] q = pts.get(i);
			float f = (i + 1) / (float) n;
			float pulseF = pulse.get() ? 0.7f + 0.3f * (float) Math.sin(now / 180.0 + i * 0.6) : 1f;
			int a = (int) (op * f * f * pulseF);
			if (a < 3) continue;
			tallQuad(b, mat, (float) q[0], (float) q[1], (float) q[2], w * (0.5f + 0.5f * f), h, Colors.alpha(colour(f), a));
		}
		Fx.draw();
		Fx.end();
	}

	private int colour(float f) {
		return switch (colour.get()) {
			case "Rainbow" -> Colors.rainbow(f);
			case "Health" -> Colors.health(mc.player == null ? 1f : mc.player.getHealth() / mc.player.getMaxHealth());
			case "White" -> 0xFFFFFFFF;
			default -> Colors.skeet(1f - f);
		};
	}

	private static void tallQuad(BufferBuilder b, Matrix4f m, float x, float y, float z, float w, float h, int c) {
		float hw = w * 0.5f, hh = h * 0.5f;
		float cx = x, cy = y + hh, cz = z;
		Fx.vt(b, m, cx - Fx.RIGHT.x * hw - Fx.UP.x * hh, cy - Fx.RIGHT.y * hw - Fx.UP.y * hh, cz - Fx.RIGHT.z * hw - Fx.UP.z * hh, 0f, 1f, c);
		Fx.vt(b, m, cx + Fx.RIGHT.x * hw - Fx.UP.x * hh, cy + Fx.RIGHT.y * hw - Fx.UP.y * hh, cz + Fx.RIGHT.z * hw - Fx.UP.z * hh, 1f, 1f, c);
		Fx.vt(b, m, cx + Fx.RIGHT.x * hw + Fx.UP.x * hh, cy + Fx.RIGHT.y * hw + Fx.UP.y * hh, cz + Fx.RIGHT.z * hw + Fx.UP.z * hh, 1f, 0f, c);
		Fx.vt(b, m, cx - Fx.RIGHT.x * hw + Fx.UP.x * hh, cy - Fx.RIGHT.y * hw + Fx.UP.y * hh, cz - Fx.RIGHT.z * hw + Fx.UP.z * hh, 0f, 0f, c);
	}
}