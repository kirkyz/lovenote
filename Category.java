package com.lovenote.module;

public enum Category { COMBAT("Combat"), MOVEMENT("Movement"), VISUALS("Visuals"), RENDER("Render"), HUD("HUD"), MISC("Misc"), HYPIXEL("Hypixel"), SPOTIFY("Spotify"), THEMES("Themes");

public final String label;

Category(String label) {
    this.label = label;
}


}