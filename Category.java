package com.lovenote.module;

public enum Category {
	COMBAT("rage"),
	MOVEMENT("movement"),
	VISUALS("visuals"),
	HUD("hud"),
	MISC("misc"),
	SPOTIFY("spotify");

	public final String label;

	Category(String label) { this.label = label; }
}
