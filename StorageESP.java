package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/** Outlines chests, barrels and shulkers through walls. */
public class StorageESP extends Module {
	private final NumberSetting radius = add(new NumberSetting("Chunk radius", 3, 1, 8, 1));
	private final BoolSetting fill = add(new BoolSetting("Fill", true));

	private final List<BlockPos> found = new ArrayList<>();
	private int timer;

	public StorageESP() {
		super("StorageESP", Category.VISUALS);
		describe("Highlights chests, barrels and shulkers.");
	}

	@Override
	public void onTick() {
		if (mc.world == null || mc.player == null) return;
		if (timer > 0) { timer--; return; }
		timer = 20;
		found.clear();
		int r = radius.getI();
		int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;
		for (int cx = -r; cx <= r; cx++) {
			for (int cz = -r; cz <= r; cz++) {
				WorldChunk chunk = mc.world.getChunk(pcx + cx, pcz + cz);
				if (chunk == null) continue;
				for (BlockPos p : chunk.getBlockEntities().keySet()) {
					var b = mc.world.getBlockState(p).getBlock();
					if (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.BARREL
							|| b == Blocks.ENDER_CHEST || b == Blocks.HOPPER || b == Blocks.DROPPER
							|| b == Blocks.DISPENSER || b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE
							|| b == Blocks.SMOKER || b == Blocks.SHULKER_BOX) {
						found.add(p.toImmutable());
					}
				}
			}
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		for (BlockPos p : found) {
			int c = Colors.wave(0f);
			Box b = new Box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
			RenderUtil.box(m, b, fill.get() ? Colors.alpha(c, 40) : 0, c);
		}
	}
}