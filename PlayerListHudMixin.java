package com.lovenote.mixin;

import com.lovenote.LovenoteClient;
import com.lovenote.module.impl.TabList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the vanilla tab list while our own TabList widget is enabled. */
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	@Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void lovenote$playerList(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard,
			ScoreboardObjective objective, CallbackInfo ci) {
		if (LovenoteClient.modules == null) return;
		TabList t = LovenoteClient.modules.get(TabList.class);
		if (t != null && t.enabled) ci.cancel();
	}
}