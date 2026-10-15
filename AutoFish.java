package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;

import java.util.Random;

/** Fishing macro with human reaction time and the occasional deliberate miss. */
public class AutoFish extends Module {
private final NumberSetting reactMin = add(new NumberSetting("Reaction min (ms)", 180, 50, 800, 10));
private final NumberSetting reactMax = add(new NumberSetting("Reaction max (ms)", 420, 100, 1200, 10));
private final NumberSetting recastMin = add(new NumberSetting("Recast min (ms)", 900, 200, 4000, 50));
private final NumberSetting recastMax = add(new NumberSetting("Recast max (ms)", 1600, 300, 6000, 50));
private final NumberSetting missChance = add(new NumberSetting("Miss chance (%)", 6, 0, 40, 1));
private final NumberSetting sensitivity = add(new NumberSetting("Bite sensitivity", 0.035, 0.01, 0.12, 0.005));
private final BoolSetting antiAfk = add(new BoolSetting("Anti-AFK", true));

private final Random rnd = new Random();
private boolean waiting;
private long reactAt;
private long recastAt;
private double prevY;
private boolean hadHook;
private int afkTicks;

public AutoFish() {
super("AutoFish", Category.HYPIXEL);
}

@Override
public void onDisable() {
waiting = false;
}

@Override
public void onTick() {
if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
if (mc.player.getMainHandStack().getItem() != Items.FISHING_ROD) return;

if (antiAfk.get() && ++afkTicks > 600) {
afkTicks = 0;
mc.player.setPitch(mc.player.getPitch() + (rnd.nextBoolean() ? 1 : -1) * (1 + rnd.nextInt(3)));
}

FishingBobberEntity hook = mc.player.fishHook;
long now = System.currentTimeMillis();

if (hook == null) {
if (now < recastAt) return;
((com.lovenote.mixin.MinecraftClientAccessor) mc).invokeDoItemUse();
recastAt = now + 400 + rnd.nextInt(300);
waiting = false;
hadHook = false;
return;
}

if (!hadHook) {
hadHook = true;
prevY = hook.getY();
return;
}

double dy = hook.getY() - prevY;
prevY = hook.getY();

if (!waiting && dy < -sensitivity.get()) {
waiting = true;
reactAt = now + reactMin.getI()
+ (long) (rnd.nextDouble() * Math.max(1, reactMax.getI() - reactMin.getI()));
if (rnd.nextInt(100) < missChance.getI()) reactAt += 350 + rnd.nextInt(500);
return;
}

if (waiting && now >= reactAt) {
waiting = false;
((com.lovenote.mixin.MinecraftClientAccessor) mc).invokeDoItemUse();
recastAt = now + recastMin.getI()
+ (long) (rnd.nextDouble() * Math.max(1, recastMax.getI() - recastMin.getI()));
}
}
}
