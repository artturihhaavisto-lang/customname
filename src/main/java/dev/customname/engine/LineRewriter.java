package dev.customname.engine;

import dev.customname.config.NameConfig;
import dev.customname.util.ColorCodes;
import dev.customname.util.SkyblockLevels;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

/**
 * The one rewrite algorithm, shared by chat, the tab list and name tags.
 *
 * <p>Order matters: the username is swapped first, then the Skyblock level tag,
 * then the donor rank. Each step re-reads the plain text of the component it is
 * given, so offsets are always consistent with the text being edited.
 */
public final class LineRewriter {
	public static final Pattern SKYBLOCK_LEVEL = Pattern.compile("\\[\\d{1,4}]");
	private static final Pattern DONOR_RANK = Pattern.compile(
		"\\[(?:VIP\\+|VIP|MVP\\+\\+|MVP\\+|MVP|YOUTUBE|ADMIN|GM|MOD|HELPER|OWNER|PIG\\+\\+\\+|PIG\\+\\+|PIG\\+|PIG)\\]",
		Pattern.CASE_INSENSITIVE
	);

	private static int skyblockTick = Integer.MIN_VALUE;
	private static boolean skyblockCached;

	private LineRewriter() {
	}

	public static void invalidate() {
		skyblockTick = Integer.MIN_VALUE;
		Identity.invalidate();
	}

	/**
	 * @param applyCustomName when false only the Hypixel spoof parts are applied,
	 *     used for surfaces that must keep the real username visible.
	 * @param animated when true chroma colours are sampled from the current clock.
	 */
	public static Component rewrite(Component original, String username, NameConfig config, boolean applyCustomName, boolean animated) {
		if (username == null || username.isBlank()) {
			return original;
		}

		boolean custom = applyCustomName && config.hasCustomDisplay();
		boolean rank = config.hasRankSpoof();
		boolean level = config.hasLevelSpoof();
		if (!custom && !rank && !level) {
			return original;
		}

		if (original == null) {
			return custom ? NameStyler.full(username, config, animated) : null;
		}

		if (custom && !rank && !level && isBareName(original, username, config)) {
			return NameStyler.full(username, config, animated);
		}

		Component result = original;
		if (custom) {
			result = Segments.replaceAll(result, Identity.wordPattern(username), () -> NameStyler.name(username, config, animated));
		}

		if (level) {
			result = replaceLevel(result, config);
		}

		if (rank) {
			result = replaceRank(result, username, config, animated);
		}

		return result;
	}

	public static Component replaceLevel(Component original, NameConfig config) {
		if (original == null || !config.hasLevelSpoof()) {
			return original;
		}

		String text = formatLevel(config.spoofSkyblockLevelValue);
		if (text == null) {
			return original;
		}

		int level = Integer.parseInt(text);
		return Segments.replaceAll(original, SKYBLOCK_LEVEL, () -> SkyblockLevels.buildLevelTag(level));
	}

	/**
	 * Swaps the Hypixel donor rank that sits in front of the local player's name.
	 * When the line has no rank tag the prefix is inserted immediately before the name.
	 */
	public static Component replaceRank(Component original, String username, NameConfig config, boolean animated) {
		if (original == null || !config.hasRankSpoof()) {
			return original;
		}

		MutableComponent prefix = NameStyler.prefix(config, animated);
		if (prefix.getString().isEmpty()) {
			return original;
		}

		String plain = original.getString();
		int[] name = findName(plain, username, config);
		if (name == null) {
			return original;
		}

		int[] rank = findRankBefore(plain, name[0]);

		// With no rank tag to swap we can only insert, and inserting is only correct
		// when our name is the sender of the line. Otherwise "Dinnerbone: hi Notch"
		// would gain a prefix in the middle of someone else's sentence.
		if (rank == null && !isSenderPosition(plain, name[0])) {
			return original;
		}

		int start = rank == null ? name[0] : rank[0];
		int end = rank == null ? name[0] : rank[1];

		// Only add a separator when the text does not already provide one,
		// otherwise replacing "[MVP+] " style tags leaves a double space.
		MutableComponent replacement = Component.empty().append(prefix);
		boolean spaceFollows = end < plain.length() && Character.isWhitespace(plain.charAt(end));
		if (!spaceFollows && !prefix.getString().endsWith(" ")) {
			replacement.append(Component.literal(" "));
		}

		return Segments.replaceRange(original, start, end, replacement);
	}

	/**
	 * Locates the local player's name. A chat-sender match ("Name:") wins, then the
	 * first match in the header before ':', then the last match anywhere.
	 */
	static int[] findName(String plain, String username, NameConfig config) {
		if (plain == null || plain.isBlank()) {
			return null;
		}

		Matcher chat = Identity.chatPattern(username).matcher(plain);
		if (chat.find()) {
			return new int[]{chat.start(), chat.end()};
		}

		int colon = plain.indexOf(':');
		if (colon > 0) {
			Matcher header = Identity.wordPattern(username).matcher(plain.substring(0, colon));
			if (header.find()) {
				return new int[]{header.start(), header.end()};
			}
		}

		Matcher any = Identity.wordPattern(username).matcher(plain);
		int start = -1;
		int end = -1;
		while (any.find()) {
			start = any.start();
			end = any.end();
		}

		return start < 0 ? null : new int[]{start, end};
	}

