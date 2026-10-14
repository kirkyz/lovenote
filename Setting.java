package com.lovenote.setting;

import com.google.gson.JsonObject;

public abstract class Setting {
	public final String name;

	protected Setting(String name) {
		this.name = name;
	}

	public abstract void save(JsonObject o);

	public abstract void load(JsonObject o);
}
