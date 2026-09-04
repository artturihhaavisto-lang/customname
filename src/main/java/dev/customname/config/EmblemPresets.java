package dev.customname.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SkyBlock emblem presets (dungeon, skill, leveling, slayer, achievement, MVP++, special).
 * Each preset is a color-coded glyph ({@code format}) that can be shown beside the
 * local player's name in chat, the tab list and the name tag.
 */
public final class EmblemPresets {
   private static final Map<String, EmblemPreset> PRESETS = new LinkedHashMap<>();
   private static final List<String> CATEGORIES = new ArrayList<>();

   private EmblemPresets() {
   }

   private static void register(String id, String category, String label, String format, String requirement) {
      if (!CATEGORIES.contains(category)) {
         CATEGORIES.add(category);
      }
      PRESETS.put(id, new EmblemPreset(id, category, label, format, requirement));
   }

   public static Map<String, EmblemPreset> all() {
      return Collections.unmodifiableMap(PRESETS);
   }

   public static List<String> categories() {
      return Collections.unmodifiableList(CATEGORIES);
   }

   public static List<EmblemPreset> inCategory(String category) {
      List<EmblemPreset> out = new ArrayList<>();
      for (EmblemPreset preset : PRESETS.values()) {
         if (preset.category().equals(category)) {
            out.add(preset);
         }
      }
      return out;
   }

   public static Optional<EmblemPreset> get(String id) {
      if (id == null) {
         return Optional.empty();
      } else {
         EmblemPreset exact = PRESETS.get(id.toLowerCase());
         if (exact != null) {
            return Optional.of(exact);
         } else {
            for (EmblemPreset preset : PRESETS.values()) {
               if (preset.id().equalsIgnoreCase(id) || preset.label().equalsIgnoreCase(id)) {
                  return Optional.of(preset);
               }
            }
            return Optional.empty();
         }
      }
   }

