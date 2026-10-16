package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/** Bigger nametags with health and distance. */
public class Nametags extends Module {
	private final BoolSetting health = add(new BoolSetting("Health", true));
	private final BoolSetting distance = add(new BoolSetting("Distance", true));
	private final BoolSetting background = add(new BoolSetting("Background", true));

	public Nametags() {
		super("Nametags", Category.VISUALS);
		describe("Bigger nametags with health and distance.");
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		if (mc.world == null || mc.player == null) return;
		var cam = mc.gameRenderer.getCamera();
		Matrix4f view = new Matrix4f();
		view.rotateX((float) Math.toRadians(cam.getPitch()));
		view.rotateY((float) Math.toRadians(cam.getYaw() + 180f));
		view.translate((float) -cam.getCameraPos().x, (float) -cam.getCameraPos().y, (float) -cam.getCameraPos().z);
		Matrix4f proj = mc.gameRenderer.getBasicProjectionMatrix(mc.options.getFov().getValue());
		Matrix4f mvp = new Matrix4f(proj).mul(view);

		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive()) continue;
			var pos = p.getLerpedPos(td);
			Vector4f v = new Vector4f((float) pos.x, (float) (pos.y + p.getHeight() + 0.5), (float) pos.z, 1f);
			v.mul(mvp);
			if (v.w <= 0.01f) continue;
			float sx = (v.x / v.w * 0.5f + 0.5f) * sw;
			float sy = (1f - (v.y / v.w * 0.5f + 0.5f)) * sh;
			if (sx < -100 || sx > sw + 100 || sy < -20 || sy > sh + 20) continue;

			String label = p.getName().getString();
			if (health.get()) label += "  " + Math.round(p.getHealth() + p.getAbsorptionAmount()) + "hp";
			if (distance.get()) label += "  " + String.format("%.1fm", mc.player.distanceTo(p));
			int w = mc.textRenderer.getWidth(label);
			int x = (int) (sx - w / 2f), y = (int) sy;
			if (background.get()) RenderUtil.rrect(ctx, x - 4, y - 3, w + 8, 13, 0x90000000);
			ctx.drawTextWithShadow(mc.textRenderer, label, x, y, Colors.wave(0f));
		}
	}
}