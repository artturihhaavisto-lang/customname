package dev.customcape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CapeCatalog {
   private static final Map<String, CapeCatalog.Entry> OFFICIAL = new LinkedHashMap<>();

   private CapeCatalog() {
   }

   private static void add(String id, String name) {
      OFFICIAL.put(id, new CapeCatalog.Entry(id, name, CapeCatalog.Kind.OFFICIAL));
   }

   public static CapeCatalog.Entry vanilla() {
      return new CapeCatalog.Entry("vanilla", "Account Cape (Vanilla)", CapeCatalog.Kind.VANILLA);
   }

   public static CapeCatalog.Entry none() {
      return new CapeCatalog.Entry("none", "No Cape", CapeCatalog.Kind.NONE);
   }

   public static List<CapeCatalog.Entry> official() {
      return Collections.unmodifiableList(new ArrayList<>(OFFICIAL.values()));
   }

   public static Optional<CapeCatalog.Entry> official(String id) {
      return Optional.ofNullable(OFFICIAL.get(id));
   }

   public static Optional<CapeCatalog.Entry> resolveBuiltIn(String id) {
      if ("vanilla".equals(id)) {
         return Optional.of(vanilla());
      } else {
         return "none".equals(id) ? Optional.of(none()) : official(id);
      }
   }

   static {
      add("minecon_2011", "MINECON 2011");
      add("minecon_2012", "MINECON 2012");
      add("minecon_2013", "MINECON 2013");
      add("minecon_2015", "MINECON 2015");
      add("minecon_2016", "MINECON 2016");
      add("minecon_2019", "MINECON Live 2019");
      add("mojang", "Mojang");
      add("mojang_classic", "Mojang Classic");
      add("mojang_studios", "Mojang Studios");
      add("mojang_office", "Mojang Office");
      add("migrator", "Migrator");
      add("vanilla_cape", "Vanilla");
      add("cherry_blossom", "Cherry Blossom");
      add("pan", "Pan");
      add("followers", "Follower's");
      add("purple_heart", "Purple Heart");
      add("bacon", "Bacon");
      add("birthday", "Birthday");
      add("christmas_2010", "Christmas 2010");
      add("new_year_2011", "New Year 2011");
      add("cobalt", "Cobalt");
      add("scrolls", "Scrolls Champion");
      add("translator", "Translator");
      add("chinese_translator", "Chinese Translator");
      add("mapmaker", "Map Maker");
      add("moderator", "Moderator");
      add("millionth", "Millionth Customer");
      add("prismarine", "Prismarine");
      add("turtle", "Turtle");
      add("cheapshot", "Cheapsh0t");
      add("dannybstyle", "dannyBstyle");
      add("julianclark", "JulianClark");
      add("mrmessiah", "MrMessiah");
      add("experience", "Minecraft Experience");
      add("mcc_15th", "MCC 15th Year");
      add("anniversary_15", "15th Anniversary");
      add("home", "Home");
      add("menace", "Menace");
      add("common", "Common");
   }

   public record Entry(String id, String displayName, CapeCatalog.Kind kind) {
      public boolean isBundled() {
         return this.kind == CapeCatalog.Kind.OFFICIAL;
      }
   }

   public static enum Kind {
      VANILLA,
      NONE,
      OFFICIAL,
      CUSTOM;

      private Kind() {
      }
   }
}
