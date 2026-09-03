package dev.customname.gui;

import dev.customname.util.ColorCodes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Formatting reference using the same glass panel chrome as {@link AppearanceScreen}.
 */
public class FormatGuideScreen extends Screen {
	private static final int PANEL_W = 520;
	private static final int PANEL_H = 328;

	private final Screen parent;
	private int panelLeft;
	private int panelTop;

	public FormatGuideScreen(Screen parent) {
		super(Component.literal("guide"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.panelLeft = (this.width - PANEL_W) / 2;
		this.panelTop = (this.height - PANEL_H) / 2;
		int x = this.panelLeft + 16;
		int y = this.panelTop + 10;
		int inner = PANEL_W - 32;

		this.addRenderableWidget(new StringWidget(x, y + 2, 70, 10, whisper("guide"), this.font));
		this.addRenderableWidget(
			new GlassButton(x + inner - 52, y, 52, 16, Component.literal("back"), () -> false, this::onClose)
		);

		int bodyTop = this.panelTop + 36;
		int colW = (inner - 12) / 2;
		int leftX = x;
		int rightX = x + colW + 12;

		this.addRenderableWidget(new StringWidget(leftX, bodyTop, colW, 10, whisper("how"), this.font));
		this.addLine(leftX, bodyTop + 14, colW, "Type & codes in name / prefix.");
		this.addLine(leftX, bodyTop + 26, colW, "Example: &cRed &lBold");
		this.addPreview(leftX, bodyTop + 38, colW, "&cRed &lBold");
		this.addLine(leftX, bodyTop + 54, colW, "Hex: &#FF55AA or #FF55AA");
		this.addPreview(leftX, bodyTop + 66, colW, "&#FF55AAHex");

		this.addRenderableWidget(new StringWidget(leftX, bodyTop + 88, colW, 10, whisper("colors"), this.font));
		char[] codes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		String[] names = {
			"black", "dark blue", "dark green", "dark aqua",
			"dark red", "dark purple", "gold", "gray",
			"dark gray", "blue", "green", "aqua",
			"red", "light purple", "yellow", "white"
		};
		for (int i = 0; i < codes.length; i++) {
			int row = i / 2;
			int col = i % 2;
			int cx = leftX + col * (colW / 2);
			int cy = bodyTop + 102 + row * 12;
			this.addRenderableWidget(new StringWidget(cx, cy, colW / 2 - 4, 10, colorLabel(codes[i], names[i]), this.font));
		}

		this.addRenderableWidget(new StringWidget(rightX, bodyTop, colW, 10, whisper("styles"), this.font));
		this.addRenderableWidget(new StringWidget(rightX, bodyTop + 14, colW, 10, styleLabel('k', "obfuscated", Style.EMPTY.withObfuscated(true)), this.font));
		this.addRenderableWidget(new StringWidget(rightX, bodyTop + 26, colW, 10, styleLabel('l', "bold", Style.EMPTY.withBold(true)), this.font));
		this.addRenderableWidget(new StringWidget(rightX, bodyTop + 38, colW, 10, styleLabel('m', "strikethrough", Style.EMPTY.withStrikethrough(true)), this.font));
		this.addRenderableWidget(new StringWidget(rightX, bodyTop + 50, colW, 10, styleLabel('n', "underline", Style.EMPTY.withUnderlined(true)), this.font));
		this.addRenderableWidget(new StringWidget(rightX, bodyTop + 62, colW, 10, styleLabel('o', "italic", Style.EMPTY.withItalic(true)), this.font));
		this.addLine(rightX, bodyTop + 78, colW, "&r resets color and styles");
		this.addLine(rightX, bodyTop + 90, colW, "Styles stack until color/&r");

		this.addRenderableWidget(new StringWidget(rightX, bodyTop + 112, colW, 10, whisper("examples"), this.font));
		this.addLine(rightX, bodyTop + 126, colW, "&c[&6MVP&c]");
		this.addPreview(rightX, bodyTop + 138, colW, "&c[&6MVP&c]");
		this.addLine(rightX, bodyTop + 154, colW, "&b&lVIP&r &7Player");
		this.addPreview(rightX, bodyTop + 166, colW, "&b&lVIP&r &7Player");
		this.addLine(rightX, bodyTop + 182, colW, "&#55FFFFOcean");
		this.addPreview(rightX, bodyTop + 194, colW, "&#55FFFFOcean");
		this.addLine(rightX, bodyTop + 214, colW, "Chroma uses the from/to");
		this.addLine(rightX, bodyTop + 226, colW, "hex fields, not & codes.");
	}

	private void addLine(int x, int y, int w, String text) {
		this.addRenderableWidget(new StringWidget(x, y, w, 10, Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC8C2B8))), this.font));
	}

	private void addPreview(int x, int y, int w, String coded) {
		this.addRenderableWidget(new StringWidget(x, y, w, 10, ColorCodes.parse(coded), this.font));
	}

	private static Component colorLabel(char code, String name) {
		int rgb = ColorCodes.legacyRgb(code);
		int swatch = rgb == 0 ? 0x888888 : rgb;
		return Component.literal("&" + code + " ")
			.append(Component.literal("\u2588 ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(swatch))))
			.append(Component.literal(name).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(swatch))));
	}

	private static Component styleLabel(char code, String name, Style style) {
		MutableComponent row = Component.literal("&" + code + " ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC8C2B8)));
		row.append(Component.literal(name).withStyle(style.withColor(TextColor.fromRgb(0xF0EDE6))));
		return row;
	}

	private static Component whisper(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(9345192)));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		if (this.minecraft.level != null) {
			this.extractBlurredBackground(graphics);
		} else {
			this.extractPanorama(graphics, partialTick);
			this.extractBlurredBackground(graphics);
		}

		graphics.fill(0, 0, this.width, this.height, 1711671824);
		graphics.fill(this.panelLeft - 1, this.panelTop - 1, this.panelLeft + PANEL_W + 1, this.panelTop + PANEL_H + 1, 872415231);
		graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_W, this.panelTop + PANEL_H, -1005579236);
		graphics.fillGradient(this.panelLeft, this.panelTop, this.panelLeft + PANEL_W, this.panelTop + 36, 587202559, 16777215);
		graphics.fill(this.panelLeft + 16, this.panelTop + 30, this.panelLeft + PANEL_W - 16, this.panelTop + 31, 587202559);

		int splitX = this.panelLeft + 16 + (PANEL_W - 32 - 12) / 2 + 4;
		graphics.fill(splitX, this.panelTop + 40, splitX + 1, this.panelTop + PANEL_H - 16, 587202559);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
