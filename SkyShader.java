package com.lovenote.module.impl;
import net.minecraft.client.render.RenderLayer;
import com.lovenote.util.Gl;
import net.minecraft.world.dimension.DimensionType;

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
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/** Custom sky: smooth sphere dome, sun/moon, star field, aurora bands and cloud tint. */
public class SkyShader extends Module {

	private final ModeSetting preset = add(new ModeSetting("Preset", "Lovenote",
			"Off", "Lovenote", "Sunset", "Dawn", "Night", "Aurora", "Cyber", "Void",
			"Nebula", "Vaporwave", "Matrix", "BloodMoon", "Ice", "Candy", "Mono", "Custom"));
	private final BoolSetting overworldOnly = add(new BoolSetting("Overworld only", true));
	private final NumberSetting starBrightness = add(new NumberSetting("Stars", 0.7, 0.0, 1.0, 0.05));
	private final NumberSetting auroraStrength = add(new NumberSetting("Aurora", 0.0, 0.0, 1.0, 0.05));
	private final NumberSetting auroraSpeed = add(new NumberSetting("Aurora speed", 0.5, 0.0, 3.0, 0.05));
	private final BoolSetting celestial = add(new BoolSetting("Sun & moon", true));
	private final NumberSetting sunSize = add(new NumberSetting("Sun size", 1.0, 0.3, 3.0, 0.1));
	private final BoolSetting cloudTintOn = add(new BoolSetting("Tint clouds", true));
	private final BoolSetting fogTintOn = add(new BoolSetting("Tint fog", true));
	private final NumberSetting domeDetail = add(new NumberSetting("Dome detail", 48, 16, 96, 8));

