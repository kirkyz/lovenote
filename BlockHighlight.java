package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;

/** Outlines the block you are looking at. */
public class BlockHighlight extends Module {
	private final NumberSetting opacity = add(new NumberSetting("Fill opacity", 50, 0, 200, 5));
	private final BoolSetting outline = add(new BoolSetting("Outline", true));

	public BlockHighlight() {
		super("BlockHighlight", Category.VISUALS);
		describe("Outlines the block you are looking at.");
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null || mc.world == null) return;
		if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;
		var pos = bhr.getBlockPos();
		if (mc.world.getBlockState(pos).isAir()) return;
		int c = Colors.wave(0f);
		RenderUtil.box(m, new Box(pos).expand(0.002), Colors.alpha(c, opacity.getI()), outline.get() ? c : 0);
	}
}
