package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;

import java.util.Random;

/** SkyBlock farming macro: hold, row-follow or spiral, with humanised timing. */
public class AutoFarm extends Module {
private final ModeSetting mode = add(new ModeSetting("Mode", "Row", "Row", "Hold", "Spiral", "Mine", "Cobble"));
private final NumberSetting rowSeconds = add(new NumberSetting("Row length (s)", 8, 2, 60, 1));
private final NumberSetting turnAngle = add(new NumberSetting("Turn angle", 180, 90, 180, 5));
private final NumberSetting turnSpeed = add(new NumberSetting("Turn speed (deg/tick)", 9, 2, 30, 1));
private final BoolSetting sneak = add(new BoolSetting("Sneak", false));
private final BoolSetting jump = add(new BoolSetting("Jump", false));
private final BoolSetting pauseInGui = add(new BoolSetting("Pause in GUI", true));
private final NumberSetting humanize = add(new NumberSetting("Humanize (%)", 20, 0, 60, 5));

private final Random rnd = new Random();
private int timer;
private float targetYaw;
private boolean active;

public AutoFarm() {
super("AutoFarm", Category.HYPIXEL);
}

@Override
public void onDisable() {
release();
}

private void release() {
if (mc.options == null) return;
mc.options.attackKey.setPressed(false);
mc.options.forwardKey.setPressed(false);
mc.options.sneakKey.setPressed(false);
mc.options.jumpKey.setPressed(false);
active = false;
}

private int jitter(int base) {
int j = (int) (base * humanize.getI() / 100.0);
return Math.max(1, base + (j <= 0 ? 0 : rnd.nextInt(j * 2 + 1) - j));
}

@Override
public void onTick() {
if (mc.player == null || mc.world == null) return;
if (pauseInGui.get() && mc.currentScreen != null) {
if (active) release();
return;
}

active = true;
mc.options.attackKey.setPressed(true);
mc.options.sneakKey.setPressed(sneak.get());

String m = mode.get();
boolean move = m.equals("Row") || m.equals("Spiral") || m.equals("Mine");
mc.options.forwardKey.setPressed(move);

if (jump.get()) mc.options.jumpKey.setPressed(mc.player.isOnGround());
else mc.options.jumpKey.setPressed(false);

if (m.equals("Row")) {
if (++timer >= jitter(rowSeconds.getI() * 20)) {
timer = 0;
targetYaw = mc.player.getYaw() + turnAngle.getI() * (rnd.nextBoolean() ? 1 : -1);
}
smoothTurn();
} else if (m.equals("Spiral")) {
targetYaw = mc.player.getYaw() + (float) (0.6 + rnd.nextDouble() * 0.4);
smoothTurn();
}
}

private void smoothTurn() {
float diff = ((targetYaw - mc.player.getYaw() + 540f) % 360f) - 180f;
if (Math.abs(diff) < 0.5f) return;
float step = Math.min(Math.abs(diff), (float) (turnSpeed.get() * (0.7 + rnd.nextDouble() * 0.6)));
mc.player.setYaw(mc.player.getYaw() + Math.signum(diff) * step);
}
}