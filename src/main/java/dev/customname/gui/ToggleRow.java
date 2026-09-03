package dev.customname.gui;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Full-width row: clear label on the left, switch on the right, soft well so they read as one control.
 */
public class ToggleRow extends AbstractWidget {
	private static final int WELL = 0x22000000;
	private static final int WELL_HOVER = 0x33000000;
	private static final int LABEL = 0xFFC8C2B8;
	private static final int LABEL_HOVER = 0xFFF0EDE6;
	private final Font font;
	private boolean value;
	private final Consumer<Boolean> onChange;

	public ToggleRow(int x, int y, int w, String label, boolean value, Consumer<Boolean> onChange) {
		super(x, y, w, 16, Component.literal(label));
		this.font = Minecraft.getInstance().font;
		this.value = value;
		this.onChange = onChange;
	}

	public ToggleRow tooltip(String text) {
		this.setTooltip(Tooltip.create(Component.literal(text)));
		return this;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		this.playDownSound(Minecraft.getInstance().getSoundManager());
		this.value = !this.value;
		this.onChange.accept(this.value);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		boolean hover = this.isHoveredOrFocused();
		graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), hover ? WELL_HOVER : WELL);

		int text = hover ? LABEL_HOVER : LABEL;
		int maxLabelW = this.getWidth() - 28;
		String label = this.font.plainSubstrByWidth(this.getMessage().getString(), maxLabelW);
		graphics.text(this.font, label, this.getX() + 4, this.getY() + 4, text, false);

		int px = this.getX() + this.getWidth() - 24;
		int py = this.getY() + 3;
		graphics.fill(px, py, px + 20, py + 10, this.value ? 0x55A8C8FF : 0x33222222);
		int knob = this.value ? px + 11 : px + 2;
		graphics.fill(knob, py + 2, knob + 7, py + 8, this.value ? 0xFFF2EEE6 : 0xFF8A847A);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		this.defaultButtonNarrationText(output);
	}
}
