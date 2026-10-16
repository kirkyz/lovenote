package com.lovenote.module.impl;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.Fx;
import com.lovenote.util.Tex;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/** Custom sky: gradient dome, sun/moon, star field, aurora bands and cloud tint. */
public class SkyShader extends Module {

	private final ModeSetting preset = add(new ModeSetting("Preset", "Lovenote",
			"Off", "Lovenote", "Sunset", "Dawn", "Night", "Aurora", "Cyber", "Void", "Custom"));
	private final BoolSetting overworldOnly = add(new BoolSetting("Overworld only", true));
	private final NumberSetting starBrightness = add(new NumberSetting("Stars", 0.7, 0.0, 1.0, 0.05));
	private final NumberSetting auroraStrength = add(new NumberSetting("Aurora", 0.0, 0.0, 1.0, 0.05));
	private final NumberSetting auroraSpeed = add(new NumberSetting("Aurora speed", 0.5, 0.0, 3.0, 0.05));
	private final BoolSetting celestial = add(new BoolSetting("Sun & moon", true));
	private final NumberSetting sunSize = add(new NumberSetting("Sun size", 1.0, 0.3, 3.0, 0.1));
	private final BoolSetting cloudTintOn = add(new BoolSetting("Tint clouds", true));
	private final BoolSetting fogTintOn = add(new BoolSetting("Tint fog", true));

	private static final float R = 260f;
	private static final float FLOOR = -70f;
	private static final Identifier SUN = new Identifier("textures/environment/sun.png");
	private static final Identifier MOON = new Identifier("textures/environment/moon_phases.png");

	private float[] stars;
	private int starCount = -1;

	public SkyShader() {
		super("SkyShader", Category.RENDER);
		describe("Custom sky, sun, stars and cloud tint");
	}

	// ---------------------------------------------------------------- presets

	private int top() {
		return switch (preset.get()) {
			case "Lovenote" -> Colors.darken(Colors.C3);
			case "Sunset" -> 0xFF2B1B4D;
			case "Dawn" -> 0xFF2A4A8A;
			case "Night" -> 0xFF05060F;
			case "Aurora" -> 0xFF071026;
			case "Cyber" -> 0xFF12002B;
			case "Void" -> 0xFF000000;
			case "Custom" -> custom(true);
			default -> 0xFF4A90D9;
		};
	}

	private int horizon() {
		return switch (preset.get()) {
			case "Lovenote" -> Colors.C1;
			case "Sunset" -> 0xFFFF7A45;
			case "Dawn" -> 0xFFFFC48A;
			case "Night" -> 0xFF141A33;
			case "Aurora" -> 0xFF0E2A2A;
			case "Cyber" -> 0xFFFF2E88;
			case "Void" -> 0xFF0A0A12;
			case "Custom" -> custom(false);
			default -> 0xFFB7D9F2;
		};
	}

	private static int custom(boolean primary) {
		InterfaceModule im = LovenoteClient.modules == null ? null : LovenoteClient.modules.get(InterfaceModule.class);
		if (im == null) return primary ? 0xFFB28DFF : 0xFFFF9ACB;
		return 0xFF000000 | (primary ? im.customSecondaryInt() : im.customPrimaryInt());
	}

	private boolean starsOn() {
		return starBrightness.getF() > 0.01f
				&& (preset.is("Night") || preset.is("Void") || preset.is("Aurora") || preset.is("Custom"));
	}

	private boolean auroraOn() {
		return auroraStrength.getF() > 0.01f
				&& (preset.is("Aurora") || preset.is("Custom") || preset.is("Cyber"));
	}

	/** Tint applied to vanilla clouds, or -1 for untouched. */
	public int cloudTint() {
		if (!enabled || !cloudTintOn.get() || preset.is("Off")) return -1;
		return horizon();
	}

	// ---------------------------------------------------------------- render

	public boolean render(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta,
			Camera camera, Runnable fogCallback) {
		if (!enabled || preset.is("Off")) return false;
		if (overworldOnly.get() && mc.world != null
				&& mc.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL) {
			return false;
		}

		matrices.push();
		matrices.multiply(new Quaternionf().rotationX((float) Math.toRadians(camera.getPitch())));
		matrices.multiply(new Quaternionf().rotationY((float) Math.toRadians(camera.getYaw() + 180f)));
		Matrix4f m = matrices.peek().getPositionMatrix();

		Fx.cameraBasis(camera);

		RenderSystem.depthMask(false);
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.disableBlend();

		dome(m);

		if (starsOn()) stars(m, tickDelta);
		if (auroraOn()) aurora(m, tickDelta);
		if (celestial.get()) sunMoon(m, tickDelta);

		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();

		if (fogCallback != null) fogCallback.run();
		if (fogTintOn.get()) {
			int h = horizon();
			RenderSystem.setShaderFogColor(((h >> 16) & 255) / 255f, ((h >> 8) & 255) / 255f, (h & 255) / 255f);
		}
		matrices.pop();
		return true;
	}

	private void dome(Matrix4f m) {
		int t = Colors.alpha(top(), 255);
		int h = Colors.alpha(horizon(), 255);
		int tb = Colors.alpha(Colors.lerp(top(), 0xFF000000, 0.25f), 255);
		int hb = Colors.alpha(Colors.lerp(horizon(), 0xFF000000, 0.35f), 255);

		BufferBuilder b = Tessellator.getInstance().getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

		face(b, m, -R, FLOOR, -R, R, FLOOR, -R, R, R, -R, -R, R, -R, hb, hb, t, t);
		face(b, m, R, FLOOR, R, -R, FLOOR, R, -R, R, R, R, R, R, hb, hb, t, t);
		face(b, m, -R, FLOOR, R, -R, FLOOR, -R, -R, R, -R, -R, R, R, hb, hb, t, t);
		face(b, m, R, FLOOR, -R, R, FLOOR, R, R, R, R, R, R, -R, hb, hb, t, t);
		face(b, m, -R, R, -R, R, R, -R, R, R, R, -R, R, R, t, t, tb, tb);

		Tessellator.getInstance().draw();
	}