	private static final float R = 260f;
	private static final Identifier SUN = Identifier.ofVanilla("textures/environment/sun.png");
	private static final Identifier MOON = Identifier.ofVanilla("textures/environment/moon_phases.png");

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
			case "Nebula" -> 0xFF1B0B3B;
			case "Vaporwave" -> 0xFF2A0A4A;
			case "Matrix" -> 0xFF001A0A;
			case "BloodMoon" -> 0xFF2A0505;
			case "Ice" -> 0xFF0B2A3A;
			case "Candy" -> 0xFF3A0B2E;
			case "Mono" -> 0xFF101014;
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
			case "Nebula" -> 0xFF6D28D9;
			case "Vaporwave" -> 0xFFFF2E88;
			case "Matrix" -> 0xFF00FF41;
			case "BloodMoon" -> 0xFFDC143C;
			case "Ice" -> 0xFF67E8F9;
			case "Candy" -> 0xFFFF9ACB;
			case "Mono" -> 0xFFDDDDDD;
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
		return starBrightness.getF() > 0.01f && switch (preset.get()) {
			case "Night", "Void", "Aurora", "Nebula", "Vaporwave", "Matrix", "BloodMoon", "Ice", "Candy", "Mono", "Custom" -> true;
			default -> false;
		};
	}

	private boolean auroraOn() {
		return auroraStrength.getF() > 0.01f && switch (preset.get()) {
			case "Aurora", "Cyber", "Nebula", "Vaporwave", "Matrix", "Custom" -> true;
			default -> false;
		};
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
				&& mc.world.getDimension().skybox() != DimensionType.Skybox.OVERWORLD) {
			return false;
		}

		matrices.push();
		matrices.multiply(new Quaternionf().rotationX((float) Math.toRadians(camera.getPitch())));
		matrices.multiply(new Quaternionf().rotationY((float) Math.toRadians(camera.getYaw() + 180f)));
		Matrix4f m = matrices.peek().getPositionMatrix();

		Fx.cameraBasis(camera);


		int seg = domeDetail.getI();
		int rings = Math.max(6, seg / 3);
		sphere(m, seg, rings);

		if (starsOn()) stars(m, tickDelta);
		if (auroraOn()) aurora(m, tickDelta);
		if (celestial.get()) sunMoon(m, tickDelta);


		if (fogCallback != null) fogCallback.run();
		if (fogTintOn.get()) {
			int h = horizon();
		}
		matrices.pop();
		return true;
	}

	/**
	 * Full sphere instead of a box. The old version was four flat walls and a flat ceiling that
	 * stopped short under the camera, which is what gave the "standing inside a square" look: hard
	 * corner seams, a visible ceiling rim and a bottom edge you could see when flying.
	 */
	private void sphere(Matrix4f m, int seg, int rings) {
		int zenith = top();
		int horizon = horizon();
		int nadir = Colors.lerp(horizon(), 0xFF000000, 0.45f);

		BufferBuilder b = Fx.texQuads();

		for (int r = 0; r < rings; r++) {
			float phi0 = (float) (r * Math.PI / rings);
			float phi1 = (float) ((r + 1) * Math.PI / rings);
			float y0 = (float) Math.cos(phi0) * R;
			float y1 = (float) Math.cos(phi1) * R;
			float rad0 = (float) Math.sin(phi0) * R;
			float rad1 = (float) Math.sin(phi1) * R;
			int c0 = band(y0, zenith, horizon, nadir);
			int c1 = band(y1, zenith, horizon, nadir);

			for (int s = 0; s < seg; s++) {
				float a0 = (float) (s * Math.PI * 2.0 / seg);
				float a1 = (float) ((s + 1) * Math.PI * 2.0 / seg);
				float x00 = MathHelper.cos(a0) * rad0, z00 = MathHelper.sin(a0) * rad0;
				float x01 = MathHelper.cos(a1) * rad0, z01 = MathHelper.sin(a1) * rad0;
				float x10 = MathHelper.cos(a0) * rad1, z10 = MathHelper.sin(a0) * rad1;
				float x11 = MathHelper.cos(a1) * rad1, z11 = MathHelper.sin(a1) * rad1;
				Fx.vc(b, m, x00, y0, z00, c0);
				Fx.vc(b, m, x01, y0, z01, c0);
				Fx.vc(b, m, x11, y1, z11, c1);
				Fx.vc(b, m, x10, y1, z10, c1);
			}
		}

		Fx.draw();
	}

	private static int band(float y, int zenith, int horizon, int nadir) {
		float t = MathHelper.clamp(y / R, -1f, 1f);
		return t >= 0f ? Colors.lerp(horizon, zenith, t) : Colors.lerp(horizon, nadir, -t);
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

		float spin = (mc.world == null ? 0f : Gl.skyAngle(mc.world, td)) * 6.28318f;
		float cs = MathHelper.cos(spin), sn = MathHelper.sin(spin);
		float bright = starBrightness.getF();

		Tex.ensure();
		Fx.texture(Tex.STAR);

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
		Fx.draw();
	}

	private void aurora(Matrix4f m, float td) {
		float str = auroraStrength.getF();
		float time = (System.currentTimeMillis() % 100000L) / 1000f * auroraSpeed.getF();

		Tex.ensure();
		Fx.texture(Tex.SOFT);

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
		Fx.draw();
	}

	private void sunMoon(Matrix4f m, float td) {
		float ang = (mc.world == null ? 0f : Gl.skyAngle(mc.world, td)) * 6.28318f;
		float sx = MathHelper.cos(ang) * R, sy = MathHelper.sin(ang) * R;
		boolean day = sy > -20f;
		float size = 34f * sunSize.getF();

		Fx.texture(day ? SUN : MOON);

		BufferBuilder b = Fx.texQuads();
		int c = day ? 0xFFFFFFFF : 0xFFD8E4FF;
		float h = size * 0.5f;
		float rx = Fx.RIGHT.x * h, ry = Fx.RIGHT.y * h, rz = Fx.RIGHT.z * h;
		float ux = Fx.UP.x * h, uy = Fx.UP.y * h, uz = Fx.UP.z * h;
		Fx.vt(b, m, sx - rx - ux, sy - ry - uy, -rz - uz, 0f, 1f, c);
		Fx.vt(b, m, sx + rx - ux, sy + ry - uy, rz - uz, 1f, 1f, c);
		Fx.vt(b, m, sx + rx + ux, sy + ry + uy, rz + uz, 1f, 0f, c);
		Fx.vt(b, m, sx - rx + ux, sy - ry + uy, -rz + uz, 0f, 0f, c);
		Fx.draw();
	}

	/** kept so anything else calling these still works. */
	public Vec3d sky(Vec3d original) { return original == null ? Vec3d.ZERO : original; }
	public Vec3d clouds(Vec3d original) { return original == null ? Vec3d.ZERO : original; }
}