	/**
	 * The last donor rank tag that belongs to the local player: it must end before the
	 * name and be separated from it only by whitespace. Without the adjacency check a
	 * line like "[MVP++] Dinnerbone: hi Notch" would steal the other player's rank.
	 */
	private static int[] findRankBefore(String plain, int nameStart) {
		Matcher matcher = DONOR_RANK.matcher(plain);
		int start = -1;
		int end = -1;

		while (matcher.find()) {
			if (matcher.end() <= nameStart && onlyWhitespaceBetween(plain, matcher.end(), nameStart)) {
				start = matcher.start();
				end = matcher.end();
			}
		}

		return start < 0 ? null : new int[]{start, end};
	}

	private static boolean onlyWhitespaceBetween(String text, int from, int to) {
		for (int i = from; i < to; i++) {
			if (!Character.isWhitespace(text.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * True when the offset sits in the sender header rather than the message body.
	 * Tab rows and name tags have no ':' at all, so they always count as the header.
	 */
	private static boolean isSenderPosition(String plain, int start) {
		int colon = plain.indexOf(':');
		return colon < 0 || start < colon;
	}

	static boolean isBareName(Component original, String username, NameConfig config) {
		if (original == null) {
			return true;
		}

		String plain = original.getString().trim();
		return plain.equals(username) || config.name != null && !config.name.isBlank() && plain.equals(config.name);
	}

	public static boolean containsLocalName(String text, String username) {
		return text != null && !text.isBlank() && Identity.wordPattern(username).matcher(text).find();
	}

	/** Normalises the user's level input to a plain 1-4 digit number, or null. */
	public static String formatLevel(String raw) {
		if (raw == null) {
			return null;
		}

		String digits = raw.trim().replaceAll("[^0-9]", "");
		if (digits.isEmpty() || digits.length() > 4) {
			return null;
		}

		try {
			return Integer.toString(Integer.parseInt(digits));
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	public static boolean inSkyblock() {
		Minecraft mc = Minecraft.getInstance();
		int tick = mc.player != null ? mc.player.tickCount : Integer.MIN_VALUE;
		if (tick == skyblockTick) {
			return skyblockCached;
		}

		skyblockTick = tick;
		skyblockCached = detectSkyblock(mc);
		return skyblockCached;
	}

	private static boolean detectSkyblock(Minecraft mc) {
		ClientLevel level = mc.level;
		if (level == null) {
			return false;
		}

		Objective sidebar = level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebar == null) {
			return false;
		}

		String stripped = ColorCodes.strip(sidebar.getDisplayName().getString()).toUpperCase(Locale.ROOT);
		return stripped.contains("SKYBLOCK") || stripped.replaceAll("[^A-Z]", "").contains("SKYBLOCK");
	}

	/**
	 * Chat-facing rewrite: the same three steps as {@link #rewrite}, tuned for chat
	 * lines. Name mentions are swapped without dragging the prefix into the middle
	 * of someone else's sentence, the SkyBlock level tag is only spoofed in the
	 * sender header, and the donor rank is swapped or inserted in the header only,
	 * exactly like the tab list.
	 */
	public static Component rewriteChat(Component original, String username, NameConfig config) {
		if (original == null || username == null || username.isBlank()) {
			return original;
		}

		boolean custom = config.hasCustomDisplay();
		boolean rank = config.hasRankSpoof();
		boolean level = config.hasLevelSpoof();
		if (!custom && !rank && !level) {
			return original;
		}

		Component result = original;
		if (custom) {
			result = Segments.replaceAll(result, Identity.wordPattern(username), () -> NameStyler.name(username, config, false));
		}

		if (level) {
			result = replaceChatLevel(result, config, username);
		}

		if (rank) {
			result = replaceRank(result, username, config, false);
		}

		return result;
	}

	/**
	 * Level spoof for chat: only the {@code [N]} tag that belongs to the sender
	 * header — sitting directly before the rank or the name — is swapped, so
	 * numbers in brackets inside the message body are left alone.
	 */
	private static Component replaceChatLevel(Component original, NameConfig config, String username) {
		if (original == null) {
			return original;
		}

		String text = formatLevel(config.spoofSkyblockLevelValue);
		if (text == null) {
			return original;
		}

		String plain = original.getString();
		int[] name = findName(plain, username, config);
		if (name == null) {
			return original;
		}

		// The header ends where the rank tag begins when one is present, so a
		// spoofed level never replaces a bracketed number further back in the line.
		int headerEnd = name[0];
		int[] rank = findRankBefore(plain, name[0]);
		if (rank != null) {
			headerEnd = rank[0];
		}

		Matcher matcher = SKYBLOCK_LEVEL.matcher(plain);
		int start = -1;
		int end = -1;
		while (matcher.find()) {
			if (matcher.end() <= headerEnd && noLettersOrDigitsBetween(plain, matcher.end(), headerEnd)) {
				start = matcher.start();
				end = matcher.end();
			}
		}

		if (start < 0) {
			return original;
		}

		return Segments.replaceRange(original, start, end, SkyblockLevels.buildLevelTag(Integer.parseInt(text)));
	}

	/**
	 * Decorative glyphs (SkyBlock puts a {@code ♦} between the level tag and the
	 * rank) may separate the level tag from the header, but real words may not.
	 */
	private static boolean noLettersOrDigitsBetween(String text, int from, int to) {
		for (int i = from; i < to; i++) {
			if (Character.isLetterOrDigit(text.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Simple chat name replacement: replaces every occurrence of the username
	 * (and custom name if configured) with the full custom display name.
	 */
	public static Component replaceChatName(Component original, String username, NameConfig config) {
		if (original == null || username == null || username.isBlank()) {
			return original;
		}

		if (!config.hasCustomDisplay()) {
			return original;
		}

		MutableComponent replacement = NameStyler.full(username, config, false);
		if (replacement.getString().isEmpty()) {
			return original;
		}

		return Segments.replaceAll(original, Identity.wordPattern(username), () -> replacement);
	}
}
