package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Shows where your bow or throwable will land. */
public class Trajectories extends Module {
	private final NumberSetting steps = add(new NumberSetting("Steps", 120, 40, 300, 10));
	private final BoolSetting mark = add(new BoolSetting("Mark landing", true));

	public Trajectories() {
		super("Trajectories", Category.VISUALS);
		describe("Shows where your projectile lands.");
	}

	@Override
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
		if (mc.player == null || mc.world == null) return;
		var item = mc.player.getMainHandStack().getItem();
		double gravity, speed;
		if (item instanceof BowItem) { gravity = 0.05; speed = 3.0; }
		else if (item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.EGG) { gravity = 0.03; speed = 1.5; }
		else if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.EXPERIENCE_BOTTLE) { gravity = 0.05; speed = 0.5; }
		else if (item == Items.TRIDENT) { gravity = 0.05; speed = 2.5; }
		else return;

		Vec3d pos = mc.player.getEyePos().subtract(0, 0.1, 0);
		Vec3d vel = mc.player.getRotationVec(1f).multiply(speed);
		int c = Colors.wave(0f);

		for (int i = 0; i < steps.getI(); i++) {
			Vec3d next = pos.add(vel);
			BlockHitResult hr = mc.world.raycast(new RaycastContext(pos, next,
					RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
			if (hr.getType() != HitResult.Type.MISS) {
				if (mark.get()) {
					Vec3d hp = hr.getPos();
					RenderUtil.box(m, new Box(hp.x - 0.1, hp.y - 0.1, hp.z - 0.1, hp.x + 0.1, hp.y + 0.1, hp.z + 0.1),
							Colors.alpha(c, 90), c);
				}
				return;
			}
			RenderUtil.line(m, pos, next, c);
			pos = next;
			vel = new Vec3d(vel.x, vel.y - gravity, vel.z).multiply(0.99);
		}
	}
}