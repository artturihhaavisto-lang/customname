package dev.customname.util;

import dev.customname.config.NameConfig;
import dev.customname.engine.Identity;
import dev.customname.engine.LineRewriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

public final class NameTagFromTab {
	private NameTagFromTab() {
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
			return rewritten != null ? rewritten : raw;
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
