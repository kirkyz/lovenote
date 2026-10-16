package com.lovenote.module.impl;
import net.minecraft.client.gl.RenderPipelines;

import com.google.gson.JsonObject;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

/**
 * Base class for draggable HUD widgets. Position is stored as a fraction of the screen
 * so it survives resolution / GUI-scale changes. Drawn in local space starting at (0,0).
 */
public abstract class HudWidget extends Module {
	/** true while the HUD editor is open: widgets show placeholder content. */
	public static boolean editing;

	public final NumberSetting size = add(new NumberSetting("Size", 1.0, 0.5, 2.0, 0.05));
	public final NumberSetting opacity = add(new NumberSetting("Opacity", 85, 20, 100, 5));

	public float fx, fy;
	/** measured unscaled size, updated by draw() */
	public int w = 80, h = 20;

	protected HudWidget(String name, float fx, float fy) {
		super(name, Category.HUD);
		this.fx = fx;
		this.fy = fy;
	}

	protected abstract void draw(DrawContext ctx, float td);

	protected int panel() {
		return Colors.alpha(0x101015, (int) (opacity.get() * 2.55));
	}

	protected void box(DrawContext ctx, int x, int y, int bw, int bh) {
		RenderUtil.rrect(ctx, x, y, bw, bh, panel());
	}

	protected void head(DrawContext ctx, Entity e, int x, int y, int s) {
		if (e instanceof AbstractClientPlayerEntity p) {
			Identifier tex = p.getSkin().body().texturePath();
			ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 8f, 8f, s, s, 8, 8, 64, 64);
			ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 40f, 8f, s, s, 8, 8, 64, 64);
		} else {
			RenderUtil.rrect(ctx, x, y, s, s, 0xFF2A2A36);
		}
	}

	@Override
	public void onHud(DrawContext ctx, float td) {
		renderAt(ctx, td);
	}

	public void renderAt(DrawContext ctx, float td) {
		Matrix3x2fStack m = ctx.getMatrices();
		m.pushMatrix();
		m.translate(px(ctx.getScaledWindowWidth()), py(ctx.getScaledWindowHeight()));
		float s = size.getF();
		m.scale(s, s);
		draw(ctx, td);
		m.popMatrix();
	}

	public int px(int sw) { return Math.round(fx * sw); }
	public int py(int sh) { return Math.round(fy * sh); }
	public int pxW() { return Math.max(8, Math.round(w * size.getF())); }
	public int pxH() { return Math.max(8, Math.round(h * size.getF())); }

	@Override
	public void saveExtra(JsonObject o) {
		o.addProperty("fx", fx);
		o.addProperty("fy", fy);
	}

	@Override
	public void loadExtra(JsonObject o) {
		if (o.has("fx")) fx = o.get("fx").getAsFloat();
		if (o.has("fy")) fy = o.get("fy").getAsFloat();
	}
}
