package dev.customname.util;

import dev.customname.config.NameConfig;
import dev.customname.engine.Identity;
import dev.customname.engine.LineRewriter;
import java.util.regex.Pattern;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/** Tab list / name tag facing wrapper around {@link LineRewriter}. */
public final class TabDisplayRewriter {
	public static final Pattern SKYBLOCK_LEVEL = LineRewriter.SKYBLOCK_LEVEL;

	private TabDisplayRewriter() {
	}

	public static void invalidateCache() {
		LineRewriter.invalidate();
	}

	public static boolean appliesTo(PlayerInfo entry, Component display) {
		if (entry == null || !Identity.active()) {
			return false;
		}

		if (entry.getProfile() != null && Identity.isLocal(entry.getProfile().id())) {
			return true;
		}

		String username = Identity.username();
		if (username == null) {
			return false;
		}

		if (entry.getProfile() != null && LineRewriter.containsLocalName(entry.getProfile().name(), username)) {
			return true;
		}

		return display != null && LineRewriter.containsLocalName(display.getString(), username);
	}

	public static Component rewrite(Component original) {
		return rewrite(original, NameConfig.get());
	}

	/** Tab-list entry point: hides the rank entirely unless showRankInTab is on. */
	public static Component rewriteForTab(Component original) {
		NameConfig config = NameConfig.get();
		return LineRewriter.rewrite(original, Identity.username(), config, true, true, !config.showRankInTab);
	}

	public static Component rewrite(Component original, NameConfig config) {
		return LineRewriter.rewrite(original, Identity.username(), config, true, true);
	}

	/** Applies only the Hypixel rank/level spoof, leaving the real username intact. */
	public static Component rewriteSpoofOnly(Component original) {
		return LineRewriter.rewrite(original, Identity.username(), NameConfig.get(), false, true);
	}

	/** Preview path for the config screen: uses an explicit username and draft config. */
	public static Component rewriteForPreview(Component original, NameConfig config, String username) {
		return LineRewriter.rewrite(original, username, config, true, true);
	}

	public static String formatLevel(String raw) {
		return LineRewriter.formatLevel(raw);
	}

	public static boolean inSkyblock() {
		return LineRewriter.inSkyblock();
	}
}
