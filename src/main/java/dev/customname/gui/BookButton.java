package dev.customname.gui;

import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Glass chrome button that shows a book item icon. */
public class BookButton extends AbstractWidget {
	private static final ItemStack BOOK = new ItemStack(Items.BOOK);
	private final Runnable onPress;
	private final BooleanSupplier selected;

	public BookButton(int x, int y, int w, int h, BooleanSupplier selected, Runnable onPress) {
		super(x, y, w, h, Component.literal("book"));
		this.selected = selected;
		this.onPress = onPress;
		this.setTooltip(Tooltip.create(Component.literal("Formatting codes — colors, styles, hex")));
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
		if (on) {
			graphics.fill(
				this.getX() + 4,
				this.getY() + this.getHeight() - 1,
				this.getX() + this.getWidth() - 4,
				this.getY() + this.getHeight(),
				-655100166
			);
		}
		int ix = this.getX() + (this.getWidth() - 16) / 2;
		int iy = this.getY() + (this.getHeight() - 16) / 2;
		graphics.item(BOOK, ix, iy);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		this.defaultButtonNarrationText(output);
	}
}
