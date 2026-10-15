package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Builds a shell of blocks around you. */
public class AutoTrap extends Module {
private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 2, 1, 10, 1));
private final BoolSetting roof = add(new BoolSetting("Roof", true));
private final BoolSetting rotate = add(new BoolSetting("Rotate", true));
private final BoolSetting swing = add(new BoolSetting("Swing hand", true));

private int cd;
private int index;

public AutoTrap() {
super("AutoTrap", Category.COMBAT);
}

@Override
public void onTick() {
if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
if (mc.currentScreen != null) return;
if (cd > 0) { cd--; return; }

BlockPos base = mc.player.getBlockPos();
BlockPos[] targets = roof.get()
? new BlockPos[]{ base.north(), base.south(), base.east(), base.west(),
base.north().up(), base.south().up(), base.east().up(), base.west().up(), base.up(2) }
: new BlockPos[]{ base.north(), base.south(), base.east(), base.west(),
base.north().up(), base.south().up(), base.east().up(), base.west().up() };

for (int n = 0; n < targets.length; n++) {
BlockPos pos = targets[(index + n) % targets.length];
if (!mc.world.getBlockState(pos).isAir()) continue;
if (place(pos)) {
index = (index + n + 1) % targets.length;
cd = delay.getI();
return;
}
}
}

private boolean place(BlockPos pos) {
BlockPos support = pos.down();
BlockState st = mc.world.getBlockState(support);
if (st.isAir() || st.getHardness(mc.world, support) < 0f) return false;

int slot = blockSlot();
if (slot == -1) return false;

Vec3d hit = Vec3d.ofCenter(support).add(0.0, 0.5, 0.0);
if (rotate.get()) look(hit);

int prev = mc.player.getInventory().selectedSlot;
mc.player.getInventory().selectedSlot = slot;
mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
new BlockHitResult(hit, Direction.UP, support, false));
if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
mc.player.getInventory().selectedSlot = prev;
return true;
}

private int blockSlot() {
for (int i = 0; i < 9; i++) {
ItemStack s = mc.player.getInventory().getStack(i);
if (!s.isEmpty() && s.getItem() instanceof BlockItem) return i;
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