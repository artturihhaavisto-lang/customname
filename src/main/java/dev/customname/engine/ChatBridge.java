package dev.customname.engine;

import com.mojang.authlib.GameProfile;
import dev.customname.CustomNameClient;
import dev.customname.config.NameConfig;
import dev.customname.util.HypixelSpoof;
import java.time.Instant;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.ChatType;

/**
 * Rewrites incoming chat through the Fabric message API instead of a mixin.
 *
 * <p>Hypixel delivers chat as system messages, not signed player chat, so
 * {@code MODIFY_GAME} is the hook that actually sees those lines (this API
 * generation offers no modify event for player chat). This also covers the
 * action bar (overlay = true), which is where purse text appears.
 *
 * <p>Chat lines run through {@link LineRewriter#rewriteChat}: the local
 * player's sender header is normalised (emblems stripped), then custom name,
 * SkyBlock level tag and donor rank are spoofed, restricted to the header
 * where that matters.
 *
 * <p><b>Debug logging (temporary):</b> every message seen by MODIFY_GAME and
 * CHAT is logged with the tag {@code [CustomName/debug]} so real Hypixel line
 * formats can be inspected in logs/latest.log. Remove once the format is
 * confirmed.
 */
public final class ChatBridge {
	private ChatBridge() {
	}

	public static void register() {
		ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> apply(message, overlay));

		// Listen-only: player-chat lines cannot be modified in this API generation,
		// but logging them tells us whether Hypixel routes chat through them.
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) ->
			CustomNameClient.LOGGER.info(
				"[CustomName/debug] CHAT sender={} line=[{}]",
				sender == null ? "?" : sender.name(),
				message == null ? "?" : message.getString()
			)
		);
	}

	private static Component apply(Component message, boolean overlay) {
		if (message == null) {
			return message;
		}

		if (overlay) {
			Component rewritten = HypixelSpoof.rewriteOverlay(message);
			return rewritten == null ? message : rewritten;
		}

		NameConfig config = NameConfig.get();
		if (!Identity.active(config)) {
			return message;
		}

		String username = Identity.username();
		if (username == null) {
			return message;
		}

		CustomNameClient.LOGGER.info("[CustomName/debug] GAME in=[{}]", message.getString());

		// The same rewrite the tab list uses, chat-tuned: header normalised
		// (no emblem), name mentions, level tag and rank spoofed in the header.
		Component rewritten = LineRewriter.rewriteChat(message, username, config);

		Component out = rewritten == null ? message : rewritten;
		if (out != message) {
			CustomNameClient.LOGGER.info("[CustomName/debug] GAME out=[{}]", out.getString());
		}

		return out;
	}
}
