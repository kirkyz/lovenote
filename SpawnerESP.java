package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SpawnerESP extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 12, 4, 32, 2));

	public SpawnerESP() {
		super("SpawnerESP", Category.DONUT);
		describe("Boxes spawners through walls.");
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.world == null || mc.player == null) return;
		int r = range.getI();
		BlockPos o = mc.player.getBlockPos();
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					BlockPos p = o.add(dx, dy, dz);
					if (!(mc.world.getBlockEntity(p) instanceof MobSpawnerBlockEntity)) continue;
					Box b = new Box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
					RenderUtil.box(m, b.expand(0.002), Colors.alpha(Colors.C1, 46), Colors.C1);
				}
			}
		}
	}
}