package dev.customname.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/** Bold section title with a thin accent underline — distinct from muted field labels. */
public class SectionHeader extends AbstractWidget {
	private static final int TITLE = 0xFFE8E2D6;
	private static final int LINE = 0x66E8E2D6;
	private final Font font;

	public SectionHeader(int x, int y, int width, String title) {
		super(x, y, width, 14, Component.literal(title).withStyle(Style.EMPTY.withBold(true)));
		this.font = Minecraft.getInstance().font;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.text(this.font, this.getMessage(), this.getX(), this.getY() + 1, TITLE, false);
		int lineY = this.getY() + 11;
		graphics.fill(this.getX(), lineY, this.getX() + Math.min(this.getWidth(), 72), lineY + 1, LINE);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		return false;
	}
}