	private static void face(BufferBuilder b, Matrix4f m,
			float x1, float y1, float z1, float x2, float y2, float z2,
			float x3, float y3, float z3, float x4, float y4, float z4,
			int c1, int c2, int c3, int c4) {
		Fx.vc(b, m, x1, y1, z1, c1);
		Fx.vc(b, m, x2, y2, z2, c2);
		Fx.vc(b, m, x3, y3, z3, c3);
		Fx.vc(b, m, x4, y4, z4, c4);
	}

	private void stars(Matrix4f m, float td) {
		int n = 520;
		if (starCount != n) {
			starCount = n;
			stars = new float[n * 3];
			Random r = new Random(0x5EED);
			for (int i = 0; i < n; i++) {
				float y = r.nextFloat();
				float a = r.nextFloat() * 6.28318f;
				float rad = (float) Math.sqrt(Math.max(0f, 1f - y * y));
				stars[i * 3] = MathHelper.cos(a) * rad * R;
				stars[i * 3 + 1] = y * R;
				stars[i * 3 + 2] = MathHelper.sin(a) * rad * R;
			}
		}

		float spin = (mc.world == null ? 0f : mc.world.getSkyAngle(td)) * 6.28318f;
		float cs = MathHelper.cos(spin), sn = MathHelper.sin(spin);
		float bright = starBrightness.getF();

		Tex.ensure();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderSystem.setShaderTexture(0, Tex.STAR);

		BufferBuilder b = Fx.texQuads();
		for (int i = 0; i < starCount; i++) {
			float x = stars[i * 3], y = stars[i * 3 + 1], z = stars[i * 3 + 2];
			float rx = x * cs - z * sn;
			float rz = x * sn + z * cs;
			float tw = 0.6f + 0.4f * MathHelper.sin((System.currentTimeMillis() % 6000L) / 1000f * 2f + i);
			int a = (int) (255 * bright * tw);
			if (a < 4) continue;
			Fx.billboard(b, m, rx, y, rz, 2.6f, Colors.alpha(0xFFFFFFFF, a));
		}
		Tessellator.getInstance().draw();
		RenderSystem.disableBlend();
	}

	private void aurora(Matrix4f m, float td) {
		float str = auroraStrength.getF();
		float time = (System.currentTimeMillis() % 100000L) / 1000f * auroraSpeed.getF();

		Tex.ensure();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderSystem.setShaderTexture(0, Tex.SOFT);

		BufferBuilder b = Fx.texQuads();
		for (int band = 0; band < 3; band++) {
			float phase = time + band * 2.1f;
			int col = Colors.rainbow(band * 0.22f + time * 0.02f);
			int a = (int) (150 * str);
			if (a < 4) continue;

			float zOff = MathHelper.sin(phase) * R * 0.55f;
			float xOff = MathHelper.cos(phase * 0.7f) * R * 0.55f;
			float y0 = 40f + band * 18f, y1 = y0 + 120f;
			float w = R * 1.15f;

			Fx.vt(b, m, -w + xOff, y0, zOff, 0f, 1f, Colors.alpha(col, 0));
			Fx.vt(b, m, w + xOff, y0, zOff, 1f, 1f, Colors.alpha(col, 0));
			Fx.vt(b, m, w + xOff, y1, zOff, 1f, 0f, Colors.alpha(col, a));
			Fx.vt(b, m, -w + xOff, y1, zOff, 0f, 0f, Colors.alpha(col, a));
		}
		Tessellator.getInstance().draw();
		RenderSystem.disableBlend();
	}

	private void sunMoon(Matrix4f m, float td) {
		float ang = (mc.world == null ? 0f : mc.world.getSkyAngle(td)) * 6.28318f;
		float sx = MathHelper.cos(ang) * R, sy = MathHelper.sin(ang) * R;
		boolean day = sy > -20f;
		float size = 34f * sunSize.getF();

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.setShaderTexture(0, day ? SUN : MOON);

		BufferBuilder b = Tessellator.getInstance().getBuffer();
		b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		int c = day ? 0xFFFFFFFF : 0xFFD8E4FF;
		float h = size * 0.5f;
		float rx = Fx.RIGHT.x * h, ry = Fx.RIGHT.y * h, rz = Fx.RIGHT.z * h;
		float ux = Fx.UP.x * h, uy = Fx.UP.y * h, uz = Fx.UP.z * h;
		Fx.vt(b, m, sx - rx - ux, sy - ry - uy, -rz - uz, 0f, 1f, c);
		Fx.vt(b, m, sx + rx - ux, sy + ry - uy, rz - uz, 1f, 1f, c);
		Fx.vt(b, m, sx + rx + ux, sy + ry + uy, rz + uz, 1f, 0f, c);
		Fx.vt(b, m, sx - rx + ux, sy - ry + uy, -rz + uz, 0f, 0f, c);
		Tessellator.getInstance().draw();
		RenderSystem.disableBlend();
	}

	/** kept so anything else calling these still works. */
	public Vec3d sky(Vec3d original) { return original == null ? Vec3d.ZERO : original; }
	public Vec3d clouds(Vec3d original) { return original == null ? Vec3d.ZERO : original; }
}
