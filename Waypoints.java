package com.lovenote.module.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/** Saved points with beams and distance labels. Click "Add here" to drop one. */
public class Waypoints extends Module {
	private final NumberSetting height = add(new NumberSetting("Beam height", 32, 8, 96, 4));
	private final NumberSetting range = add(new NumberSetting("Label range", 96, 8, 256, 8));
	private final BoolSetting labels = add(new BoolSetting("Labels", true));
	private final BoolSetting showDistance = add(new BoolSetting("Show distance", true));
	private final BoolSetting add = add(new BoolSetting("Add here", false));
	private final BoolSetting clear = add(new BoolSetting("Clear all", false));

	private final List<double[]> points = new ArrayList<>();

	public Waypoints() {
		super("Waypoints", Category.VISUALS);
		describe("Saved points with beams and distance labels.");
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		if (add.get()) {
			points.add(new double[]{mc.player.getX(), mc.player.getY(), mc.player.getZ()});
			if (points.size() > 32) points.remove(0);
			add.toggle();
		}
		if (clear.get()) {
			points.clear();
			clear.toggle();
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null) return;
		int i = 0;
		for (double[] p : points) {
			int c = Colors.wave(i++ * 0.06f);
			Box b = new Box(p[0] - 0.06, p[1], p[2] - 0.06, p[0] + 0.06, p[1] + height.getI(), p[2] + 0.06);
			RenderUtil.box(m, b, Colors.alpha(c, 50), c);
		}
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (!labels.get() || mc.player == null || points.isEmpty()) return;
		var cam = mc.gameRenderer.getCamera();
		Matrix4f view = new Matrix4f();
		view.rotateX((float) Math.toRadians(cam.getPitch()));
		view.rotateY((float) Math.toRadians(cam.getYaw() + 180f));
		view.translate((float) -cam.getCameraPos().x, (float) -cam.getCameraPos().y, (float) -cam.getCameraPos().z);
		Matrix4f proj = mc.gameRenderer.getBasicProjectionMatrix(mc.options.getFov().getValue());
		Matrix4f mvp = new Matrix4f(proj).mul(view);

		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		for (int i = 0; i < points.size(); i++) {
			double[] p = points.get(i);
			double d = new Vec3d(p[0], p[1], p[2]).distanceTo(mc.player.getEntityPos());
			if (d > range.getI()) continue;

			Vector4f v = new Vector4f((float) p[0], (float) (p[1] + height.getI()), (float) p[2], 1f);
			v.mul(mvp);
			if (v.w <= 0.01f) continue;
			float sx = (v.x / v.w * 0.5f + 0.5f) * sw;
			float sy = (1f - (v.y / v.w * 0.5f + 0.5f)) * sh;
			if (sx < -100 || sx > sw + 100 || sy < -20 || sy > sh + 20) continue;

			String label = "WP" + (i + 1) + (showDistance.get() ? "  " + String.format("%.0fm", d) : "");
			int w = mc.textRenderer.getWidth(label);
			ctx.drawTextWithShadow(mc.textRenderer, label, (int) (sx - w / 2f), (int) sy, Colors.wave(i * 0.06f));
		}
	}

	@Override
	public void saveExtra(JsonObject o) {
		JsonArray arr = new JsonArray();
		for (double[] p : points) {
			JsonArray e = new JsonArray();
			e.add(p[0]);
			e.add(p[1]);
			e.add(p[2]);
			arr.add(e);
		}
		o.add("points", arr);
	}

	@Override
	public void loadExtra(JsonObject o) {
		points.clear();
		if (!o.has("points")) return;
		for (var el : o.getAsJsonArray("points")) {
			JsonArray e = el.getAsJsonArray();
			points.add(new double[]{e.get(0).getAsDouble(), e.get(1).getAsDouble(), e.get(2).getAsDouble()});
		}
	}
}