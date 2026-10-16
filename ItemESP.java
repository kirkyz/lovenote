package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ItemESP extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 48, 8, 128, 4));

	public ItemESP() {
		super("ItemESP", Category.VISUALS);
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.world == null || mc.player == null) return;
		float td = ctx.tickDelta();
		for (Entity e : mc.world.getEntities()) {
			if (!(e instanceof ItemEntity) || mc.player.distanceTo(e) > range.get()) continue;
			Vec3d l = e.getLerpedPos(td);
			Box b = e.getBoundingBox().offset(l.subtract(e.getEntityPos())).expand(0.05);
			int c = Colors.wave(0f);
			RenderUtil.box(m, b, Colors.alpha(c, 45), c);
		}
	}
}
