package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Re-draws entity models with a flat colour, visible through walls. */
public class Chams extends Module {

	private final ModeSetting targets = add(new ModeSetting("Targets", "Players", "Players", "Mobs", "All"));
	private final ModeSetting colour = add(new ModeSetting("Colour", "Theme", "Theme", "Rainbow", "Solid", "Health"));
	private final NumberSetting alpha = add(new NumberSetting("Opacity", 200, 20, 255, 5));
	private final BoolSetting throughWalls = add(new BoolSetting("Through walls", true));
	private final BoolSetting self = add(new BoolSetting("Include self", true));

	public Chams() {
		super("Chams", Category.VISUALS);
		describe("Re-colours entity models, visible through walls");
	}

	public boolean should(LivingEntity e) {
		if (mc.player == null) return false;
		if (e == mc.player && !self.get()) return false;
		if (e.isInvisibleTo(mc.player)) return false;
		String t = targets.get();
		boolean player = e instanceof PlayerEntity;
		if (t.equals("Players") && !player) return false;
		if (t.equals("Mobs") && player) return false;
		return true;
	}

	public int tint(LivingEntity e) {
		float off = (e.getId() % 16) / 16f;
		int base = switch (colour.get()) {
			case "Rainbow" -> Colors.rainbow(off);
			case "Solid" -> 0xFFFFFFFF;
			case "Health" -> Colors.health(e.getHealth() / Math.max(1f, e.getMaxHealth()));
			default -> Colors.wave(off);
		};
		return Colors.alpha(base, alpha.getI());
	}

	public boolean throughWalls() { return throughWalls.get(); }
}
