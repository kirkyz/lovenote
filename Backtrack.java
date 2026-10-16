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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Keeps a short position history and draws where targets were. */
public class Backtrack extends Module {
	private static final class Snap {
		final Vec3d pos;
		final long time;
		Snap(Vec3d pos, long time) { this.pos = pos; this.time = time; }
	}

	private final NumberSetting delay = add(new NumberSetting("Delay (ms)", 150, 50, 500, 10));
	private final NumberSetting range = add(new NumberSetting("Range", 8, 3, 24, 1));
	private final BoolSetting fill = add(new BoolSetting("Fill", false));
	private final ModeSetting color = add(new ModeSetting("Color", "Accent", "Accent", "Rainbow"));

	private final Map<UUID, Deque<Snap>> history = new HashMap<>();

	public Backtrack() {
		super("Backtrack", Category.COMBAT);
		describe("Shows targets at their delayed position.");
	}

	@Override
	public void onTick() {
		if (mc.world == null || mc.player == null) return;
		long now = System.currentTimeMillis();
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player) continue;
			Deque<Snap> q = history.computeIfAbsent(p.getUuid(), k -> new ArrayDeque<>());
			q.addLast(new Snap(p.getEntityPos(), now));
			while (q.size() > 1 && now - q.peekFirst().time > 800) q.removeFirst();
		}
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.world == null || mc.player == null) return;
		long now = System.currentTimeMillis();
		float td = ctx.tickDelta();
		int i = 0;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || !p.isAlive()) continue;
			if (p.getLerpedPos(td).distanceTo(mc.player.getEntityPos()) > range.get()) continue;
			Deque<Snap> q = history.get(p.getUuid());
			if (q == null) continue;
			Vec3d at = null;
			for (Iterator<Snap> it = q.descendingIterator(); it.hasNext(); ) {
				Snap s = it.next();
				if (now - s.time >= delay.get()) { at = s.pos; break; }
			}
			if (at == null) continue;
			Box b = new Box(at.x - 0.3, at.y, at.z - 0.3, at.x + 0.3, at.y + p.getHeight(), at.z + 0.3);
			int c = Colors.byMode(color.get(), 1f, i++ * 0.07f);
			RenderUtil.box(m, b, fill.get() ? Colors.alpha(c, 45) : 0, c);
		}
	}
}