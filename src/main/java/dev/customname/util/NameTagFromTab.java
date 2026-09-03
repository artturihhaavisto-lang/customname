package dev.customname.util;

import dev.customname.config.NameConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.LoggerFactory;

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

		boolean self = mc.isLocalPlayer(player.getUUID());

		if (self) {
			Component rewritten = TabDisplayRewriter.rewrite(raw);
			LoggerFactory.getLogger("customname").info(
				"nametag raw=[{}] rewritten=[{}] same={}",
				raw.getString(),
				rewritten != null ? rewritten.getString() : "null",
				rewritten == raw
			);
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
