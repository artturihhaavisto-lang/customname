package dev.customname.gui;

import java.awt.Color;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Saturation/value square + hue strip color picker. */
public class ColorPickerWidget extends AbstractWidget {
	private ScreenRectangle svRect = ScreenRectangle.empty();
	private ScreenRectangle hueRect = ScreenRectangle.empty();
	private float hue;
	private float saturation = 1.0F;
	private float value = 1.0F;
	private boolean dragSv;
	private boolean dragHue;
	private IntConsumer onChange = rgb -> {
	};

	public ColorPickerWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.literal("picker"));
		this.layout();
	}

	public void setOnChange(IntConsumer onChange) {
		this.onChange = onChange != null ? onChange : rgb -> {
		};
	}

	public void setRgb(int rgb) {
		float[] hsb = Color.RGBtoHSB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, null);
		this.hue = hsb[0];
		this.saturation = hsb[1];
		this.value = hsb[2];
	}

	public int getRgb() {
		return Color.HSBtoRGB(this.hue, this.saturation, this.value) & 0xFFFFFF;
	}

	private void layout() {
		int hueH = 10;
		int gap = 6;
		this.hueRect = new ScreenRectangle(this.getX(), this.getBottom() - hueH, this.getWidth(), hueH);
		this.svRect = new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.hueRect.top() - this.getY() - gap);
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		this.layout();
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		this.layout();
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		this.layout();
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		this.layout();
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		int mx = (int)event.x();
		int my = (int)event.y();
		this.dragSv = this.svRect.containsPoint(mx, my);
		this.dragHue = this.hueRect.containsPoint(mx, my);
		this.applyPointer(mx, my);
	}

	@Override
	protected void onDrag(MouseButtonEvent event, double dx, double dy) {
		if (this.dragSv || this.dragHue) {
			this.applyPointer((int)event.x(), (int)event.y());
		}
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		this.dragSv = false;
		this.dragHue = false;
	}

	private void applyPointer(int mx, int my) {
		if (this.dragHue) {
			this.hue = Mth.clamp((mx - this.hueRect.left()) / (float)Math.max(1, this.hueRect.width() - 1), 0.0F, 1.0F);
			this.onChange.accept(this.getRgb());
		}
		if (this.dragSv) {
			this.saturation = Mth.clamp((mx - this.svRect.left()) / (float)Math.max(1, this.svRect.width() - 1), 0.0F, 1.0F);
			this.value = 1.0F - Mth.clamp((my - this.svRect.top()) / (float)Math.max(1, this.svRect.height() - 1), 0.0F, 1.0F);
			this.onChange.accept(this.getRgb());
		}
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.layout();
		int pure = Color.HSBtoRGB(this.hue, 1.0F, 1.0F) | 0xFF000000;
		for (int px = 0; px < this.svRect.width(); px++) {
			float s = this.svRect.width() <= 1 ? 0.0F : px / (float)(this.svRect.width() - 1);
			int top = Color.HSBtoRGB(this.hue, s, 1.0F) | 0xFF000000;
			graphics.fillGradient(this.svRect.left() + px, this.svRect.top(), this.svRect.left() + px + 1, this.svRect.bottom(), top, 0xFF000000);
		}
		graphics.outline(this.svRect.left() - 1, this.svRect.top() - 1, this.svRect.width() + 2, this.svRect.height() + 2, 0x55FFFFFF);

		int thumbX = this.svRect.left() + Math.round(this.saturation * Math.max(0, this.svRect.width() - 1));
		int thumbY = this.svRect.top() + Math.round((1.0F - this.value) * Math.max(0, this.svRect.height() - 1));
		graphics.fill(thumbX - 2, thumbY - 2, thumbX + 3, thumbY + 3, 0xFF000000);
		graphics.fill(thumbX - 1, thumbY - 1, thumbX + 2, thumbY + 2, 0xFFFFFFFF);
		graphics.fill(thumbX, thumbY, thumbX + 1, thumbY + 1, pure);

		for (int px = 0; px < this.hueRect.width(); px++) {
			float h = this.hueRect.width() <= 1 ? 0.0F : px / (float)(this.hueRect.width() - 1);
			int c = Color.HSBtoRGB(h, 1.0F, 1.0F) | 0xFF000000;
			graphics.fill(this.hueRect.left() + px, this.hueRect.top(), this.hueRect.left() + px + 1, this.hueRect.bottom(), c);
		}
		graphics.outline(this.hueRect.left() - 1, this.hueRect.top() - 1, this.hueRect.width() + 2, this.hueRect.height() + 2, 0x55FFFFFF);
		int hueThumb = this.hueRect.left() + Math.round(this.hue * Math.max(0, this.hueRect.width() - 1));
		graphics.fill(hueThumb - 1, this.hueRect.top() - 1, hueThumb + 2, this.hueRect.bottom() + 1, 0xFF000000);
		graphics.fill(hueThumb, this.hueRect.top(), hueThumb + 1, this.hueRect.bottom(), 0xFFFFFFFF);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}
