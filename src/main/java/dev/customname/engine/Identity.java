package dev.customname.engine;

import dev.customname.config.NameConfig;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Single source of truth for "who is the local player and may we rewrite them".
 * All name matching goes through the cached patterns here so chat, tab and
 * name tags always agree on what counts as the local player's name.
 */
public final class Identity {
	private static String cacheKey = "";
	private static Pattern wordPattern;
	private static Pattern chatPattern;

	private Identity() {
	}

	public static void invalidate() {
		cacheKey = "";
		wordPattern = null;
		chatPattern = null;
	}

	/** The real Mojang username of the local player, or null if not in a world. */
	public static String username() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return null;
		}

		String name = player.getGameProfile().name();
		return name == null || name.isBlank() ? null : name;
	}

	public static boolean isLocal(UUID uuid) {
		LocalPlayer player = Minecraft.getInstance().player;
		return player != null && uuid != null && player.getUUID().equals(uuid);
	}

	/** True when any rewrite (custom display or Hypixel spoof) is currently requested. */
	public static boolean active() {
		return active(NameConfig.get());
	}

	public static boolean active(NameConfig config) {
		return Minecraft.getInstance().player != null && (config.hasCustomDisplay() || config.hasHypixelSpoof());
	}

	/** Matches the local player's name as a standalone word. */
	public static Pattern wordPattern(String username) {
		refresh(username);
		return wordPattern;
	}

	/** Matches the local player's name only where it acts as a chat sender ("Name:"). */
	public static Pattern chatPattern(String username) {
		refresh(username);
		return chatPattern;
	}

	private static void refresh(String username) {
		NameConfig config = NameConfig.get();
		String custom = config.name == null ? "" : config.name;
		String key = username + "\u0000" + custom;
		if (key.equals(cacheKey) && wordPattern != null && chatPattern != null) {
			return;
		}

		String alternation = alternation(username, custom);
		cacheKey = key;
		wordPattern = Pattern.compile("(?<![A-Za-z0-9_])(?:" + alternation + ")(?![A-Za-z0-9_])");
		chatPattern = Pattern.compile("(?<![A-Za-z0-9_])(?:" + alternation + ")(?=:[\\s\\u00A0]?)");
	}

	private static String alternation(String username, String custom) {
		Set<String> names = new LinkedHashSet<>();
		if (username != null && !username.isBlank()) {
			names.add(username);
		}

		if (custom != null && !custom.isBlank()) {
			names.add(custom);
		}

		StringBuilder out = new StringBuilder();
		for (String name : names) {
			if (out.length() > 0) {
				out.append('|');
			}

			out.append(token(name));
		}

		return out.length() == 0 ? "(?!)" : out.toString();
	}

	/** Treats '.' and '_' as interchangeable, since Hypixel renders some names with dots. */
	private static String token(String name) {
		StringBuilder token = new StringBuilder();
		int i = 0;

		while (i < name.length()) {
			int cp = name.codePointAt(i);
			if (cp == '.' || cp == '_') {
				token.append("[._]");
			} else {
				token.append(Pattern.quote(new String(Character.toChars(cp))));
			}

			i += Character.charCount(cp);
		}

		return token.toString();
	}
}
