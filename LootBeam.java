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

public class LootBeam extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 24, 6, 64, 2));
	private final NumberSetting height = add(new NumberSetting("Height", 4, 1, 16, 0.5));

	public LootBeam() {
		super("LootBeam", Category.DONUT);
		describe("Vertical beams on dropped items.");
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.world == null || mc.player == null) return;
		double r = range.getF();
		for (Entity e : mc.world.getEntities()) {
			if (!(e instanceof ItemEntity)) continue;
			if (e.squaredDistanceTo(mc.player) > r * r) continue;
			Box b = e.getBoundingBox();
			Box beam = new Box(b.minX, b.minY, b.minZ, b.maxX, b.minY + height.get(), b.maxZ).expand(0.02);
			RenderUtil.box(m, beam, Colors.alpha(Colors.C2, 30), Colors.alpha(Colors.C2, 200));
		}
	}
}