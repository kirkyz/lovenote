package com.lovenote.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Tiny stand-in for Fabric's WorldRenderEvents. */
public final class WorldRender {
	public static final List<Consumer<WorldRenderContext>> START = new ArrayList<>();
	public static final List<Consumer<WorldRenderContext>> END = new ArrayList<>();
	public static final List<Consumer<WorldRenderContext>> LAST = new ArrayList<>();

	private WorldRender() {}

	public static void fire(List<Consumer<WorldRenderContext>> list, WorldRenderContext ctx) {
		for (Consumer<WorldRenderContext> c : list) c.accept(ctx);
	}
}
