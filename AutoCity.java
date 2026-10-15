package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Chews through the blocks holding your opponent in their hole. */
public class AutoCity extends Module {
private final NumberSetting range = add(new NumberSetting("Target range", 4.5, 2, 8, 0.5));
private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 2, 1, 10, 1));
private final BoolSetting roof = add(new BoolSetting("Break roof", true));
private final BoolSetting swing = add(new BoolSetting("Swing hand", true));

private BlockPos current;
private int cd;

public AutoCity() {
super("AutoCity", Category.COMBAT);
}

@Override
public void onDisable() {
current = null;
}

@Override
public void onTick() {
if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
if (mc.currentScreen != null) return;

if (current != null && !mc.world.getBlockState(current).isAir()) {
if (cd > 0) { cd--; return; }
mc.interactionManager.updateBlockBreakingProgress(current, Direction.UP);
if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
cd = delay.getI();
return;
}

current = findTarget();
}

private BlockPos findTarget() {
PlayerEntity best = null;
double bd = range.get();
for (PlayerEntity p : mc.world.getPlayers()) {
if (p == mc.player || !p.isAlive()) continue;
double d = mc.player.distanceTo(p);
if (d < bd) { bd = d; best = p; }
}
if (best == null) return null;

BlockPos base = best.getBlockPos();
BlockPos[] cands = roof.get()
? new BlockPos[]{ base.north(), base.south(), base.east(), base.west(), base.up(2), base.up(3) }
: new BlockPos[]{ base.north(), base.south(), base.east(), base.west(), base.up(2) };

for (BlockPos pos : cands) {
BlockState st = mc.world.getBlockState(pos);
if (st.isAir()) continue;
if (st.getHardness(mc.world, pos) < 0f) continue;
return pos;
}
return null;
}
}