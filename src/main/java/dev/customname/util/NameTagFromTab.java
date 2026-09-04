package dev.customname.util;

import dev.customname.config.NameConfig;
import dev.customname.engine.Identity;
import dev.customname.engine.LineRewriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NameTagFromTab {
	private static final Logger LOG = LoggerFactory.getLogger("customname");
	private static long lastDebugLog;

	private NameTagFromTab() {
	}

	/** Throttled pipeline logging (max one line per 10s) for nametag diagnosis. */
	public static void debugNametag(String label, Object... parts) {
		long now = System.currentTimeMillis();
		if (now - lastDebugLog < 10_000L) {
			return;
		}

		lastDebugLog = now;
		StringBuilder sb = new StringBuilder("nametag ").append(label);
		for (int i = 0; i + 1 < parts.length; i += 2) {
			sb.append(' ').append(parts[i]).append("=[").append(parts[i + 1]).append(']');
		}

		LOG.info("[CustomName/debug] {}", sb);
	}

	public static void invalidateCache() {
	}

	public static Component resolve(AbstractClientPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		ClientPacketListener connection = mc.getConnection();
		if (connection == null) {
			return null;
		}

		boolean self = mc.isLocalPlayer(player.getUUID());
		PlayerInfo info = connection.getPlayerInfo(player.getUUID());
		Component raw = info == null ? null : info.getTabListDisplayName();
		if (raw != null && raw.getString().isBlank()) {
			raw = null;
		}

		if (!self) {
			return raw;
		}

		// The own name tag is the sender header floating over your head: run
		// it through the same chat rewrite (emblem stripped, level spoofed on
		// SkyBlock only, rank replaced, custom name applied).
		String username = Identity.username();
		NameConfig config = NameConfig.get();
		Component base = raw;
		// Emblems are a SkyBlock cosmetic; only synthesise a header for one when
		// actually in SkyBlock (otherwise the own name tag would be rebuilt even
		// though the emblem is never shown outside SkyBlock).
		boolean emblemSpoof = config.hasEmblemSpoof() && LineRewriter.inSkyblock();
		if (base == null && username != null
			&& (config.hasCustomDisplay() || config.hasRankSpoof() || config.hasLevelSpoof() || emblemSpoof)) {
			// 1.3.4/1.3.5 field logs showed the server never sends a tab-list
			// display name for the player's OWN entry (fallback fired for whole
			// sessions, the self branch never ran), so building the own tag
			// from tab data can never succeed on Hypixel. Synthesize the bare
			// sender header instead: rewriteChat inserts the level tag (only
			// on SkyBlock) and the prefix exactly as it does for chat lines,
			// yielding "[level] [prefix] name" over your own head.
			base = Component.literal(username);
		}

		if (base == null) {
			return null;
		}

		Component rewritten = username == null
			? null
			: LineRewriter.rewriteChat(base, username, config, true);
		Component out = rewritten != null ? rewritten : base;
		debugNametag("self",
			"src", raw == null ? "synth" : "tab",
			"raw", LineRewriter.escapeForLog(base.getString()),
			"out", LineRewriter.escapeForLog(out.getString()));
		return out;
	}

	public static boolean isHidden(AbstractClientPlayer player) {
		NameConfig config = NameConfig.get();
		if (config.hidePlayerNameTags) {
			return true;
		}

		return Minecraft.getInstance().isLocalPlayer(player.getUUID()) ? config.hideOwnNameTag : config.hideOtherNameTags;
	}
}
