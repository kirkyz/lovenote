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
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Soft additive glow halo around players. */
public class Bloom extends Module {
	private final NumberSetting size = add(new NumberSetting("Size", 2.2, 0.5, 6.0, 0.1));
	private final NumberSetting range = add(new NumberSetting("Range", 40, 8, 128, 4));
	private final ModeSetting color = add(new ModeSetting("Color", "Accent", "Accent", "Health", "Rainbow"));
	private final BoolSetting self = add(new BoolSetting("Include self", false));

	public Bloom() {
		super("Bloom", Category.VISUALS);
		describe("Soft additive glow around players.");
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
		Fx.texture(Tex.GLOW);
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
			float f = (float) (size.get() * p.getHeight());
			BufferBuilder b = Fx.texQuads();
			Fx.billboard(b, mat, (float) pos.x, (float) (pos.y + p.getHeight() * 0.5), (float) pos.z, f,
					Colors.alpha(c, 90));
			Fx.draw();
		}
		Fx.end();
	}
}