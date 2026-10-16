package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/** Highlights ore and other blocks around you. */
public class BlockESP extends Module {
	private final ModeSetting block = add(new ModeSetting("Block", "Diamond",
			"Diamond", "Ancient debris", "Emerald", "Gold", "Iron", "Redstone", "Lapis", "Spawner"));
	private final NumberSetting radius = add(new NumberSetting("Radius", 16, 6, 40, 2));
	private final NumberSetting yRange = add(new NumberSetting("Vertical", 10, 4, 40, 2));
	private final BoolSetting fill = add(new BoolSetting("Fill", false));

	private final List<BlockPos> found = new ArrayList<>();
	private int timer;

	public BlockESP() {
		super("BlockESP", Category.VISUALS);
		describe("Highlights ore and other blocks around you.");
	}

	private Block target() {
		return switch (block.get()) {
			case "Ancient debris" -> Blocks.ANCIENT_DEBRIS;
			case "Emerald" -> Blocks.EMERALD_ORE;
			case "Gold" -> Blocks.GOLD_ORE;
			case "Iron" -> Blocks.IRON_ORE;
			case "Redstone" -> Blocks.REDSTONE_ORE;
			case "Lapis" -> Blocks.LAPIS_ORE;
			case "Spawner" -> Blocks.SPAWNER;
			default -> Blocks.DIAMOND_ORE;
		};
	}

	@Override
	public void onTick() {
		if (mc.world == null || mc.player == null) return;
		if (timer > 0) { timer--; return; }
		timer = 12;
		found.clear();
		Block want = target();
		BlockPos base = mc.player.getBlockPos();
		int r = radius.getI(), vy = yRange.getI();
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int dx = -r; dx <= r; dx++)
			for (int dz = -r; dz <= r; dz++)
				for (int dy = -vy; dy <= vy; dy++) {
					p.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
					Block b = mc.world.getBlockState(p).getBlock();
					if (b == want || (want == Blocks.DIAMOND_ORE && b == Blocks.DEEPSLATE_DIAMOND_ORE)
							|| (want == Blocks.GOLD_ORE && b == Blocks.DEEPSLATE_GOLD_ORE)
							|| (want == Blocks.IRON_ORE && b == Blocks.DEEPSLATE_IRON_ORE)
							|| (want == Blocks.EMERALD_ORE && b == Blocks.DEEPSLATE_EMERALD_ORE)
							|| (want == Blocks.LAPIS_ORE && b == Blocks.DEEPSLATE_LAPIS_ORE)
							|| (want == Blocks.REDSTONE_ORE && b == Blocks.DEEPSLATE_REDSTONE_ORE)) {
						found.add(p.toImmutable());
					}
				}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		int c = Colors.wave(0f);
		for (BlockPos p : found) {
			Box b = new Box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
			RenderUtil.box(m, b, fill.get() ? Colors.alpha(c, 45) : 0, c);
		}
	}
}