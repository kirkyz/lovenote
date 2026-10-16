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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Tints the sky and fog, plus a screen vignette. Driven by WorldRendererMixin. */
public class WorldTint extends Module {

	private final ModeSetting mode = add(new ModeSetting("Mode", "Violet", "Violet", "Teal", "Sunset", "Custom"));
	private final NumberSetting strength = add(new NumberSetting("Strength", 55, 0, 100, 5));
	private final NumberSetting hue = add(new NumberSetting("Custom hue", 275, 0, 360, 1));
	private final NumberSetting sat = add(new NumberSetting("Custom sat", 85, 0, 100, 5));
	private final BoolSetting vignette = add(new BoolSetting("Vignette", true));
	private final NumberSetting vigStrength = add(new NumberSetting("Vignette strength", 150, 0, 255, 5));

	public WorldTint() {
		super("WorldTint", Category.VISUALS);
		describe("Tints the sky, fog and screen edges.");
	}

	/** Called from WorldRendererMixin#getSkyColor - fog follows this automatically. */
	public Vec3d tint(Vec3d in) {
		int c = switch (mode.get()) {
			case "Teal" -> 0xFF1CE8C0;
			case "Sunset" -> 0xFFFF7A45;
			case "Custom" -> Colors.hsv(hue.getI() / 360f, sat.getI() / 100f, 1f);
			default -> 0xFF8A4DFF;
		};
		float t = strength.getI() / 100f;
		double cr = ((c >> 16) & 255) / 255.0;
		double cg = ((c >> 8) & 255) / 255.0;
		double cb = (c & 255) / 255.0;
		return new Vec3d(
			MathHelper.clamp(in.x * (1.0 - t + t * cr * 1.8), 0, 1),
			MathHelper.clamp(in.y * (1.0 - t + t * cg * 1.8), 0, 1),
			MathHelper.clamp(in.z * (1.0 - t + t * cb * 1.8), 0, 1));
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (!vignette.get() || mc.world == null) return;
		Tex.ensure();
		int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
		int a = vigStrength.getI();
		Matrix4f mat = RenderSystem.getModelViewStack();
		Fx.texture(Tex.VIGNETTE);
		BufferBuilder b = Fx.texQuads();
		int col = Colors.alpha(0x000000, a);
		Fx.vt(b, mat, 0, 0, 0, 0f, 0f, col);
		Fx.vt(b, mat, 0, h, 0, 0f, 1f, col);
		Fx.vt(b, mat, w, h, 0, 1f, 1f, col);
		Fx.vt(b, mat, w, 0, 0, 1f, 0f, col);
		Fx.draw();
	}
}
