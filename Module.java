package com.lovenote.module;

import com.google.gson.JsonObject;
import com.lovenote.setting.Setting;
import com.lovenote.util.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class Module {
	protected final MinecraftClient mc = MinecraftClient.getInstance();

	public final String name;
	public final Category category;
	public final List<Setting> settings = new ArrayList<>();
	/** Extra tabs this module is also listed under. */
	public final Set<Category> alsoIn = new HashSet<>();
	public String description = "";
	public boolean favorite;
	public boolean hidden;
	public boolean enabled;
	public int key = -1;
	public boolean keyDown;

	protected Module(String name, Category category) {
		this.name = name;
		this.category = category;
	}

	protected <T extends Setting> T add(T s) {
		settings.add(s);
		return s;
	}

	public Module describe(String d) {
		this.description = d;
		return this;
	}

	public boolean matches(String q) {
		if (q.isEmpty()) return true;
		return name.toLowerCase(Locale.ROOT).contains(q)
				|| description.toLowerCase(Locale.ROOT).contains(q);
	}

	public void toggle() { setEnabled(!enabled); }

	public void setEnabled(boolean e) {
		if (e == enabled) return;
		enabled = e;
		if (e) onEnable(); else onDisable();
		if (category != Category.HUD) com.lovenote.module.impl.Notifications.push(name, e);
	}

	public void onEnable() {}
	public void onDisable() {}
	public void onTick() {}
	public void onAttack(Entity target) {}
	public void onTotemPop(Entity entity) {}
	public void onRender3D(WorldRenderContext ctx, MatrixStack m) {}
	public void onHud(DrawContext ctx, float tickDelta) {}
	public void saveExtra(JsonObject o) {}
	public void loadExtra(JsonObject o) {}
}
