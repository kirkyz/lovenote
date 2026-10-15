package com.lovenote.setting;

import com.google.gson.JsonObject;

/** A set of independent on/off chips (e.g. Targets: Players, Mobs, Animals...). */
public class MultiSetting extends Setting {
	public final String[] names;
	private final boolean[] on;

	public MultiSetting(String name, String[] names, String... defaultsOn) {
		super(name);
		this.names = names;
		this.on = new boolean[names.length];
		for (String d : defaultsOn) for (int i = 0; i < names.length; i++) if (names[i].equals(d)) on[i] = true;
	}

	public boolean get(int i) { return on[i]; }
	public void toggle(int i) { on[i] = !on[i]; }

	public boolean is(String n) {
		for (int i = 0; i < names.length; i++) if (names[i].equals(n)) return on[i];
		return false;
	}

	@Override public void save(JsonObject o) {
		JsonObject sub = new JsonObject();
		for (int i = 0; i < names.length; i++) sub.addProperty(names[i], on[i]);
		o.add(name, sub);
	}

	@Override public void load(JsonObject o) {
		if (!o.has(name) || !o.get(name).isJsonObject()) return;
		JsonObject sub = o.getAsJsonObject(name);
		for (int i = 0; i < names.length; i++) if (sub.has(names[i])) on[i] = sub.get(names[i]).getAsBoolean();
	}
}
