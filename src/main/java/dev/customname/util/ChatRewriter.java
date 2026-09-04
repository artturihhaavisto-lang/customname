package dev.customname.util;

import dev.customname.config.NameConfig;
import dev.customname.engine.LineRewriter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Chat-side helpers. The live rewrite happens in {@code dev.customname.engine.ChatBridge}. */
public final class ChatRewriter {
	private ChatRewriter() {
	}

	/**
	 * Builds the config screen's chat preview from a fake vanilla sender line,
	 * using the same rewrite the real chat path uses.
	 */
	public static Component previewChatLine(Component vanillaSender, NameConfig draft, String username) {
		// Preview always shows the chosen emblem so it is visible in the config screen,
		// even when the screen is opened outside SkyBlock.
		Component sender = LineRewriter.rewriteChat(vanillaSender, username, draft, false, true);
		MutableComponent out = Component.empty();
		out.append(sender == null ? vanillaSender : sender);
		out.append(Component.literal(": Hello!"));
		return out;
	}
}
