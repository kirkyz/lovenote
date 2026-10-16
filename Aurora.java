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
import org.joml.Matrix4f;

/** Aurora curtains drifting across the sky. */
public class Aurora extends Module {
	private final ModeSetting colour = add(new ModeSetting("Colour", "Aurora", "Aurora", "Violet", "Ice", "Theme", "Rainbow"));
	private final NumberSetting height = add(new NumberSetting("Height", 42, 16, 90, 2));
	private final NumberSetting span = add(new NumberSetting("Span", 70, 20, 140, 5));
	private final NumberSetting width = add(new NumberSetting("Width", 26, 8, 60, 2));
	private final NumberSetting bands = add(new NumberSetting("Bands", 9, 2, 24, 1));
	private final NumberSetting speed = add(new NumberSetting("Drift", 100, 0, 300, 10));
	private final NumberSetting brightness = add(new NumberSetting("Brightness", 110, 20, 255, 5));

	public Aurora() {
		super("Aurora", Category.VISUALS);
		describe("Drifting aurora curtains high in the sky.");
	}

	private int pick(int i) {
		return switch (colour.get()) {
			case "Violet" -> Colors.lerp(0xFF8A4DFF, 0xFFFF6FD8, (i % 5) / 4f);
			case "Ice" -> Colors.lerp(0xFF3BE8FF, 0xFF7DFFCF, (i % 5) / 4f);
			case "Theme" -> Colors.wave(i * 0.07f);
			case "Rainbow" -> Colors.rainbow(i * 0.07f);
			default -> Colors.lerp(0xFF39FF9C, 0xFF7A5CFF, (i % 5) / 4f);
		};
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null || mc.world == null) return;
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());
		Fx.texture(Tex.SOFT);
		Fx.additive();

		Matrix4f mat = m.peek().getPositionMatrix();
		double t = System.currentTimeMillis() / 1000.0 * (speed.get() / 100.0);
		double px = mc.player.getX(), pz = mc.player.getZ();
		double py = mc.player.getY() + height.get();
		int n = bands.getI();
		float spanF = (float) span.get();
		float wF = (float) width.get();
		float aF = (float) brightness.get();

		for (int i = 0; i < n; i++) {
			double ph = t + i * 0.7;
			float bx = (float) (px + Math.sin(ph * 0.5) * spanF);
			float bz = (float) (pz + Math.cos(ph * 0.4) * spanF);
			float by = (float) (py + Math.sin(ph * 0.8) * 8.0);
			int c = Colors.alpha(pick(i), (int) (aF * (0.55f + 0.45f * (float) Math.sin(ph))));
			BufferBuilder b = Fx.texQuads();
			Fx.billboard(b, mat, bx, by, bz, wF, c);
			Fx.draw();
		}
		Fx.end();
	}
}