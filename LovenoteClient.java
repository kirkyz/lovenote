package com.lovenote;
import net.minecraft.client.util.Window;

import com.lovenote.gui.HudEditorScreen;
import com.lovenote.gui.LovenoteScreen;
import com.lovenote.module.Module;
import com.lovenote.module.ModuleManager;
import com.lovenote.module.impl.Fog;
import com.lovenote.module.impl.InterfaceModule;
import com.lovenote.spotify.SpotifyManager;
import com.lovenote.util.Config;
import com.lovenote.util.WorldRender;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class LovenoteClient implements ClientModInitializer {
	public static ModuleManager modules;
	public static KeyBinding openGui, hudEditor, spotifyNext, spotifyPrev, spotifyPause;

	private static final KeyBinding.Category CAT = KeyBinding.Category.create(Identifier.of("lovenote", "main"));

	@Override
	public void onInitializeClient() {
		modules = new ModuleManager();
		Config.load();
		InterfaceModule theme = modules.get(InterfaceModule.class);
		if (theme != null) theme.apply();
		SpotifyManager.I.start();

		openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Lovenote menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CAT));
		hudEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding("Edit HUD layout", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CAT));
		spotifyNext = KeyBindingHelper.registerKeyBinding(new KeyBinding("Spotify: next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CAT));
		spotifyPrev = KeyBindingHelper.registerKeyBinding(new KeyBinding("Spotify: previous", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CAT));
		spotifyPause = KeyBindingHelper.registerKeyBinding(new KeyBinding("Spotify: play/pause", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CAT));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openGui.wasPressed()) mc.setScreen(new LovenoteScreen());
			while (hudEditor.wasPressed()) mc.setScreen(new HudEditorScreen());
			while (spotifyNext.wasPressed()) SpotifyManager.I.next();
			while (spotifyPrev.wasPressed()) SpotifyManager.I.previous();
			while (spotifyPause.wasPressed()) SpotifyManager.I.togglePlay();

			if (mc.player == null || mc.world == null) return;

			Window win = mc.getWindow();
			for (Module m : modules.all()) {
				if (m.key == -1) continue;
				boolean down = InputUtil.isKeyPressed(win, m.key);
				if (down && !m.keyDown && mc.currentScreen == null) m.toggle();
				m.keyDown = down;
			}
			modules.tick();
		});

		WorldRender.START.add(ctx -> Fog.active = true);
		WorldRender.END.add(ctx -> Fog.active = false);

		HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null || mc.options.hudHidden) return;
			if (mc.currentScreen instanceof HudEditorScreen) return;
			modules.hud(ctx, tickCounter.getTickProgress(false));
		});

		WorldRender.LAST.add(ctx -> {
			if (MinecraftClient.getInstance().player == null) return;
			modules.render3D(ctx);
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> Config.save());
	}
}
