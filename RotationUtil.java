package com.lovenote.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {
	private RotationUtil() {}

	/** {yaw, pitch} needed for 'from' to look at the upper body of 'to'. */
	public static float[] to(Entity from, Entity to) {
		Vec3d eye = from.getEyePos();
		Vec3d p = to.getEntityPos().add(0, to.getHeight() * 0.6, 0);
		double dx = p.x - eye.x, dy = p.y - eye.y, dz = p.z - eye.z;
		double flat = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));
		return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f)};
	}
}
