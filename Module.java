package com.lovenote.module;
import com.lovenote.module.Category;
import com.google.gson.JsonObject;
import com.lovenote.setting.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
	protected final MinecraftClient mc = MinecraftClient.getInstance();

	public final String name;
	public final Category category;
	public final List<Setting> settings = new ArrayList<>();
	public String description = "";
	public boolean enabled;
	public boolean hidden;
	public int key = -1;
	public boolean keyDown;

	protected Module(String name, Category category) {
		this.name = name;
		this.category = category;
	}
	protected void describe(String text) {
    this.description = text;
}

	protected <T extends Setting> T add(T s) {
		settings.add(s);
		return s;
	}

	public void toggle() { setEnabled(!enabled); }

	public void setEnabled(boolean e) {
		if (e == enabled) return;
		enabled = e;
		if (e) onEnable(); else onDisable();
		if (category != Category.HUD && !hidden) com.lovenote.module.impl.Notifications.push(name, e);
	}

	public void onEnable() {}
	public void onDisable() {}
	public void onTick() {}
	public void onAttack(Entity target) {}
	public void onTotemPop(Entity entity) {}
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {}
	public void onHud(DrawContext ctx, float tickDelta) {}

	/** Extra data saved next to the settings (HUD positions, custom colours...). */
	public void saveExtra(JsonObject o) {}
	public void loadExtra(JsonObject o) {}
}
