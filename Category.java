package com.lovenote.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	VISUALS("Visuals"),
	HUD("HUD"),
	MISC("Misc"),
	SPOTIFY("Spotify"),
	THEMES("Themes");

	public final String label;

	Category(String label) { this.label = label; }
}
