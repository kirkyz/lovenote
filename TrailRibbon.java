package com.lovenote.module.impl;

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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

/** Camera-facing ribbon that trails behind you. */
public class TrailRibbon extends Module {

	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "Static"));
	private final NumberSetting width = add(new NumberSetting("Width", 0.32, 0.05, 1.5, 0.05));
	private final NumberSetting length = add(new NumberSetting("Length", 45, 5, 140, 1));
	private final NumberSetting height = add(new NumberSetting("Height", 1.0, 0.1, 3.0, 0.05));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 150, 10, 255, 5));

	private final Deque<Vec3d> points = new ArrayDeque<>();
	private Vec3d last;

	public TrailRibbon() {
		super("TrailRibbon", Category.VISUALS);
		describe("Camera-facing ribbon trailing behind you.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) { points.clear(); last = null; return; }
		Vec3d p = new Vec3d(mc.player.getX(), mc.player.getY() + height.get(), mc.player.getZ());
		if (last != null && p.squaredDistanceTo(last) > 12.0) { points.clear(); last = null; }
		if (last == null || p.squaredDistanceTo(last) > 0.0025) {
			points.addLast(p);
			last = p;
			while (points.size() > length.getI()) points.removeFirst();
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (points.size() < 3) return;
		Tex.ensure();
		Matrix4f mat = m.peek().getPositionMatrix();
		Vec3d cam = ctx.camera().getCameraPos();
		Vec3d[] pts = points.toArray(new Vec3d[0]);
		int n = pts.length;

		Fx.additive();
		Fx.texture(Tex.SOFT);
		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < n; i++) {
			Vec3d cur = pts[i];
			Vec3d prev = pts[Math.max(0, i - 1)];
			Vec3d next = pts[Math.min(n - 1, i + 1)];
			Vec3d side = next.subtract(prev).crossProduct(cam.subtract(cur));
			double sl = side.length();
			side = sl < 1e-5 ? new Vec3d(1, 0, 0) : side.multiply(1.0 / sl);

			double f = i / (double) (n - 1);
			double w = width.get() * f;
			int col = Colors.alpha(switch (colour.get()) {
				case "Rainbow" -> Colors.rainbow((float) (f * 0.6f));
				case "Static" -> 0xFF9A6BFF;
				default -> Colors.skeet((float) f);
			}, (int) (opacity.getI() * f * f));

			Vec3d l = cur.add(side.multiply(w));
			Vec3d r = cur.subtract(side.multiply(w));
			Fx.vt(b, mat, (float) l.x, (float) l.y, (float) l.z, 0f, 1f, col);
			Fx.vt(b, mat, (float) r.x, (float) r.y, (float) r.z, 1f, 1f, col);
		}
		Fx.draw();
		Fx.end();
	}
}
