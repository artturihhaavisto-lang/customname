package dev.customname.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Styled text surgery. Every operation works on UTF-16 offsets taken from
 * {@link Component#getString()} so regex match ranges can be applied directly
 * to a styled component without losing the surrounding styles.
 */
public final class Segments {
	private Segments() {
	}

	/** One indivisible unit of styled text (a full code point, so surrogate pairs never split). */
	public record Glyph(String text, Style style) {
		int length() {
			return this.text.length();
		}
	}

	public static List<Glyph> flatten(Component text) {
		List<Glyph> glyphs = new ArrayList<>();
		if (text == null) {
			return glyphs;
		}

		text.visit((style, string) -> {
			if (string != null && !string.isEmpty()) {
				Style resolved = style == null ? Style.EMPTY : style;
				int i = 0;
				while (i < string.length()) {
					int cp = string.codePointAt(i);
					int len = Character.charCount(cp);
					glyphs.add(new Glyph(string.substring(i, i + len), resolved));
					i += len;
				}
			}

			return Optional.empty();
		}, Style.EMPTY);
		return glyphs;
	}

	public static MutableComponent join(List<Glyph> glyphs) {
		MutableComponent out = Component.empty();
		if (glyphs.isEmpty()) {
			return out;
		}

		StringBuilder run = new StringBuilder();
		Style runStyle = glyphs.get(0).style();

		for (Glyph glyph : glyphs) {
			if (glyph.style().equals(runStyle)) {
				run.append(glyph.text());
			} else {
				if (run.length() > 0) {
					out.append(Component.literal(run.toString()).withStyle(runStyle));
				}

				run.setLength(0);
				run.append(glyph.text());
				runStyle = glyph.style();
			}
		}

		if (run.length() > 0) {
			out.append(Component.literal(run.toString()).withStyle(runStyle));
		}

		return out;
	}

	/**
	 * Replaces the UTF-16 range {@code [start, end)} with {@code replacement}.
	 * A zero-length range performs an insertion at {@code start}.
	 */
	public static Component replaceRange(Component original, int start, int end, Component replacement) {
		if (original == null || start < 0 || end < start) {
			return original;
		}

		List<Glyph> before = new ArrayList<>();
		List<Glyph> after = new ArrayList<>();
		int offset = 0;

		for (Glyph glyph : flatten(original)) {
			int glyphStart = offset;
			int glyphEnd = offset + glyph.length();
			offset = glyphEnd;

			if (glyphEnd <= start) {
				before.add(glyph);
			} else if (glyphStart >= end) {
				after.add(glyph);
			}
		}

		MutableComponent out = Component.empty();
		if (!before.isEmpty()) {
			out.append(join(before));
		}

		if (replacement != null) {
			out.append(replacement);
		}

		if (!after.isEmpty()) {
			out.append(join(after));
		}

		return out;
	}

	public static Component replaceAll(Component original, Pattern pattern, Supplier<Component> replacement) {
		return replaceAll(original, pattern, matcher -> replacement.get());
	}

	/**
	 * Replaces every match of {@code pattern} in the component's plain text.
	 * Matches are applied right-to-left so earlier offsets stay valid.
	 */
	public static Component replaceAll(Component original, Pattern pattern, Function<Matcher, Component> replacement) {
		if (original == null) {
			return null;
		}

		String plain = original.getString();
		if (plain == null || plain.isEmpty()) {
			return original;
		}

		Matcher matcher = pattern.matcher(plain);
		List<int[]> ranges = new ArrayList<>();
		List<Component> pieces = new ArrayList<>();

		while (matcher.find()) {
			Component piece = replacement.apply(matcher);
			if (piece != null) {
				ranges.add(new int[]{matcher.start(), matcher.end()});
				pieces.add(piece);
			}
		}

		if (ranges.isEmpty()) {
			return original;
		}

		Component result = original;
		for (int i = ranges.size() - 1; i >= 0; i--) {
			int[] range = ranges.get(i);
			result = replaceRange(result, range[0], range[1], pieces.get(i));
		}

		return result;
	}
}
