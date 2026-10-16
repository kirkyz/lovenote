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

/** Low lying mist rolling across the ground around you. */
public class GroundFog extends Module {

	private final NumberSetting radius = add(new NumberSetting("Radius", 26, 6, 60, 1));
	private final NumberSetting detail = add(new NumberSetting("Detail", 9, 3, 20, 1));
	private final NumberSetting height = add(new NumberSetting("Height", 0.55, 0.05, 3.0, 0.05));
	private final NumberSetting opacity = add(new NumberSetting("Opacity", 120, 10, 255, 5));
	private final NumberSetting speed = add(new NumberSetting("Speed", 0.35, 0.0, 2.0, 0.05));
	private final NumberSetting tile = add(new NumberSetting("Tile", 6.0, 1.0, 16.0, 0.5));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "White", "Grey"));
	private final BoolSetting wave = add(new BoolSetting("Wave", true));

	public GroundFog() {
		super("GroundFog", Category.RENDER);
		describe("Low lying mist rolling across the ground.");
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		PlayerEntity p = mc.player;
		if (p == null) return;
		Tex.ensure();

		Matrix4f mat = m.peek().getPositionMatrix();
		float R = radius.getF();
		int N = detail.getI();
		float cell = (R * 2f) / N;
		float td = ctx.tickDelta();
		double cx = MathHelper.lerp(td, p.lastX, p.getX());
		double cz = MathHelper.lerp(td, p.lastZ, p.getZ());
		float gy = (float) (p.getY() + 0.06 + height.getF() * 0.08);
		float time = (System.currentTimeMillis() % 100000L) / 1000f * speed.getF();
		float tl = tile.getF();
		int base = base();
		int op = opacity.getI();
		boolean wv = wave.get();

		Fx.additive();
		Fx.texture(Tex.SOFT);

		BufferBuilder b = Fx.texQuads();
		for (int gx = 0; gx < N; gx++) {
			for (int gz = 0; gz < N; gz++) {
				float x0 = (float) (cx - R + gx * cell);
				float z0 = (float) (cz - R + gz * cell);
				float x1 = x0 + cell, z1 = z0 + cell;
				float mx = (x0 + x1) * 0.5f - (float) cx;
				float mz = (z0 + z1) * 0.5f - (float) cz;
				float d = (float) Math.sqrt(mx * mx + mz * mz) / R;
				float fade = MathHelper.clamp(1f - d, 0f, 1f);
				float puls = wv ? 0.7f + 0.3f * MathHelper.sin(time * 2f + d * 6f) : 1f;
				int a = (int) (op * fade * fade * puls);
				if (a < 3) continue;
				int c = Colors.alpha(base, a);
				float u0 = (x0 / tl) + time * 0.1f, u1 = (x1 / tl) + time * 0.1f;
				float v0 = (z0 / tl) + time * 0.08f, v1 = (z1 / tl) + time * 0.08f;
				Fx.vt(b, mat, x0, gy, z0, u0, v1, c);
				Fx.vt(b, mat, x1, gy, z0, u1, v1, c);
				Fx.vt(b, mat, x1, gy, z1, u1, v0, c);
				Fx.vt(b, mat, x0, gy, z1, u0, v0, c);
			}
		}
		Fx.draw();
		Fx.end();
	}

	private int base() {
		return switch (colour.get()) {
			case "Rainbow" -> Colors.rainbow(0f);
			case "White" -> 0xFFFFFFFF;
			case "Grey" -> 0xFFB8B8C4;
			default -> Colors.wave(0f);
		};
	}
}