   static {
      register("mining_pickaxe", "Skills", "Mining Pickaxe", "&7&l\u2E15", "Mining L (50)");
      register("mining_master", "Skills", "Mining Master", "&6&l\u2E15", "Mining LX (60)");
      register("farming_flower", "Skills", "Farming Flower", "&7\u273F", "Farming L (50)");
      register("farming_master", "Skills", "Farming Master", "&6\u273F", "Farming LX (60)");
      register("combat_explosion", "Skills", "Combat Explosion", "&7\u2741", "Combat L (50)");
      register("combat_master", "Skills", "Combat Master", "&6\u2741", "Combat LX (60)");
      register("foraging_leaf", "Skills", "Foraging Leaf", "&7\u2E19", "Foraging L (50)");
      register("fishing_fish", "Skills", "Fishing Fish", "&7\u03B1", "Fishing L (50)");
      register("enchanting_pencil", "Skills", "Enchanting Pencil", "&7\u270E", "Enchanting L (50)");
      register("enchanting_master", "Skills", "Enchanting Master", "&6\u270E", "Enchanting LX (60)");
      register("alchemy_brew", "Skills", "Alchemy Brew", "&7\u2615", "Alchemy L (50)");
      register("carpentry_house", "Skills", "Carpentry House", "&7\u2616", "Carpentry L (50)");
      register("taming_clover", "Skills", "Taming Clover", "&7\u2663", "Taming L (50)");
      register("taming_master", "Skills", "Taming Master", "&6\u2663", "Taming LX (60)");
      register("social_statement", "Skills", "Social Statement", "&7&l\u213B", "Social XV (15)");
      register("social_master", "Skills", "Social Master", "&6&l\u213B", "Social XXV (25)");
      register("catacombs_swords", "Catacombs", "Catacombs Swords", "&7&l\u2694", "Catacombs XL (40)");
      register("catacombs_swords_gold", "Catacombs", "Golden Catacombs Swords", "&6&l\u2694", "Catacombs L (50)");
      register("archer_spade", "Catacombs", "Archer Spade", "&7\u27B6", "Archer XL");
      register("archer_master", "Catacombs", "Archer Master", "&6\u27B6", "Archer L");
      register("mage_lightning", "Catacombs", "Mage Lightning", "&7&l\u26A1", "Mage XL");
      register("mage_master", "Catacombs", "Mage Master", "&6&l\u26A1", "Mage L");
      register("berserk_explosion", "Catacombs", "Berserk Explosion", "&7\u2604", "Berserk XL");
      register("berserk_master", "Catacombs", "Berserk Master", "&6\u2604", "Berserk L");
      register("healer_staff", "Catacombs", "Healer Staff", "&7\u269A", "Healer XL");
      register("healer_master", "Catacombs", "Healer Master", "&6\u269A", "Healer L");
      register("tank_anchor", "Catacombs", "Tank Anchor", "&7\u2693", "Tank XL");
      register("tank_master", "Catacombs", "Tank Master", "&6\u2693", "Tank L");
      register("dungeon_runner", "Catacombs", "Dungeon Runner", "&7\u2620", "The Catacombs - Floor VII Completion");
      register("dungeon_master", "Catacombs", "Dungeon Master", "&6\u2620", "Master Mode Catacombs - Floor VII Completion");
      register("class_master", "Catacombs", "Class Master", "&7&l\u269B", "Class Average XXX (30)");
      register("class_master_gold", "Catacombs", "Golden Class Master", "&6&l\u269B", "Class Average XL (40)");
      register("class_master_diamond", "Catacombs", "Diamond Class Master", "&b&l\u269B", "Class Average L (50)");
      register("diamond", "Leveling", "Diamond", "&7\u2666", "SkyBlock Level 10");
      register("spade", "Leveling", "Spade", "&7\u2660", "SkyBlock Level 20");
      register("heart", "Leveling", "Heart", "&7\u2764", "SkyBlock Level 30");
      register("pristine", "Leveling", "Pristine", "&7\u2727", "SkyBlock Level 40");
      register("arc_reactor", "Leveling", "Arc Reactor", "&7\u238A", "SkyBlock Level 50");
      register("marker", "Leveling", "Marker", "&7\u1360", "SkyBlock Level 60");
      register("badge", "Leveling", "Badge", "&7\u262C", "SkyBlock Level 70");
      register("star", "Leveling", "Star", "&7&l\u269D", "SkyBlock Level 80");
      register("boxes", "Leveling", "Boxes", "&7\u29C9", "SkyBlock Level 90");
      register("jerry", "Leveling", "Jerry", "&7&l\uA214", "SkyBlock Level 100");
      register("globe", "Leveling", "Globe", "&7&l\u32D6", "SkyBlock Level 120");
      register("soulflow", "Leveling", "Soulflow", "&7\u2E0E", "SkyBlock Level 140");
      register("warning", "Leveling", "Warning", "&7\u26A0", "SkyBlock Level 160");
      register("mustache", "Leveling", "Mustache", "&7&l\uA541", "SkyBlock Level 180");
      register("helmet", "Leveling", "Helmet", "&7\u3020", "SkyBlock Level 200");
      register("sideways_smiley", "Leveling", "Sideways Smiley", "&7&l\u30C4", "SkyBlock Level 250");
      register("spaceship", "Leveling", "Spaceship", "&7\u2948", "SkyBlock Level 300");
      register("toxic", "Leveling", "Toxic", "&7\u2622", "SkyBlock Level 350");
      register("biohazard", "Leveling", "Biohazard", "&7\u2623", "SkyBlock Level 400");
      register("florette", "Leveling", "Florette", "&7\u273E", "SkyBlock Level 450");
      register("fleur_de_lis", "Leveling", "Golden Fleur De Lis", "&6\u269C", "SkyBlock Level 500");
      register("revenant_brain", "Slayer", "Revenant Horror Brain", "&7\u0BD0", "Revenant Horror Tier V (5) Completion");
      register("revenant_brain_gold", "Slayer", "Golden Revenant Horror Brain", "&6\u0BD0", "Zombie Slayer LVL IX (9)");
      register("broodmother_string", "Slayer", "Broodmother String", "&7\u0A6D", "Tarantula Broodfather Tier IV (4) Completion");
      register("broodmother_gold", "Slayer", "Golden Broodmother String", "&6\u0A6D", "Spider Slayer LVL IX (9)");
      register("sven_shield", "Slayer", "Sven Shield", "&7\u2742", "Sven Packmaster Tier IV (4) Completion");
      register("sven_shield_gold", "Slayer", "Golden Sven Shield", "&6\u2742", "Wolf Slayer LVL IX (9)");
      register("voidgloom_rune", "Slayer", "Voidgloom Rune", "&7\u16C3", "Voidgloom Seraph Tier IV (4) Completion");
      register("voidgloom_rune_gold", "Slayer", "Golden Voidgloom Rune", "&6\u16C3", "Enderman Slayer LVL IX (9)");
      register("inferno_rods", "Slayer", "Inferno Rods", "&7\u3023", "Inferno Demonlord Tier IV (4) Completion");
      register("inferno_rods_gold", "Slayer", "Golden Inferno Rods", "&6\u3023", "Blaze Slayer LVL IX (9)");
      register("riftstalker", "Slayer", "Riftstalker Strange Time", "&7\u10F6", "Riftstalker Bloodfiend Tier V (5) Completion");
      register("riftstalker_gold", "Slayer", "Golden Riftstalker Strange Time", "&6\u10F6", "Vampire Slayer LVL V (5)");
      register("rift_time", "Achievement", "Rift Time", "&7\u0444", "Completed the Rift Guide");
      register("harp_note", "Achievement", "Harp Note", "&7\u266A", "All Harp Songs Completed");
      register("harp_master", "Achievement", "Harp Master", "&6\u266B", "All Harp Songs Perfected");
      register("kuudra_slayer", "Achievement", "Kuudra Slayer", "&7&l\u04C3", "2000 Kuudra Boss Collection Progress");
      register("kuudra_slayer_master", "Achievement", "Master Kuudra Slayer", "&6&l\u04C3", "5000 Kuudra Boss Collection Progress");
      register("ultra_banker", "Achievement", "Ultra Banker", "&6\u26C1", "Luxurious Bank Upgrade");
      register("extreme_banker", "Achievement", "Extreme Banker", "&6\u26C3", "Palatial Bank Upgrade");
      register("mining_helix", "Achievement", "Mining Helix", "&7&l\u16DD", "Heart of the Mountain 5");
      register("mining_helix_gold", "Achievement", "Golden Mining Helix", "&6&l\u16DD", "Heart of the Mountain 7");
      register("mining_helix_diamond", "Achievement", "Diamond Mining Helix", "&b&l\u16DD", "Heart of the Mountain 10");
      register("foraging_clover", "Achievement", "Foraging Clover", "&7&l\u2618", "Heart of the Forest 5");
      register("foraging_clover_gold", "Achievement", "Golden Foraging Clover", "&6&l\u2618", "Heart of the Forest 7");
      register("agatha_jewel", "Achievement", "Agatha's Jewel", "&7&l\uA598", "Agatha Milestone V (5)");
      register("gardener", "Achievement", "Gardener", "&7\uA03E", "Garden XII (12)");
      register("gardener_gold", "Achievement", "Golden Gardener", "&6\uA03E", "Garden XV (15)");
      register("collecting_top_hat", "Achievement", "Collecting Top Hat", "&7\u1C6A", "100 Museum Donations");
      register("collecting_top_hat_gold", "Achievement", "Golden Collecting Top Hat", "&6\u1C6A", "300 Museum Donations");
      register("magical_sigma", "Achievement", "Magical Sigma", "&7\u03A3", "1000 Accessory Power");
      register("magical_sigma_gold", "Achievement", "Golden Magical Sigma", "&6\u03A3", "2000 Accessory Power");
      register("chili_pepper_gold", "Achievement", "Golden Chili Pepper", "&6\u09EB", "5 Reaper Peppers Eaten");
      register("snowman", "Achievement", "Snowman", "&7\u2603", "500 Gifts Given");
      register("trophy_king", "Achievement", "Trophy King", "&7\u2654", "All Silver Trophy Fish");
      register("trophy_king_gold", "Achievement", "Golden Trophy King", "&6\u2654", "All Gold Trophy Fish");
      register("trophy_king_diamond", "Achievement", "Diamond Trophy King", "&b\u2654", "All Diamond Trophy Fish");
      register("trophy_frog_king", "Achievement", "Trophy Frog King", "&7\uD83D\uDC38", "All Silver Trophy Frogs");
      register("trophy_frog_king_gold", "Achievement", "Golden Trophy Frog King", "&6\uD83D\uDC38", "All Gold Trophy Frogs");
      register("trophy_frog_king_diamond", "Achievement", "Diamond Trophy Frog King", "&b\uD83D\uDC38", "All Diamond Trophy Frogs");
      register("century_celebrant", "Achievement", "Century Celebrant", "&7\u26C2", "Raffle Milestone II");
      register("century_partygoer", "Achievement", "Century Partygoer", "&6\u26C2", "Raffle Milestone IV");
      register("great_spook", "Achievement", "Great Spook", "&5\u0FC7", "Vargul the Unearthed Milestone VI");
      register("gift_of_giving", "Achievement", "Gift of Giving", "&6\u2709", "Gift Milestone XXV");
      register("jacobs_contest", "Achievement", "Jacob's Contest Completionist", "&7\uA56A", "Silver in all crops in Jacob's Farming Contests");
      register("jacobs_contest_gold", "Achievement", "Golden Jacob's Contest Completionist", "&6\uA56A", "Gold in all crops in Jacob's Farming Contests");
      register("jacobs_contest_diamond", "Achievement", "Diamond Jacob's Contest Completionist", "&b\uA56A", "Diamond in all crops in Jacob's Farming Contests");
      register("chocolate_bar", "Achievement", "Chocolate Bar", "&7\u2592", "5M all-time Chocolate");
      register("chocolate_bar_gold", "Achievement", "Golden Chocolate Bar", "&6\u2592", "50B all-time Chocolate");
      register("burning_strength", "Achievement", "Emblem of Burning Strength", "&6\u2600", "Purchase at Oruo The Almighty");
      register("subscriber_1", "MVP++", "1-Month Subscriber's Star", "&d\u2729", "MVP++ and 1 total subscribed month");
      register("subscriber_2", "MVP++", "2-Month Subscriber's Star", "&d\u272C", "MVP++ and 2 total subscribed months");
      register("subscriber_3", "MVP++", "3-Month Subscriber's Star", "&d\u272E", "MVP++ and 3 total subscribed months");
      register("subscriber_6", "MVP++", "6-Month Subscriber's Star", "&d\u272F", "MVP++ and 6 total subscribed months");
      register("subscriber_9", "MVP++", "9-Month Subscriber's Star", "&d\u2736", "MVP++ and 9 total subscribed months");
      register("subscriber_12", "MVP++", "12-Month Subscriber's Star", "&d\u2733", "MVP++ and 12 total subscribed months");
      register("subscriber_18", "MVP++", "18-Month Subscriber's Star", "&d\u2734", "MVP++ and 18 total subscribed months");
      register("subscriber_24", "MVP++", "24-Month Subscriber's Star", "&d\u2737", "MVP++ and 24 total subscribed months");
      register("subscriber_36", "MVP++", "36-Month Subscriber's Star", "&d\u2738", "MVP++ and 36 total subscribed months");
      register("subscriber_48", "MVP++", "48-Month Subscriber's Star", "&d\u2739", "MVP++ and 48 total subscribed months");
      register("subscriber_60", "MVP++", "60-Month Subscriber's Star", "&d\u273A", "MVP++ and 60 total subscribed months");
      register("hypixel", "Special", "Hypixel", "&6\u12DE", "Hypixel Staff Only");
      register("raffle_green", "Special", "Century Raffle Green", "&a\u26C3", "Randomly selected during Raffle of the Century");
      register("raffle_yellow", "Special", "Century Raffle Yellow", "&e\u26C3", "Randomly selected during Raffle of the Century");
      register("raffle_red", "Special", "Century Raffle Red", "&c\u26C3", "Randomly selected during Raffle of the Century");
      register("raffle_pink", "Special", "Century Raffle Pink", "&d\u26C3", "Randomly selected during Raffle of the Century");
      register("raffle_blue", "Special", "Century Raffle Blue", "&9\u26C3", "Randomly selected during Raffle of the Century");
   }

   public record EmblemPreset(String id, String category, String label, String format, String requirement) {
   }
}
