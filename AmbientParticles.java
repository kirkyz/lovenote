package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.ParticleUtil;
import net.minecraft.particle.ParticleEffect;

import java.util.Random;

/** Fills the air around you with shaped particle effects. */
public class AmbientParticles extends Module {
private final ModeSetting particle = add(new ModeSetting("Particle", "Snow", ParticleUtil.names()));
private final ModeSetting shape = add(new ModeSetting("Shape", "Scatter", "Scatter", "Ring", "Spiral",
"Vortex", "Helix", "Burst", "Wave", "Rain", "Heart", "DNA", "Cage"));
private final NumberSetting amount = add(new NumberSetting("Amount", 4, 1, 30, 1));
private final NumberSetting interval = add(new NumberSetting("Interval (ticks)", 3, 1, 20, 1));
private final NumberSetting radius = add(new NumberSetting("Radius", 2.5, 0.5, 12, 0.1));
private final NumberSetting height = add(new NumberSetting("Height", 3.0, 0.5, 16, 0.5));
private final NumberSetting area = add(new NumberSetting("Scatter area", 8, 2, 24, 1));
private final NumberSetting speed = add(new NumberSetting("Speed", 0.06, 0, 0.5, 0.01));
private final NumberSetting drift = add(new NumberSetting("Drift", 0.02, 0, 0.3, 0.01));
private final NumberSetting spin = add(new NumberSetting("Spin", 1.0, 0, 6, 0.1));

private int ticks;
private float phase;
private final Random rnd = new Random();

public AmbientParticles() {
super("AmbientParticles", Category.VISUALS);
}

@Override
public void onTick() {
if (mc.player == null || mc.world == null) return;
if (++ticks % interval.getI() != 0) return;

ParticleEffect e = ParticleUtil.get(particle.get());
String sh = shape.get();
int n = amount.getI();
double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
double rad = radius.get(), h = height.get(), d = drift.get(), sp = speed.get();
phase += 0.05f + (float) spin.get() * 0.01f;

for (int i = 0; i < n; i++) {
double f = n <= 1 ? 0 : i / (double) n;
double x, y, z, vx, vy, vz;
double a = phase * spin.get() + f * Math.PI * 2;

switch (sh) {
case "Ring" -> {
x = Math.cos(a) * rad; z = Math.sin(a) * rad;
y = rnd.nextDouble() * 0.3;
vx = Math.cos(a) * sp; vy = 0.02; vz = Math.sin(a) * sp;
}
case "Spiral" -> {
double r = rad * (0.25 + f);
x = Math.cos(a + f * 6) * r; z = Math.sin(a + f * 6) * r;
y = f * h;
vx = 0; vy = sp * 0.5; vz = 0;
}
case "Vortex" -> {
double r = rad * (1.2 - f * 0.8);
x = Math.cos(a - f * 8) * r; z = Math.sin(a - f * 8) * r;
y = f * h * 1.6;
vx = 0; vy = sp * 0.4; vz = 0;
}
case "Helix" -> {
double side = (i % 2 == 0) ? 0 : Math.PI;
x = Math.cos(a + side + f * 9) * rad;
z = Math.sin(a + side + f * 9) * rad;
y = f * h;
vx = 0; vy = sp * 0.6; vz = 0;
}
case "DNA" -> {
double side = (i % 2 == 0) ? 0 : Math.PI;
double r = rad * 0.6;
x = Math.cos(f * 12 + side + phase) * r;
z = Math.sin(f * 12 + side + phase) * r;
y = f * h * 1.4;
vx = 0; vy = 0.01; vz = 0;
}
case "Burst" -> {
double t = rnd.nextDouble() * Math.PI * 2;
double p = rnd.nextDouble() * Math.PI - Math.PI / 2;
x = Math.cos(t) * Math.cos(p) * 0.2;
y = Math.sin(p) * 0.2 + 0.6;
z = Math.sin(t) * Math.cos(p) * 0.2;
vx = Math.cos(t) * Math.cos(p) * sp * 3;
vy = Math.sin(p) * sp * 3 + 0.03;
vz = Math.sin(t) * Math.cos(p) * sp * 3;
}
case "Wave" -> {
double t = f * Math.PI * 2;
x = Math.sin(t) * rad; z = Math.cos(t * 0.5) * rad;
y = 0.2 + Math.sin(t * 2 + phase) * 0.6;
vx = 0; vy = 0.02; vz = 0;
}
case "Rain" -> {
x = (rnd.nextDouble() * 2 - 1) * rad;
z = (rnd.nextDouble() * 2 - 1) * rad;
y = h;
vx = 0; vy = -sp * 4; vz = 0;
}
case "Heart" -> {
double t = f * Math.PI * 2;
double hx = 16 * Math.pow(Math.sin(t), 3);
double hy = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
x = hx * rad * 0.06;
z = 0;
y = 0.6 + hy * rad * 0.06;
vx = 0; vy = 0.015; vz = 0;
}
case "Cage" -> {
int edge = i % 12;
double s = rad * 0.5;
double t = rnd.nextDouble();
double ax = ((edge & 1) == 0) ? -s : s;
double az = ((edge & 2) == 0) ? -s : s;
double ay = ((edge & 4) == 0) ? 0 : h * 1.2;
if (edge < 4) { x = (t * 2 - 1) * s; y = ay * 0.6; z = az; }
else if (edge < 8) { x = ax; y = ay * 0.6; z = (t * 2 - 1) * s; }
else { x = ax; y = t * h * 1.2; z = az; }
vx = 0; vy = 0; vz = 0;
}
default -> {
x = (rnd.nextDouble() * 2 - 1) * area.get();
y = rnd.nextDouble() * h - 1;
z = (rnd.nextDouble() * 2 - 1) * area.get();
vx = (rnd.nextDouble() - 0.5) * d;
vy = -d;
vz = (rnd.nextDouble() - 0.5) * d;
}
}
mc.world.addParticle(e, px + x, py + y, pz + z, vx, vy, vz);
}
}
}