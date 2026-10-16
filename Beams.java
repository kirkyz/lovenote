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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Vertical light shafts above players. */
public class Beams extends Module {
	private final NumberSetting height = add(new NumberSetting("Height", 24, 4, 64, 2));
	private final NumberSetting width = add(new NumberSetting("Width", 0.5, 0.1, 2.0, 0.1));
	private final NumberSetting range = add(new NumberSetting("Range", 48, 8, 128, 4));
	private final ModeSetting color = add(new ModeSetting("Color", "Accent", "Accent", "Health", "Rainbow"));
	private final BoolSetting self = add(new BoolSetting("Include self", false));

	public Beams() {
		super("Beams", Category.VISUALS);
		describe("Vertical light shafts above players.");
	}

	private int pick(String mode, float health, float offset) {
		return switch (mode) {
			case "Health" -> Colors.health(health);
			case "Rainbow" -> Colors.rainbow(offset);
			default -> Colors.wave(offset);
		};
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.world == null || mc.player == null) return;
		Tex.ensure();
		Fx.cameraBasis(ctx.camera());
		Fx.texture(Tex.SOFT);
		Fx.additive();
		Matrix4f mat = m.peek().getPositionMatrix();
		float td = ctx.tickDelta();
		int i = 0;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player && !self.get()) continue;
			if (!p.isAlive()) continue;
			Vec3d pos = p.getLerpedPos(td);
			if (pos.distanceTo(mc.player.getEntityPos()) > range.get()) continue;
			float hp = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
			int c = pick(color.get(), hp, i++ * 0.08f);

			float hw = (float) (width.get() * 0.5);
			float x = (float) pos.x, y0 = (float) pos.y, z = (float) pos.z;
			float y1 = y0 + (float) height.get();
			float rx = Fx.RIGHT.x * hw, rz = Fx.RIGHT.z * hw;
			int top = Colors.alpha(c, 0);
			int bottom = Colors.alpha(c, 110);

			BufferBuilder b = Fx.texQuads();
			Fx.vt(b, mat, x - rx, y0, z - rz, 0f, 1f, bottom);
			Fx.vt(b, mat, x + rx, y0, z + rz, 1f, 1f, bottom);
			Fx.vt(b, mat, x + rx, y1, z + rz, 1f, 0f, top);
			Fx.vt(b, mat, x - rx, y1, z - rz, 0f, 0f, top);
			Fx.draw();
		}
		Fx.end();
	}
}