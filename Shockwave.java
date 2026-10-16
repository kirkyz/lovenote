package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.Fx;
import com.lovenote.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;

/** Expanding ring bursts on attack and optionally on jump. */
public class Shockwave extends Module {

	private final NumberSetting radius = add(new NumberSetting("Radius", 3.5, 0.5, 12.0, 0.1));
	private final NumberSetting duration = add(new NumberSetting("Duration (ms)", 600, 150, 2500, 25));
	private final NumberSetting segments = add(new NumberSetting("Segments", 48, 12, 120, 2));
	private final NumberSetting rings = add(new NumberSetting("Rings", 2, 1, 5, 1));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "White"));
	private final BoolSetting dome = add(new BoolSetting("Dome", true));
	private final BoolSetting onAttack = add(new BoolSetting("On attack", true));
	private final BoolSetting onJump = add(new BoolSetting("On jump", false));

	private static final class Wave {
		double x, y, z;
		long start;
	}

	private final ArrayList<Wave> waves = new ArrayList<>();
	private boolean wasAirborne;

	public Shockwave() {
		super("Shockwave", Category.VISUALS);
		describe("Expanding ring bursts when you attack or jump.");
	}

	private void spawn(double x, double y, double z) {
		Wave w = new Wave();
		w.x = x; w.y = y; w.z = z; w.start = System.currentTimeMillis();
		waves.add(w);
		if (waves.size() > 40) waves.remove(0);
	}

	@Override
	public void onAttack(Entity target) {
		if (target == null || !onAttack.get()) return;
		spawn(target.getX(), target.getY(), target.getZ());
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		boolean air = !mc.player.isOnGround();
		if (onJump.get() && air && !wasAirborne) spawn(mc.player.getX(), mc.player.getY(), mc.player.getZ());
		wasAirborne = air;
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (waves.isEmpty()) return;
		long now = System.currentTimeMillis();
		int dur = duration.getI();
		waves.removeIf(w -> now - w.start > dur);
		if (waves.isEmpty()) return;

		int base = base();
		int seg = segments.getI();

		for (Wave w : waves) {
			float t = (now - w.start) / (float) dur;
			for (int k = 0; k < rings.getI(); k++) {
				float tt = MathHelper.clamp(t - k * 0.12f, 0f, 1f);
				if (tt <= 0.001f) continue;
				float r = radius.getF() * tt;
				int a = (int) (255 * (1f - tt) * 0.9f);
				if (a < 4) continue;
				RenderUtil.ring(m, new Vec3d(w.x, w.y + 0.06, w.z), r, Colors.alpha(base, a), seg);
				if (dome.get()) dome(m, w.x, w.y, w.z, r, Colors.alpha(base, a / 2), seg);
			}
		}
	}

	private void dome(MatrixStack s, double x, double y, double z, float r, int c, int seg) {
		Matrix4f m = s.peek().getPositionMatrix();
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = Fx.colorQuads();
		for (int i = 0; i <= seg; i++) {
			double a = i * Math.PI / seg;
			Fx.vc(b, m, (float) (x + Math.cos(a) * r), (float) (y + Math.sin(a) * r), (float) z, c);
		}
		Fx.draw();
	}

	private int base() {
		return switch (colour.get()) {
			case "Rainbow" -> Colors.rainbow(0f);
			case "White" -> 0xFFFFFFFF;
			default -> Colors.wave(0f);
		};
	}
}