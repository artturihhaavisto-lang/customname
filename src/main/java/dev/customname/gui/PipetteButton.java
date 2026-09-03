package dev.customname.gui;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Glass button with a pipette glyph and live color swatch. */
public class PipetteButton extends AbstractWidget {
	private final Runnable onPress;
	private final BooleanSupplier selected;
	private final IntSupplier color;

	public PipetteButton(int x, int y, IntSupplier color, Runnable onPress) {
		this(x, y, 16, 16, color, () -> false, onPress);
	}

	public PipetteButton(int x, int y, int w, int h, IntSupplier color, BooleanSupplier selected, Runnable onPress) {
		super(x, y, w, h, Component.literal("pick color"));
		this.color = color;
		this.selected = selected;
		this.onPress = onPress;
		this.setTooltip(Tooltip.create(Component.literal("Open color picker")));
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		this.playDownSound(Minecraft.getInstance().getSoundManager());
		this.onPress.run();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		boolean on = this.selected.getAsBoolean();
		int bg = on ? 872415231 : (this.isHoveredOrFocused() ? 721420287 : 352321535);
		graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), bg);

		int rgb = this.color.getAsInt() & 0xFFFFFF;
		int swatch = 0xFF000000 | rgb;
		int sx = this.getX() + 2;
		int sy = this.getY() + 2;
		graphics.fill(sx, sy, sx + 5, sy + 5, 0xFF000000);
		graphics.fill(sx + 1, sy + 1, sx + 4, sy + 4, swatch);

		// Simple pipette / eyedropper glyph.
		int ink = this.isHoveredOrFocused() ? 0xFFF2EEE6 : 0xFFB8B2A8;
		int px = this.getX() + 7;
		int py = this.getY() + 3;
		graphics.fill(px + 4, py, px + 6, py + 2, ink);
		graphics.fill(px + 3, py + 1, px + 5, py + 3, ink);
		graphics.fill(px + 2, py + 2, px + 4, py + 4, ink);
		graphics.fill(px + 1, py + 3, px + 3, py + 5, ink);
		graphics.fill(px, py + 4, px + 2, py + 6, ink);
		graphics.fill(px - 1, py + 6, px + 1, py + 9, ink);
		graphics.fill(px - 1, py + 9, px + 2, py + 10, 0xFF000000 | rgb);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		this.defaultButtonNarrationText(output);
	}
}
