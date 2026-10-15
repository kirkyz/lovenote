package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Places an end crystal next to your target, then pops it. */
public class AutoCrystal extends Module {
private final NumberSetting targetRange = add(new NumberSetting("Target range", 5.0, 2, 8, 0.5));
private final NumberSetting placeRange = add(new NumberSetting("Place range", 4.5, 2, 6, 0.5));
private final NumberSetting breakRange = add(new NumberSetting("Break range", 4.5, 2, 6, 0.5));
private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 2, 1, 10, 1));
private final BoolSetting rotate = add(new BoolSetting("Rotate", true));
private final BoolSetting swing = add(new BoolSetting("Swing hand", true));
private final BoolSetting avoidSelf = add(new BoolSetting("Avoid self", true));

private int cd;

public AutoCrystal() {
super("AutoCrystal", Category.COMBAT);
}

@Override
public void onTick() {
if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
if (mc.currentScreen != null) return;
if (cd > 0) { cd--; return; }

PlayerEntity target = nearest();
if (target == null) return;

EndCrystalEntity crystal = crystalNear(target);
if (crystal != null) {
if (rotate.get()) look(crystal.getPos());
mc.interactionManager.attackEntity(mc.player, crystal);
if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
cd = delay.getI();
return;
}

int slot = crystalSlot();
if (slot == -1) return;
BlockPos base = findBase(target);
if (base == null) return;

Vec3d hit = Vec3d.ofCenter(base).add(0.0, 0.5, 0.0);
int prev = mc.player.getInventory().selectedSlot;
mc.player.getInventory().selectedSlot = slot;
if (rotate.get()) look(hit);
mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
new BlockHitResult(hit, Direction.UP, base, false));
if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
mc.player.getInventory().selectedSlot = prev;
cd = delay.getI();
}

private PlayerEntity nearest() {
PlayerEntity best = null;
double bd = targetRange.get();
for (PlayerEntity p : mc.world.getPlayers()) {
if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
double d = mc.player.distanceTo(p);
if (d < bd) { bd = d; best = p; }
}
return best;
}

private EndCrystalEntity crystalNear(PlayerEntity target) {
Box box = target.getBoundingBox().expand(breakRange.get());
EndCrystalEntity best = null;
double bd = Double.MAX_VALUE;
for (Entity e : mc.world.getEntitiesByClass(EndCrystalEntity.class, box, x -> true)) {
double d = mc.player.distanceTo(e);
if (d > breakRange.get() + 0.5) continue;
if (d < bd) { bd = d; best = (EndCrystalEntity) e; }
}
return best;
}

private BlockPos findBase(PlayerEntity target) {
BlockPos tp = target.getBlockPos();
BlockPos[] cands = { tp, tp.north(), tp.south(), tp.east(), tp.west(), tp.down(),
tp.up(), tp.north().down(), tp.south().down(), tp.east().down(), tp.west().down() };
BlockPos best = null;
double bd = Double.MAX_VALUE;
for (BlockPos base : cands) {
BlockState st = mc.world.getBlockState(base);
if (st.isAir()) continue;
BlockPos above = base.up();
if (!mc.world.getBlockState(above).isAir()) continue;
if (avoidSelf.get() && mc.player.getBlockPos().equals(above)) continue;
double d = mc.player.getPos().distanceTo(Vec3d.ofCenter(above));
if (d > placeRange.get()) continue;
if (!mc.world.getOtherEntities(null, new Box(above)).isEmpty()) continue;
if (d < bd) { bd = d; best = base; }
}
return best;
}

private int crystalSlot() {
for (int i = 0; i < 9; i++) {
ItemStack st = mc.player.getInventory().getStack(i);
if (!st.isEmpty() && st.getItem() == Items.END_CRYSTAL) return i;
}
return -1;
}

private void look(Vec3d at) {
Vec3d eye = mc.player.getEyePos();
double dx = at.x - eye.x, dy = at.y - eye.y, dz = at.z - eye.z;
double flat = Math.sqrt(dx * dx + dz * dz);
mc.player.setYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, flat)));
}
}