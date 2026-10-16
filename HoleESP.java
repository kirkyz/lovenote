package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/** Marks safe one block holes near you. */
public class HoleESP extends Module {
	private final NumberSetting radius = add(new NumberSetting("Radius", 6, 2, 12, 1));
	private final BoolSetting fill = add(new BoolSetting("Fill", true));

	private final List<BlockPos> holes = new ArrayList<>();
	private int timer;

	public HoleESP() {
		super("HoleESP", Category.VISUALS);
		describe("Marks safe holes around you.");
	}

	@Override
	public void onTick() {
		if (mc.world == null || mc.player == null) return;
		if (timer > 0) { timer--; return; }
		timer = 10;
		holes.clear();
		BlockPos base = mc.player.getBlockPos();
		int r = radius.getI();
		for (int dx = -r; dx <= r; dx++)
			for (int dz = -r; dz <= r; dz++)
				for (int dy = -1; dy <= 1; dy++) {
					BlockPos p = base.add(dx, dy, dz);
					if (isHole(p)) holes.add(p.toImmutable());
				}
	}

	private boolean isHole(BlockPos p) {
		if (!solid(p.down())) return false;
		if (solid(p) || solid(p.up())) return false;
		return solid(p.north()) && solid(p.south()) && solid(p.east()) && solid(p.west());
	}

	private boolean solid(BlockPos p) {
		return mc.world.getBlockState(p).isSolidBlock(mc.world, p);
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		int i = 0;
		for (BlockPos p : holes) {
			int c = Colors.wave(i++ * 0.05f);
			Box b = new Box(p.getX() + 0.05, p.getY(), p.getZ() + 0.05, p.getX() + 0.95, p.getY() + 0.06, p.getZ() + 0.95);
			RenderUtil.box(m, b, fill.get() ? Colors.alpha(c, 90) : 0, c);
		}
	}
}