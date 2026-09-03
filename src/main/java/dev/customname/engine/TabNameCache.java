package dev.customname.engine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;

public final class TabNameCache {
	private static final Map<UUID, Component> SPOOFED = new ConcurrentHashMap<>();

	private TabNameCache() {
	}

	public static void put(UUID uuid, Component name) {
		SPOOFED.put(uuid, name);
	}

	public static Component get(UUID uuid) {
		return SPOOFED.get(uuid);
	}
}
