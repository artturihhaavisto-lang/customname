package dev.customname.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.customname.CustomNameClient;
import dev.customname.util.NameTagFromTab;
import dev.customname.util.TabDisplayRewriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public final class NameConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static NameConfig INSTANCE = new NameConfig();

   /** Resolved lazily so the class can be loaded outside a launched game. */
   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("customname.json");
   }

   public String name = "";
   public String nameColor = "";
   public String prefix = "";
   public String presetId = "";
   public boolean enabled = true;
   public boolean nameChroma = false;
   public String nameChromaStart = "#FF5555";
   public String nameChromaEnd = "#55FFFF";
   public boolean prefixChroma = false;
   public String prefixChromaStart = "#FFAA00";
   public String prefixChromaEnd = "#FF55FF";
public boolean replaceLevelWithPrefix = false;
    public boolean spoofSkyblockLevel = false;
    public String spoofSkyblockLevelValue = "420";
    public boolean prefixEnabled = false;
    public boolean levelOverridesPrefixInSkyblock = false;
    public boolean ownTabListNameTag = false;
   public boolean hideOwnNameTag = false;
   public boolean hideOtherNameTags = false;
   public boolean hidePlayerNameTags = false;
   public boolean nameBold = false;
   public boolean nameItalic = false;
   public boolean nameUnderline = false;
   public boolean nameStrikethrough = false;
   public boolean nameObfuscated = false;
   public boolean prefixBold = false;
   public boolean prefixItalic = false;
   public boolean prefixUnderline = false;
   public boolean prefixStrikethrough = false;
   public boolean prefixObfuscated = false;
   public boolean spoofPurse = false;
   public String spoofPurseAmount = "1,000,000,000";
   public boolean spoofLobby = false;
   public String spoofLobbyId = "1";
   public List<NameConfig.SavedPrefix> savedPrefixes = new ArrayList<>();

   private NameConfig() {
   }

   public static NameConfig get() {
      return INSTANCE;
   }

   public static void load() {
      Path PATH = path();
      if (!Files.exists(PATH)) {
         save();
      } else {
         try (BufferedReader reader = Files.newBufferedReader(PATH)) {
            NameConfig loaded = (NameConfig)GSON.fromJson(reader, NameConfig.class);
            if (loaded != null) {
               loaded.normalize();
               INSTANCE = loaded;
            }
         } catch (IOException var5) {
            CustomNameClient.LOGGER.error("Failed to load customname.json", var5);
         }
      }
   }

   public static void save() {
      try {
         Path PATH = path();
         Files.createDirectories(PATH.getParent());

         try (BufferedWriter writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(INSTANCE, writer);
         }

         invalidateDisplayCaches();
      } catch (IOException var5) {
         CustomNameClient.LOGGER.error("Failed to save customname.json", var5);
      }
   }

   private void normalize() {
      if (this.name == null) {
         this.name = "";
      }

      if (this.nameColor == null) {
         this.nameColor = "";
      }

      if (this.prefix == null) {
         this.prefix = "";
      }

      if (this.presetId == null) {
         this.presetId = "";
      }

      if (this.nameChromaStart == null) {
         this.nameChromaStart = "#FF5555";
      }

      if (this.nameChromaEnd == null) {
         this.nameChromaEnd = "#55FFFF";
      }

      if (this.prefixChromaStart == null) {
         this.prefixChromaStart = "#FFAA00";
      }

      if (this.prefixChromaEnd == null) {
         this.prefixChromaEnd = "#FF55FF";
      }

      if (this.spoofPurseAmount == null || this.spoofPurseAmount.isBlank()) {
         this.spoofPurseAmount = "1,000,000,000";
      }

      if (this.spoofLobbyId == null) {
         this.spoofLobbyId = "1";
      }

      if (this.spoofSkyblockLevelValue == null || this.spoofSkyblockLevelValue.isBlank()) {
         this.spoofSkyblockLevelValue = "420";
      }

      if (this.hidePlayerNameTags) {
         this.hideOwnNameTag = true;
         this.hideOtherNameTags = true;
         this.hidePlayerNameTags = false;
      }

      if (this.savedPrefixes == null) {
         this.savedPrefixes = new ArrayList<>();
      }

      for (NameConfig.SavedPrefix preset : this.savedPrefixes) {
         preset.normalize();
      }
   }

   public NameConfig.SavedPrefix upsertSavedPrefix(String prefix, boolean chroma, String chromaStart, String chromaEnd) {
      if (this.savedPrefixes == null) {
         this.savedPrefixes = new ArrayList<>();
      }

      for (NameConfig.SavedPrefix preset : this.savedPrefixes) {
         if (prefix.equals(preset.prefix)) {
            preset.chroma = chroma;
            preset.chromaStart = chromaStart;
            preset.chromaEnd = chromaEnd;
            preset.normalize();
            return preset;
         }
      }

      NameConfig.SavedPrefix created = new NameConfig.SavedPrefix();
      created.id = Long.toString(System.nanoTime(), 36);
      created.prefix = prefix;
      created.chroma = chroma;
      created.chromaStart = chromaStart;
      created.chromaEnd = chromaEnd;
      created.normalize();
      this.savedPrefixes.add(created);
      return created;
   }

   public void removeSavedPrefix(String id) {
      if (this.savedPrefixes != null && id != null) {
         Iterator<NameConfig.SavedPrefix> iterator = this.savedPrefixes.iterator();

         while (iterator.hasNext()) {
            if (id.equals(iterator.next().id)) {
               iterator.remove();
               return;
            }
         }
      }
   }

   public static String savedPresetId(String id) {
      return "saved:" + id;
   }

   public static boolean isSavedPreset(String presetId) {
      return presetId != null && presetId.startsWith("saved:");
   }

   public boolean hasCustomDisplay() {
      return this.enabled
         && (
            !this.name.isBlank()
               || !this.prefix.isBlank()
               || !this.nameColor.isBlank()
               || this.nameChroma
               || this.prefixChroma
               || this.nameBold
               || this.nameItalic
               || this.nameUnderline
               || this.nameStrikethrough
               || this.nameObfuscated
               || this.prefixBold
               || this.prefixItalic
               || this.prefixUnderline
               || this.prefixStrikethrough
               || this.prefixObfuscated
         );
   }

   public boolean hasRankSpoof() {
       return this.prefix != null && !this.prefix.isBlank();
    }

   public boolean hasLevelSpoof() {
      return this.spoofSkyblockLevel && TabDisplayRewriter.formatLevel(this.spoofSkyblockLevelValue) != null;
   }

   public boolean hasHypixelSpoof() {
      return this.hasRankSpoof() || this.hasLevelSpoof();
   }

   public static void invalidateDisplayCaches() {
      NameTagFromTab.invalidateCache();
      TabDisplayRewriter.invalidateCache();
   }

   public void clear() {
      this.name = this.nameColor = this.prefix = this.presetId = "";
      this.nameChroma = this.prefixChroma = false;
      this.nameBold = this.nameItalic = this.nameUnderline = this.nameStrikethrough = this.nameObfuscated = false;
      this.prefixBold = this.prefixItalic = this.prefixUnderline = this.prefixStrikethrough = this.prefixObfuscated = false;
this.replaceLevelWithPrefix = false;
       this.spoofSkyblockLevel = false;
       this.spoofSkyblockLevelValue = "420";
       this.prefixEnabled = true;
       this.levelOverridesPrefixInSkyblock = false;
       this.ownTabListNameTag = false;
      this.hideOwnNameTag = false;
      this.hideOtherNameTags = false;
      this.hidePlayerNameTags = false;
      this.spoofPurse = false;
      this.spoofPurseAmount = "1,000,000,000";
      this.spoofLobby = false;
      this.spoofLobbyId = "1";
      this.enabled = true;
      save();
   }

   public NameConfig copy() {
      NameConfig copy = new NameConfig();
      copy.applyFrom(this);
      return copy;
   }

   public void applyFrom(NameConfig other) {
      this.name = other.name != null ? other.name : "";
      this.nameColor = other.nameColor != null ? other.nameColor : "";
      this.prefix = other.prefix != null ? other.prefix : "";
      this.presetId = other.presetId != null ? other.presetId : "";
      this.enabled = other.enabled;
      this.nameChroma = other.nameChroma;
      this.nameChromaStart = other.nameChromaStart != null ? other.nameChromaStart : "#FF5555";
      this.nameChromaEnd = other.nameChromaEnd != null ? other.nameChromaEnd : "#55FFFF";
      this.prefixChroma = other.prefixChroma;
      this.prefixChromaStart = other.prefixChromaStart != null ? other.prefixChromaStart : "#FFAA00";
      this.prefixChromaEnd = other.prefixChromaEnd != null ? other.prefixChromaEnd : "#FF55FF";
      this.replaceLevelWithPrefix = other.replaceLevelWithPrefix;
      this.spoofSkyblockLevel = other.spoofSkyblockLevel;
      this.spoofSkyblockLevelValue = other.spoofSkyblockLevelValue != null && !other.spoofSkyblockLevelValue.isBlank() ? other.spoofSkyblockLevelValue : "420";
      this.ownTabListNameTag = other.ownTabListNameTag;
      this.hideOwnNameTag = other.hideOwnNameTag;
      this.hideOtherNameTags = other.hideOtherNameTags;
      this.hidePlayerNameTags = false;
      this.nameBold = other.nameBold;
      this.nameItalic = other.nameItalic;
      this.nameUnderline = other.nameUnderline;
      this.nameStrikethrough = other.nameStrikethrough;
      this.nameObfuscated = other.nameObfuscated;
      this.prefixBold = other.prefixBold;
      this.prefixItalic = other.prefixItalic;
      this.prefixUnderline = other.prefixUnderline;
      this.prefixStrikethrough = other.prefixStrikethrough;
      this.prefixObfuscated = other.prefixObfuscated;
      this.spoofPurse = other.spoofPurse;
      this.spoofPurseAmount = other.spoofPurseAmount != null && !other.spoofPurseAmount.isBlank() ? other.spoofPurseAmount : "1,000,000,000";
      this.spoofLobby = other.spoofLobby;
      this.spoofLobbyId = other.spoofLobbyId != null ? other.spoofLobbyId : "1";
      this.savedPrefixes = new ArrayList<>();
      if (other.savedPrefixes != null) {
         for (NameConfig.SavedPrefix preset : other.savedPrefixes) {
            this.savedPrefixes.add(preset.copy());
         }
      }

      invalidateDisplayCaches();
   }

   public static final class SavedPrefix {
      public String id = "";
      public String prefix = "";
      public boolean chroma;
      public String chromaStart = "#FFAA00";
      public String chromaEnd = "#FF55FF";

      public SavedPrefix() {
      }

      public void normalize() {
         if (this.id == null) {
            this.id = "";
         }

         if (this.prefix == null) {
            this.prefix = "";
         }

         if (this.chromaStart == null || this.chromaStart.isBlank()) {
            this.chromaStart = "#FFAA00";
         }

         if (this.chromaEnd == null || this.chromaEnd.isBlank()) {
            this.chromaEnd = "#FF55FF";
         }
      }

      public NameConfig.SavedPrefix copy() {
         NameConfig.SavedPrefix copy = new NameConfig.SavedPrefix();
         copy.id = this.id;
         copy.prefix = this.prefix;
         copy.chroma = this.chroma;
         copy.chromaStart = this.chromaStart;
         copy.chromaEnd = this.chromaEnd;
         return copy;
      }
   }
}
