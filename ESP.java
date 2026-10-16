package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ESP extends Module {
	private final BoolSetting fill = add(new BoolSetting("Fill", true));
	private final BoolSetting outline = add(new BoolSetting("Outline", true));
	private final ModeSetting color = add(new ModeSetting("Color", "Accent", "Accent", "Health", "Rainbow"));
	private final NumberSetting opacity = add(new NumberSetting("Fill opacity", 50, 10, 200, 5));

	public ESP() {
		super("ESP", Category.VISUALS);
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.world == null) return;
		float td = ctx.tickDelta();
		int i = 0;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
			Vec3d l = p.getLerpedPos(td);
			Box b = p.getBoundingBox().offset(l.subtract(p.getEntityPos())).expand(0.05);
			float hp = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
			int c = Colors.byMode(color.get(), hp, i++ * 0.07f);
			RenderUtil.box(m, b, fill.get() ? Colors.alpha(c, opacity.getI()) : 0, outline.get() ? c : 0);
		}
	}
}
