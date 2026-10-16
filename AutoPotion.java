package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

/** Throws a splash healing potion at your feet when health is low. */
public class AutoPotion extends Module {
	private final NumberSetting health = add(new NumberSetting("Health", 8, 1, 19, 1));
	private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 12, 2, 40, 1));
	private int timer;

	public AutoPotion() {
		super("AutoPotion", Category.COMBAT);
	}

	private int findHeal() {
		for (int i = 0; i < 9; i++) {
			ItemStack st = mc.player.getInventory().getStack(i);
			if (!st.isOf(Items.SPLASH_POTION)) continue;
			PotionContentsComponent contents = st.get(DataComponentTypes.POTION_CONTENTS);
			if (contents == null) continue;
			boolean heal = false;
			for (StatusEffectInstance e : contents.getEffects()) {
				if (e.getEffectType() == StatusEffects.INSTANT_HEALTH) { heal = true; break; }
			}
			if (heal) return i;
		}
		return -1;
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.interactionManager == null) return;
		if (timer > 0) { timer--; return; }
		if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.get()) return;
		int slot = findHeal();
		if (slot == -1) return;

		int prev = mc.player.getInventory().getSelectedSlot();
		mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), 90f, mc.player.isOnGround(), false));
		mc.player.getInventory().setSelectedSlot(slot);
		mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
		mc.player.getInventory().setSelectedSlot(prev);
		timer = delay.getI();
	}
}