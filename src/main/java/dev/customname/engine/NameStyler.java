package dev.customname.engine;

import dev.customname.config.NameConfig;
import dev.customname.util.ColorCodes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/** Builds the styled prefix and name components from the user's config. */
public final class NameStyler {
	private NameStyler() {
	}

	public static MutableComponent prefix(NameConfig config, boolean animated) {
		if (config.prefix == null || config.prefix.isBlank()) {
			return Component.empty();
		}

		MutableComponent coloured = config.prefixChroma
			? Chroma.gradient(config.prefix, config.prefixChromaStart, config.prefixChromaEnd, animated)
			: ColorCodes.parse(config.prefix);

		return applyFormats(
			coloured,
			config.prefixBold,
			config.prefixItalic,
			config.prefixUnderline,
			config.prefixStrikethrough,
			config.prefixObfuscated
		);
	}

	public static MutableComponent name(String realUsername, NameConfig config, boolean animated) {
		String fallback = realUsername == null ? "" : realUsername;
		if (!config.enabled) {
			return Component.literal(fallback);
		}

		String body = config.name != null && !config.name.isBlank() ? config.name : fallback;
		if (body.isEmpty()) {
			return Component.empty();
		}

		MutableComponent coloured;
		if (config.nameMatchesRankColor && config.prefix != null && !config.prefix.isBlank()) {
			// Name color follows the rank: same gradient for a chroma prefix,
			// otherwise the prefix's dominant color.
			coloured = config.prefixChroma
				? Chroma.gradient(ColorCodes.strip(body), config.prefixChromaStart, config.prefixChromaEnd, animated)
				: nameInRankColor(body, config);
		} else if (config.nameChroma) {
			coloured = Chroma.gradient(body, config.nameChromaStart, config.nameChromaEnd, animated);
		} else {
			coloured = ColorCodes.coloredLiteral(body, config.nameColor);
		}

		return applyFormats(
			coloured,
			config.nameBold,
			config.nameItalic,
			config.nameUnderline,
			config.nameStrikethrough,
			config.nameObfuscated
		);
	}

	/** Prefix + space + name, as used when replacing a bare username outright. */
	public static MutableComponent full(String realUsername, NameConfig config, boolean animated) {
		if (!config.enabled) {
			return Component.literal(realUsername == null ? "" : realUsername);
		}

		MutableComponent out = Component.empty();
		MutableComponent prefix = prefix(config, animated);
		if (!prefix.getString().isEmpty()) {
			out.append(prefix);
			if (!prefix.getString().endsWith(" ")) {
				out.append(Component.literal(" "));
			}
		}

		out.append(name(realUsername, config, animated));
		return out;
	}

	/**
	 * Re-emits the component with the requested formatting flags forced on,
	 * preserving whatever colours the source already carried.
	 */
	static MutableComponent applyFormats(
		Component source,
		boolean bold,
		boolean italic,
		boolean underline,
		boolean strikethrough,
		boolean obfuscated
	) {
		if (!bold && !italic && !underline && !strikethrough && !obfuscated) {
			return source instanceof MutableComponent mutable ? mutable : source.copy();
		}

		MutableComponent out = Component.empty();
		source.visit((style, string) -> {
			if (string != null && !string.isEmpty()) {
				Style next = style == null ? Style.EMPTY : style;
				if (bold) {
					next = next.withBold(true);
				}

				if (italic) {
					next = next.withItalic(true);
				}

				if (underline) {
					next = next.withUnderlined(true);
				}

				if (strikethrough) {
					next = next.withStrikethrough(true);
				}

				if (obfuscated) {
					next = next.withObfuscated(true);
				}

				out.append(Component.literal(string).withStyle(next));
			}

			return Optional.empty();
		}, Style.EMPTY);
		return out;
	}

	/** Single-color name using the prefix's dominant color; falls back to the configured name color. */
	private static MutableComponent nameInRankColor(String body, NameConfig config) {
		TextColor rank = dominantPrefixColor(config);
		if (rank == null) {
			return ColorCodes.coloredLiteral(body, config.nameColor);
		}

		return Component.literal(ColorCodes.strip(body)).withStyle(Style.EMPTY.withColor(rank));
	}

	/**
	 * The rank's letter color: the most frequent color among the prefix's
	 * alphanumeric glyphs (earliest wins ties). Brackets, punctuation like
	 * {@code ++}, and spaces are ignored — so {@code &b[&6MVP&c++&b]} yields the
	 * gold of the {@code MVP} letters, not the aqua brackets.
	 */
	private static TextColor dominantPrefixColor(NameConfig config) {
		TextColor best = null;
		int bestCount = 0;
		Map<TextColor, int[]> counts = new LinkedHashMap<>();
		for (Segments.Glyph glyph : Segments.flatten(prefix(config, false))) {
			String text = glyph.text();
			if (text == null || text.isEmpty() || !Character.isLetterOrDigit(text.charAt(0))) {
				continue;
			}

			TextColor color = glyph.style().getColor();
			if (color == null) {
				continue;
			}

			int[] tally = counts.computeIfAbsent(color, key -> new int[]{0, counts.size()});
			tally[0]++;
			if (tally[0] > bestCount || tally[0] == bestCount && counts.get(best) != null && tally[1] < counts.get(best)[1]) {
				best = color;
				bestCount = tally[0];
			}
		}

		return best;
	}
}
