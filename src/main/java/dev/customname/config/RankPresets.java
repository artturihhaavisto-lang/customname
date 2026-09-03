package dev.customname.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RankPresets {
   private static final Map<String, RankPresets.RankPreset> PRESETS = new LinkedHashMap<>();

   private RankPresets() {
   }

   private static void register(String id, String label, String format, String description) {
      PRESETS.put(id, new RankPresets.RankPreset(id, label, format, description));
   }

   public static Map<String, RankPresets.RankPreset> all() {
      return Collections.unmodifiableMap(PRESETS);
   }

   public static Optional<RankPresets.RankPreset> get(String id) {
      if (id == null) {
         return Optional.empty();
      } else {
         RankPresets.RankPreset exact = PRESETS.get(id.toLowerCase());
         if (exact != null) {
            return Optional.of(exact);
         } else {
            for (RankPresets.RankPreset preset : PRESETS.values()) {
               if (preset.label().equalsIgnoreCase(id) || preset.id().equalsIgnoreCase(id)) {
                  return Optional.of(preset);
               }
            }

            return Optional.empty();
         }
      }
   }

   static {
      register("h", "H", "&c[&e\u12de&c]", "Hypixel staff rank (including owners).");
      register("youtube", "YOUTUBE", "&c[&fYOUTUBE&c]", "Hypixel creator program rank.");
      register("pig", "PIG+++", "&d[&bPIG+++&d]", "Exclusive to Technoblade.");
      register("innit", "INNIT", "&d[&fINNIT&d]", "Exclusive to TommyInnit.");
      register("mojang", "MOJANG", "&6[&6MOJANG&6]", "Exclusive to Mojang Studios employees.");
      register("events", "EVENTS", "&6[&6EVENTS&6]", "Official Hypixel events account.");
      register("mcp", "MCP", "&c[&dMCP&c]", "MasterControl developer account.");
   }

   public record RankPreset(String id, String label, String format, String description) {
   }
}
