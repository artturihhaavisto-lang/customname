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

		PlayerInfo info = connection.getPlayerInfo(player.getUUID());
		if (info == null) {
			return null;
		}

		Component raw = info.getTabListDisplayName();
		if (raw == null || raw.getString().isBlank()) {
			return null;
		}

		if (mc.isLocalPlayer(player.getUUID())) {
			// The own name tag is the sender header floating over your head: run
			// it through the same chat rewrite (emblem stripped, level spoofed on
			// SkyBlock only, rank replaced, custom name applied).
			String username = Identity.username();
			Component rewritten = username == null
				? null
				: LineRewriter.rewriteChat(raw, username, NameConfig.get());
			Component out = rewritten != null ? rewritten : raw;
			debugNametag("self",
				"raw", LineRewriter.escapeForLog(raw.getString()),
				"out", LineRewriter.escapeForLog(out.getString()));
			return out;
		}

		return raw;
	}

	public static boolean isHidden(AbstractClientPlayer player) {
		NameConfig config = NameConfig.get();
		if (config.hidePlayerNameTags) {
			return true;
		}

		return Minecraft.getInstance().isLocalPlayer(player.getUUID()) ? config.hideOwnNameTag : config.hideOtherNameTags;
	}
}
