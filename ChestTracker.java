package com.lovenote.module.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Marks containers you have not looted yet. */
public class ChestTracker extends Module {
	private final NumberSetting range = add(new NumberSetting("Range", 48, 8, 128, 4));
	private final BoolSetting beams = add(new BoolSetting("Beams", true));

	private final Set<Long> opened = new HashSet<>();
	private final List<BlockPos> cache = new ArrayList<>();
	private int rescan;

	public ChestTracker() {
		super("ChestTracker", Category.DONUT);
		describe("Marks chests you have not looted yet.");
	}

	@Override
	public void onEnable() { rescan = 0; }

	private static boolean isContainer(BlockState s) {
		return s.getBlock() instanceof ChestBlock
				|| s.getBlock() instanceof BarrelBlock
				|| s.getBlock() instanceof ShulkerBoxBlock;
	}

	@Override
	public void onTick() {
		if (mc.world == null || mc.player == null) return;
		if (rescan > 0) { rescan--; return; }
		rescan = 5;
		cache.clear();
		int r = Math.min(range.getI(), 48);
		BlockPos o = mc.player.getBlockPos();
		for (BlockPos p : BlockPos.iterate(o.add(-r, -r, -r), o.add(r, r, r))) {
			if (!isContainer(mc.world.getBlockState(p))) continue;
			BlockPos im = p.toImmutable();
			cache.add(im);
			if (im.isWithinDistance(o, 2.5)) opened.add(im.asLong());
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null) return;
		for (BlockPos p : cache) {
			if (opened.contains(p.asLong())) continue;
			Box b = new Box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
			RenderUtil.box(m, b.expand(0.002), Colors.alpha(Colors.C3, 26), Colors.alpha(Colors.C3, 170));
			if (beams.get()) {
				Box beam = new Box(p.getX() + 0.35, p.getY() + 1, p.getZ() + 0.35,
						p.getX() + 0.65, p.getY() + 3.2, p.getZ() + 0.65);
				RenderUtil.box(m, beam, Colors.alpha(Colors.C1, 30), Colors.alpha(Colors.C1, 180));
			}
		}
	}

	@Override
	public void saveExtra(JsonObject o) {
		JsonArray a = new JsonArray();
		for (long l : opened) a.add(l);
		o.add("opened", a);
	}

	@Override
	public void loadExtra(JsonObject o) {
		opened.clear();
		if (!o.has("opened")) return;
		for (JsonElement e : o.getAsJsonArray("opened")) opened.add(e.getAsLong());
	}
}