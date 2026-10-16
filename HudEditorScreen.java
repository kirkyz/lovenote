package com.lovenote.gui;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Module;
import com.lovenote.module.impl.HudWidget;
import com.lovenote.util.Colors;
import com.lovenote.util.Config;
import com.lovenote.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/** Drag HUD widgets around, snap them to guides, resize them, and add disabled ones from the tray. */
public class HudEditorScreen extends Screen {
	private HudWidget drag, resize;
	private int grabX, grabY;
	private final List<int[]> guides = new ArrayList<>(); // {vertical(1)/horizontal(0), position}
	private final List<Object[]> tray = new ArrayList<>(); // {HudWidget, int[]{x,y,w,h}}
	private long openAt;

	public HudEditorScreen() {
		super(Text.literal("HUD editor"));
	}

	@Override
	protected void init() {
		HudWidget.editing = true;
		openAt = System.currentTimeMillis();
	}

	@Override
	public void removed() {
		HudWidget.editing = false;
		Config.save();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private List<HudWidget> active() {
		List<HudWidget> l = new ArrayList<>();
		for (Module m : LovenoteClient.modules.all()) if (m instanceof HudWidget w && w.enabled) l.add(w);
		return l;
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		float a = Math.min(1f, (System.currentTimeMillis() - openAt) / 220f);
		ctx.fill(0, 0, width, height, Colors.alpha(0x000000, (int) (95 * a)));
		ctx.fill(width / 2, 0, width / 2 + 1, height, 0x18FFFFFF);
		ctx.fill(0, height / 2, width, height / 2 + 1, 0x18FFFFFF);

		for (HudWidget w : active()) {
			w.renderAt(ctx, delta);
			int x = w.px(width), y = w.py(height), pw = w.pxW(), ph = w.pxH();
			boolean hot = w == drag || w == resize || (mx >= x && mx <= x + pw && my >= y && my <= y + ph);
			int c = Colors.alpha(Colors.wave(x * 0.002f), hot ? 255 : 130);
			ctx.drawStrokedRectangle(x - 2, y - 2, pw + 4, ph + 4, c);
			ctx.fill(x + pw - 3, y + ph - 3, x + pw + 4, y + ph + 4, c);
			if (hot) ctx.drawTextWithShadow(textRenderer, w.name, x, y - 12, 0xFFFFFFFF);
		}

		for (int[] g : guides) {
			int c = Colors.alpha(Colors.C2, 200);
			if (g[0] == 1) ctx.fill(g[1], 0, g[1] + 1, height, c); else ctx.fill(0, g[1], width, g[1] + 1, c);
		}

		// tray of disabled widgets
		tray.clear();
		int tx = 10, ty = height - 24;
		ctx.drawTextWithShadow(textRenderer, "Add:", tx, ty + 4, 0xFFB0B0C0);
		tx += textRenderer.getWidth("Add:") + 8;
		for (Module m : LovenoteClient.modules.all()) {
			if (!(m instanceof HudWidget w) || w.enabled || w.hidden) continue;
			int cw = textRenderer.getWidth(w.name) + 12;
			boolean hov = mx >= tx && mx <= tx + cw && my >= ty && my <= ty + 15;
			RenderUtil.rrect(ctx, tx, ty, cw, 15, hov ? Colors.alpha(Colors.C2, 200) : 0xE0202028);
			ctx.drawText(textRenderer, w.name, tx + 6, ty + 4, hov ? 0xFF101015 : 0xFFD0D0DC, false);
			tray.add(new Object[]{w, new int[]{tx, ty, cw, 15}});
			tx += cw + 4;
		}
		ctx.drawCenteredTextWithShadow(textRenderer, "drag widgets  |  scroll or drag the corner to resize  |  right-click removes  |  esc saves",
				width / 2, 8, 0xFFE8E8F0);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		int btn = button;
		double mx = mouseX;
		double my = mouseY;
		for (Object[] t : tray) {
			int[] r = (int[]) t[1];
			if (mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3]) {
				HudWidget w = (HudWidget) t[0];
				w.setEnabled(true);
				w.fx = 0.4f;
				w.fy = 0.4f;
				return true;
			}
		}
		List<HudWidget> list = active();
		for (int i = list.size() - 1; i >= 0; i--) {
			HudWidget w = list.get(i);
			int x = w.px(width), y = w.py(height), pw = w.pxW(), ph = w.pxH();
			if (btn == 0 && mx >= x + pw - 6 && mx <= x + pw + 6 && my >= y + ph - 6 && my <= y + ph + 6) {
				resize = w;
				return true;
			}
			if (mx >= x && mx <= x + pw && my >= y && my <= y + ph) {
				if (btn == 1) { w.setEnabled(false); return true; }
				drag = w;
				grabX = (int) mx - x;
				grabY = (int) my - y;
				return true;
			}
		}
		return super.mouseClicked(new Click(mx, my, click.buttonInfo()), doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		int btn = button;
		double mx = mouseX;
		double my = mouseY;
		double deltaX = offsetX;
		double deltaY = offsetY;
		double dx = offsetX;
		double dy = offsetY;
		if (resize != null) {
			int x = resize.px(width);
			resize.size.set((mx - x) / Math.max(1.0, resize.w));
			return true;
		}
		if (drag != null) {
			int wpx = drag.pxW(), hpx = drag.pxH();
			int nx = (int) mx - grabX, ny = (int) my - grabY;
			guides.clear();

			List<int[]> xs = new ArrayList<>(), ys = new ArrayList<>();
			xs.add(new int[]{0, 0});
			xs.add(new int[]{(width - wpx) / 2, width / 2});
			xs.add(new int[]{width - wpx, width});
			ys.add(new int[]{0, 0});
			ys.add(new int[]{(height - hpx) / 2, height / 2});
			ys.add(new int[]{height - hpx, height});
			for (HudWidget o : active()) {
				if (o == drag) continue;
				int ox = o.px(width), oy = o.py(height), ow = o.pxW(), oh = o.pxH();
				xs.add(new int[]{ox, ox});
				xs.add(new int[]{ox + ow - wpx, ox + ow});
				xs.add(new int[]{ox + ow / 2 - wpx / 2, ox + ow / 2});
				xs.add(new int[]{ox + ow, ox + ow});
				ys.add(new int[]{oy, oy});
				ys.add(new int[]{oy + oh - hpx, oy + oh});
				ys.add(new int[]{oy + oh / 2 - hpx / 2, oy + oh / 2});
				ys.add(new int[]{oy + oh, oy + oh});
			}
			int[] bx = null, by = null;
			int bd = 6;
			for (int[] c : xs) { int d = Math.abs(nx - c[0]); if (d < bd) { bd = d; bx = c; } }
			bd = 6;
			for (int[] c : ys) { int d = Math.abs(ny - c[0]); if (d < bd) { bd = d; by = c; } }
			if (bx != null) { nx = bx[0]; guides.add(new int[]{1, bx[1]}); }
			if (by != null) { ny = by[0]; guides.add(new int[]{0, by[1]}); }

			drag.fx = MathHelper.clamp(nx / (float) width, 0f, Math.max(0f, (width - wpx) / (float) width));
			drag.fy = MathHelper.clamp(ny / (float) height, 0f, Math.max(0f, (height - hpx) / (float) height));
			return true;
		}
		return super.mouseDragged(new Click(mx, my, click.buttonInfo()), dx, dy);
	}

	@Override
	public boolean mouseReleased(Click click) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		int btn = button;
		double mx = mouseX;
		double my = mouseY;
		drag = null;
		resize = null;
		guides.clear();
		return super.mouseReleased(new Click(mx, my, click.buttonInfo()));
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
		return super.mouseScrolled(mx, my, horizontalAmount, verticalAmount);
	}
}
