package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/** Randomised auto clicker. Runs per frame, so intervals are not tick-locked. */
public class AutoClicker extends Module {
private final NumberSetting minCps = add(new NumberSetting("Min CPS", 8, 1, 20, 0.5));
private final NumberSetting maxCps = add(new NumberSetting("Max CPS", 13, 1, 20, 0.5));
private final BoolSetting left = add(new BoolSetting("Left click", true));
private final BoolSetting right = add(new BoolSetting("Right click", false));
private final BoolSetting onlyHolding = add(new BoolSetting("Only while holding", true));
private final NumberSetting pauseChance = add(new NumberSetting("Pause chance (%)", 4, 0, 30, 1));
private final NumberSetting pauseMs = add(new NumberSetting("Pause length (ms)", 130, 30, 900, 10));
private final BoolSetting jitterLook = add(new BoolSetting("Look jitter", false));
private final NumberSetting jitterAmt = add(new NumberSetting("Jitter amount", 0.4, 0.05, 3, 0.05));

private final Random rnd = new Random();
private long nextClick;
private long pauseUntil;
private long nextJitter;

public AutoClicker() {
super("AutoClicker", Category.HYPIXEL);
}

private long interval() {
double lo = Math.min(minCps.get(), maxCps.get());
double hi = Math.max(minCps.get(), maxCps.get());
double cps = lo + rnd.nextDouble() * (hi - lo);
if (cps < 0.1) cps = 0.1;
return (long) (1000.0 / cps);
}

@Override
public void onHud(DrawContext ctx, float td) {
if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
if (mc.currentScreen != null) return;

long now = System.currentTimeMillis();
if (now < pauseUntil) return;
if (onlyHolding.get() && !mc.options.attackKey.isPressed() && !mc.options.useKey.isPressed()) return;

if (jitterLook.get() && now >= nextJitter) {
nextJitter = now + 90 + rnd.nextInt(260);
double a = jitterAmt.get();
mc.player.setYaw(mc.player.getYaw() + (float) ((rnd.nextDouble() * 2 - 1) * a));
mc.player.setPitch(Math.max(-90f, Math.min(90f,
mc.player.getPitch() + (float) ((rnd.nextDouble() * 2 - 1) * a * 0.6))));
}

int guard = 0;
while (now >= nextClick && guard++ < 3) {
if (rnd.nextInt(100) < pauseChance.getI()) {
pauseUntil = now + (long) (pauseMs.get() * (0.6 + rnd.nextDouble() * 0.8));
nextClick = pauseUntil;
break;
}
if (left.get()) ((com.lovenote.mixin.MinecraftClientAccessor) mc).invokeDoAttack();
if (right.get()) ((com.lovenote.mixin.MinecraftClientAccessor) mc).invokeDoItemUse();
nextClick = now + interval();
}
if (nextClick < now) nextClick = now + interval();
}
}
