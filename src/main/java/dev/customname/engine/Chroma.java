package dev.customname.engine;

import dev.customname.util.ColorCodes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Gradient / chroma colouring.
 *
 * <p>Colours are computed at build time. Anything rebuilt every frame (tab list,
 * name tags) therefore animates on its own; chat lines are built once when the
 * message arrives and keep the gradient they were given.
 */
public final class Chroma {
	private static final long CYCLE_MILLIS = 3000L;

	private Chroma() {
	}

	/** Phase in [0,1) driven by wall clock, so all animated text stays in sync. */
	public static double phase() {
		return System.currentTimeMillis() % CYCLE_MILLIS / (double)CYCLE_MILLIS;
	}

	public static MutableComponent gradient(String text, String startCode, String endCode, boolean animated) {
		String plain = ColorCodes.strip(text == null ? "" : text);
		if (plain.isEmpty()) {
			return Component.empty();
		}

		int start = rgbOf(startCode, 0xFF5555);
		int end = rgbOf(endCode, 0x55FFFF);
		double shift = animated ? phase() : 0.0;

		MutableComponent out = Component.empty();
		int[] points = plain.codePoints().toArray();
		int visible = 0;

		for (int point : points) {
			if (!Character.isWhitespace(point)) {
				visible++;
			}
		}

		int span = Math.max(1, visible - 1);
		int index = 0;

		for (int point : points) {
			String piece = new String(Character.toChars(point));
			if (Character.isWhitespace(point)) {
				out.append(Component.literal(piece));
				continue;
			}

			double base = span == 0 ? 0.0 : (double)index / span;
			int rgb = lerp(start, end, triangle(base + shift));
			out.append(Component.literal(piece).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
			index++;
		}

		return out;
	}

	/** Folds a linear ramp into a ping-pong ramp so animated gradients have no colour seam. */
	private static double triangle(double t) {
		double wrapped = t - Math.floor(t);
		return wrapped < 0.5 ? wrapped * 2.0 : (1.0 - wrapped) * 2.0;
	}

	private static int lerp(int from, int to, double t) {
		int fr = from >> 16 & 0xFF;
		int fg = from >> 8 & 0xFF;
		int fb = from & 0xFF;
		int tr = to >> 16 & 0xFF;
		int tg = to >> 8 & 0xFF;
		int tb = to & 0xFF;
		int r = (int)Math.round(fr + (tr - fr) * t);
		int g = (int)Math.round(fg + (tg - fg) * t);
		int b = (int)Math.round(fb + (tb - fb) * t);
		return r << 16 | g << 8 | b;
	}

	private static int rgbOf(String code, int fallback) {
		TextColor color = ColorCodes.resolveTextColor(code);
		return color == null ? fallback : color.getValue();
	}
}
