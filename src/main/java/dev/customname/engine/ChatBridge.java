package dev.customname.engine;

import dev.customname.config.NameConfig;
import dev.customname.util.HypixelSpoof;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;

/**
 * Rewrites incoming chat through the Fabric message API instead of a mixin.
 *
 * <p>Hypixel delivers chat as system messages, not signed player chat, so
 * {@code MODIFY_GAME} is the hook that actually sees those lines (this API
 * generation offers no modify event for player chat). This also covers the
 * action bar (overlay = true), which is where purse text appears.
 *
 * <p>Chat lines run through {@link LineRewriter#rewriteChat}: the local
 * player's sender header is normalised (equipped emblems stripped), then custom
 * name, SkyBlock level tag and donor rank are spoofed, restricted to the header
 * where that matters.
 */
public final class ChatBridge {
	private ChatBridge() {
	}

	public static void register() {
		ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> apply(message, overlay));
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

		// The same rewrite the tab list uses, chat-tuned: header normalised
		// (no emblem), name mentions, level tag and rank spoofed in the header.
		Component rewritten = LineRewriter.rewriteChat(message, username, config);
		return rewritten == null ? message : rewritten;
	}
}
