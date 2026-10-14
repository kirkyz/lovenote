package com.lovenote.setting;

import com.google.gson.JsonObject;

public class NumberSetting extends Setting {
	public final double min, max, step;
	private double value;

	public NumberSetting(String name, double def, double min, double max, double step) {
		super(name);
		this.min = min;
		this.max = max;
		this.step = step;
		set(def);
	}

	public double get() { return value; }
	public float getF() { return (float) value; }
	public int getI() { return (int) Math.round(value); }

	public void set(double v) {
		v = Math.max(min, Math.min(max, v));
		if (step > 0) v = Math.round((v - min) / step) * step + min;
		v = Math.max(min, Math.min(max, v));
		value = Math.round(v * 1000.0) / 1000.0;
	}

	@Override public void save(JsonObject o) { o.addProperty(name, value); }
	@Override public void load(JsonObject o) { if (o.has(name)) set(o.get(name).getAsDouble()); }
}
