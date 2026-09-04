package dev.customname.util;

import dev.customname.config.NameConfig;
import dev.customname.engine.Identity;
import dev.customname.engine.NameStyler;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Public entry point for building the local player's styled display name. */
public final class DisplayNameBuilder {
	private DisplayNameBuilder() {
	}

	public static boolean appliesTo(UUID uuid) {
		NameConfig config = NameConfig.get();
		return Identity.isLocal(uuid) && config.enabled && (config.hasCustomDisplay() || config.hasEmblemSpoof());
	}

	public static boolean appliesToLocalPlayer() {
		NameConfig config = NameConfig.get();
		return Minecraft.getInstance().player != null && config.enabled && (config.hasCustomDisplay() || config.hasEmblemSpoof());
	}

	public static MutableComponent build(String realUsername) {
		return build(realUsername, NameConfig.get());
	}

	public static MutableComponent build(String realUsername, NameConfig config) {
		return NameStyler.full(realUsername, config, true);
	}

	public static MutableComponent buildPrefix() {
		return buildPrefix(NameConfig.get());
	}

	public static MutableComponent buildPrefix(NameConfig config) {
		return NameStyler.prefix(config, true);
	}

	public static MutableComponent buildNameOnly(String realUsername) {
		return buildNameOnly(realUsername, NameConfig.get());
	}

	public static MutableComponent buildNameOnly(String realUsername, NameConfig config) {
		return NameStyler.name(realUsername, config, true);
	}

	public static MutableComponent buildForLocalPlayer() {
		return buildForLocalPlayer(NameConfig.get());
	}

	public static MutableComponent buildForLocalPlayer(NameConfig config) {
		LocalPlayer player = Minecraft.getInstance().player;
		return build(player != null ? player.getGameProfile().name() : "Player", config);
	}

	public static MutableComponent buildChatPreview(NameConfig config) {
		return buildForLocalPlayer(config).copy().append(Component.literal(": Hello!"));
	}
}
