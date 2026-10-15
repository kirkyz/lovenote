package com.lovenote.module;

public enum Category {
	COMBAT("rage", "\u25C6"),
	MOVEMENT("movement", "\u25B2"),
	VISUALS("visuals", "\u25CF"),
	HUD("hud", "\u25A0"),
	MISC("misc", "\u2666"),
	HYPIXEL("hypixel", "\u2605"),
	SPOTIFY("spotify", "\u266A");

	public final String label;
	public final String icon;

	Category(String label, String icon) {
		this.label = label;
		this.icon = icon;
	}
}