package com.lovenote.setting;

import com.google.gson.JsonObject;

public class ModeSetting extends Setting {
	public final String[] modes;
	private int index;

	public ModeSetting(String name, String def, String... modes) {
		super(name);
		this.modes = modes;
		for (int i = 0; i < modes.length; i++) if (modes[i].equals(def)) index = i;
	}

	public String get() { return modes[index]; }
	public boolean is(String m) { return modes[index].equals(m); }

	public void cycle(int dir) {
		index = (index + dir + modes.length) % modes.length;
	}

	@Override public void save(JsonObject o) { o.addProperty(name, modes[index]); }
	@Override public void load(JsonObject o) {
		if (!o.has(name)) return;
		String v = o.get(name).getAsString();
		for (int i = 0; i < modes.length; i++) if (modes[i].equals(v)) index = i;
	}
}
