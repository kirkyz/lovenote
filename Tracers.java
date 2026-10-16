package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {
	private final ModeSetting color = add(new ModeSetting("Color", "Accent", "Accent", "Health", "Rainbow"));

	public Tracers() {
		super("Tracers", Category.VISUALS);
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.world == null) return;
		float td = ctx.tickDelta();
		Vec3d cam = ctx.camera().getCameraPos();
		Vec3d start = cam.add(Vec3d.fromPolar(ctx.camera().getPitch(), ctx.camera().getYaw()));
		int i = 0;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
			Vec3d end = p.getLerpedPos(td).add(0, p.getHeight() / 2.0, 0);
			float hp = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
			RenderUtil.line(m, start, end, Colors.byMode(color.get(), hp, i++ * 0.07f));
		}
	}
}
