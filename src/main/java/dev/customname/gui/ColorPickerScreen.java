package dev.customname.gui;

import dev.customname.util.ColorCodes;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/** Glass-panel color chooser opened from pipette buttons. */
public class ColorPickerScreen extends Screen {
	private static final int PANEL_W = 280;
	private static final int PANEL_H = 248;

	private final Screen parent;
	private final Consumer<String> onPick;
	private int panelLeft;
	private int panelTop;
	private int rgb;
	private ColorPickerWidget picker;
	private EditBox hexBox;
	private boolean syncing;

	public ColorPickerScreen(Screen parent, String currentHex, Consumer<String> onPick) {
		super(Component.literal("color"));
		this.parent = parent;
		this.onPick = onPick;
		this.rgb = parseRgb(currentHex);
	}

	@Override
	protected void init() {
		this.panelLeft = (this.width - PANEL_W) / 2;
		this.panelTop = (this.height - PANEL_H) / 2;
		int x = this.panelLeft + 16;
		int y = this.panelTop + 10;
		int inner = PANEL_W - 32;

		this.addRenderableWidget(new StringWidget(x, y + 2, 80, 10, whisper("color"), this.font));
		this.addRenderableWidget(new GlassButton(x + inner - 108, y, 48, 16, Component.literal("back"), () -> false, this::onClose));
		this.addRenderableWidget(new GlassButton(x + inner - 52, y, 52, 16, Component.literal("apply"), () -> false, this::applyAndClose));

		int body = this.panelTop + 36;
		this.picker = new ColorPickerWidget(x, body, inner, 120);
		this.picker.setRgb(this.rgb);
		this.picker.setOnChange(this::setRgbFromPicker);
		this.addRenderableWidget(this.picker);

		int row = body + 128;
		this.addRenderableWidget(new StringWidget(x, row + 3, 28, 10, whisper("hex"), this.font));
		this.hexBox = new EditBox(this.font, x + 32, row, 86, 16, Component.literal("#hex"));
		this.hexBox.setMaxLength(7);
		this.hexBox.setBordered(false);
		this.hexBox.setTextShadow(false);
		this.hexBox.setTextColor(-723208);
		this.hexBox.setValue(toHex(this.rgb));
		this.hexBox.setHint(whisper("#RRGGBB"));
		this.hexBox.setResponder(this::onHexTyped);
		this.addRenderableWidget(this.hexBox);

		this.addRenderableWidget(new StringWidget(x, row + 24, inner, 10, whisper("presets"), this.font));
		char[] codes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		int presetY = row + 38;
		for (int i = 0; i < codes.length; i++) {
			final char code = codes[i];
			int rgb = ColorCodes.legacyRgb(code);
			int bx = x + (i % 8) * 30;
			int by = presetY + (i / 8) * 18;
			this.addRenderableWidget(new GlassButton(bx, by, 26, 16, Component.literal("&" + code).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb == 0 ? 0x888888 : rgb))), () -> false, () -> {
				this.setRgb(rgb);
			}, true));
		}
	}

	private void setRgbFromPicker(int rgb) {
		this.setRgb(rgb);
	}

	private void setRgb(int rgb) {
		this.rgb = rgb & 0xFFFFFF;
		this.syncing = true;
		if (this.picker != null) {
			this.picker.setRgb(this.rgb);
		}
		if (this.hexBox != null) {
			this.hexBox.setValue(toHex(this.rgb));
		}
		this.syncing = false;
	}

	private void onHexTyped(String text) {
		if (this.syncing) {
			return;
		}
		String normalized = ColorCodes.normalizeColorCode(text);
		if (normalized.startsWith("&#") && normalized.length() >= 8) {
			try {
				this.rgb = Integer.parseInt(normalized.substring(2, 8), 16);
				if (this.picker != null) {
					this.syncing = true;
					this.picker.setRgb(this.rgb);
					this.syncing = false;
				}
			} catch (NumberFormatException ignored) {
			}
		}
	}

	private void applyAndClose() {
		this.onPick.accept(toHex(this.rgb));
		this.minecraft.setScreenAndShow(this.parent);
		if (this.parent instanceof AppearanceScreen appearance) {
			appearance.afterColorPicked();
		}
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

		// Hex field well + live preview swatch.
		int wellX = this.panelLeft + 16 + 30;
		int wellY = this.panelTop + 36 + 128;
		graphics.fill(wellX, wellY, wellX + 90, wellY + 16, 570425344);
		int previewX = this.panelLeft + PANEL_W - 16 - 28;
		graphics.fill(previewX - 1, wellY - 1, previewX + 25, wellY + 17, 0x55FFFFFF);
		graphics.fill(previewX, wellY, previewX + 24, wellY + 16, 0xFF000000 | this.rgb);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static Component whisper(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(9345192)));
	}

	private static String toHex(int rgb) {
		return String.format("#%06X", rgb & 0xFFFFFF);
	}

	private static int parseRgb(String raw) {
		String normalized = ColorCodes.normalizeColorCode(raw);
		if (normalized.startsWith("&#") && normalized.length() >= 8) {
			try {
				return Integer.parseInt(normalized.substring(2, 8), 16);
			} catch (NumberFormatException ignored) {
			}
		}
		return 0xFFFFFF;
	}
}
