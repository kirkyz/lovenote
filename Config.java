package com.lovenote.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovenote.LovenoteClient;
import com.lovenote.module.Module;
import com.lovenote.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Config() {}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("lovenote.json");
	}

	public static void save() {
		try {
			JsonObject root = new JsonObject();
			for (Module m : LovenoteClient.modules.all()) {
				JsonObject o = new JsonObject();
				o.addProperty("enabled", m.enabled);
				o.addProperty("key", m.key);
				JsonObject s = new JsonObject();
				for (Setting st : m.settings) st.save(s);
				o.add("settings", s);
				m.saveExtra(o);
				root.add(m.name, o);
			}
			Files.writeString(path(), GSON.toJson(root));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void load() {
		Path p = path();
		if (!Files.exists(p)) return;
		try {
			JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
			for (Module m : LovenoteClient.modules.all()) {
				if (!root.has(m.name)) continue;
				JsonObject o = root.getAsJsonObject(m.name);
				if (o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
				if (o.has("key")) m.key = o.get("key").getAsInt();
				if (o.has("settings")) {
					JsonObject s = o.getAsJsonObject("settings");
					for (Setting st : m.settings) st.load(s);
				}
				m.loadExtra(o);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
