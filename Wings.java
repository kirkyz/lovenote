package com.lovenote.module.impl;
import com.lovenote.util.Fx;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.mojang.blaze3d.systems.RenderSystem;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Cosmetic wings, drawn behind every player. */
public class Wings extends Module {
private final BoolSetting self = add(new BoolSetting("Self", true));
private final BoolSetting others = add(new BoolSetting("Other players", true));
private final NumberSetting range = add(new NumberSetting("Range", 32, 4, 64, 2));
private final NumberSetting size = add(new NumberSetting("Size", 1.0, 0.4, 2.5, 0.1));
private final NumberSetting flapSpeed = add(new NumberSetting("Flap speed", 1.0, 0, 4, 0.1));
private final NumberSetting opacity = add(new NumberSetting("Opacity", 200, 20, 255, 5));
private final ModeSetting mode = add(new ModeSetting("Mode", "Theme", "Theme", "Rainbow", "Static"));
private final BoolSetting glow = add(new BoolSetting("Glow pass", true));

private static final int FEATHERS = 8;

public Wings() {
super("Wings", Category.VISUALS);
}

@Override
public void onRender3D(WorldRenderContext ctx, MatrixStack m) {
if (mc.world == null) return;
float td = ctx.tickDelta();
float time = (System.currentTimeMillis() % 600000L) / 1000f;

for (PlayerEntity p : mc.world.getPlayers()) {
if (p == mc.player) {
if (!self.get()) continue;
} else {
if (!others.get()) continue;
if (mc.player != null && mc.player.distanceTo(p) > range.get()) continue;
}
if (!p.isAlive()) continue;
draw(m, p, td, time);
}
}

private void draw(MatrixStack m, PlayerEntity p, float td, float time) {
Vec3d pos = p.getLerpedPos(td);
float yaw = MathHelper.lerp(td, p.lastYaw, p.getYaw());
double ry = Math.toRadians(yaw);

Vec3d right = new Vec3d(-Math.cos(ry), 0, -Math.sin(ry));
Vec3d up = new Vec3d(0, 1, 0);
Vec3d fwd = new Vec3d(-Math.sin(ry), 0, Math.cos(ry));

double flap = Math.sin(time * 2.4 * flapSpeed.get()) * 0.30 * flapSpeed.get();
Vec3d origin = pos.add(0, 1.15, 0).add(fwd.multiply(-0.12));
float sz = (float) size.get();

BufferBuilder b = Fx.texQuads();

if (glow.get()) {
wing(b, m, origin, right, up, fwd, flap, sz * 1.18f, 60);
Fx.draw();
}

wing(b, m, origin, right, up, fwd, flap, sz, opacity.getI());
Fx.draw();

}

private void wing(BufferBuilder b, MatrixStack m, Vec3d o, Vec3d right, Vec3d up, Vec3d fwd,
  double flap, float sz, int alpha) {
for (int s = -1; s <= 1; s += 2) {
for (int i = 0; i < FEATHERS; i++) {
float f = i / (float) (FEATHERS - 1);
double ang = -0.75 + f * 1.45 + flap;
double len = (0.55 + f * 0.45) * sz;
double hw = (0.055 - f * 0.02) * sz;

Vec3d dir = right.multiply(s * Math.cos(ang)).add(up.multiply(Math.sin(ang))).add(fwd.multiply(-0.10));
Vec3d perp = right.multiply(-s * Math.sin(ang)).add(up.multiply(Math.cos(ang)));

Vec3d root = o.add(dir.multiply(0.04 * sz));
Vec3d tip = o.add(dir.multiply(len));

int cRoot = color(f, alpha);
int cTip = color(f, Math.max(0, alpha - 90));

quad(b, m, root.add(perp.multiply(hw)), root.subtract(perp.multiply(hw)),
tip.subtract(perp.multiply(hw * 0.45)), tip.add(perp.multiply(hw * 0.45)),
cRoot, cRoot, cTip, cTip);
}
}
}

private int color(float f, int alpha) {
int base = switch (mode.get()) {
case "Rainbow" -> Colors.rainbow(f * 0.6f);
case "Static" -> Colors.C2;
default -> Colors.lerp(Colors.C1, Colors.C3, f);
};
return Colors.alpha(base, alpha);
}

private static void quad(BufferBuilder b, MatrixStack m, Vec3d a, Vec3d bb, Vec3d c, Vec3d d,
 int ca, int cb, int cc, int cd) {
Matrix4f mat = m.peek().getPositionMatrix();
vert(b, mat, a, ca);
vert(b, mat, bb, cb);
vert(b, mat, c, cc);
vert(b, mat, d, cd);
}

private static void vert(BufferBuilder b, Matrix4f mat, Vec3d v, int c) {
b.vertex(mat, (float) v.x, (float) v.y, (float) v.z)
.color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255)
;
}
}
