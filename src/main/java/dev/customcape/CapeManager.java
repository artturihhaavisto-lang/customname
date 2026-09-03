package dev.customcape;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.ClientAsset.Texture;
import net.minecraft.world.entity.player.PlayerSkin;

public final class CapeManager {
   private static CapeManager instance;
   private final CapeConfig config;
   private final CapeTextureManager textures;

   private CapeManager(CapeConfig config, CapeTextureManager textures) {
      this.config = config;
      this.textures = textures;
   }

   public static void init() {
      CapeConfig config = CapeConfig.load();
      CapeTextureManager textures = new CapeTextureManager();
      textures.initFolders();
      instance = new CapeManager(config, textures);
   }

   public static CapeManager get() {
      return instance;
   }

   public CapeConfig config() {
      return this.config;
   }

   public CapeTextureManager textures() {
      return this.textures;
   }

   public void reloadCustom() {
      this.textures.reload();
   }

   public CapeTextureManager.ImportResult importCape(Path source) {
      CapeTextureManager.ImportResult result = this.textures.importPng(source);
      if (result.success()) {
         this.select(result.id());
      }

      return result;
   }

   public void select(String id) {
      this.config.setSelectedCape(id);
      this.config.save();
   }

   public List<CapeCatalog.Entry> allEntries() {
      List<CapeCatalog.Entry> list = new ArrayList<>();
      list.add(CapeCatalog.vanilla());
      list.add(CapeCatalog.none());
      list.addAll(CapeCatalog.official());
      list.addAll(this.textures.customEntries());
      return list;
   }

   public Optional<CapeCatalog.Entry> findEntry(String id) {
      Optional<CapeCatalog.Entry> builtIn = CapeCatalog.resolveBuiltIn(id);
      return builtIn.isPresent() ? builtIn : this.textures.customEntries().stream().filter(e -> e.id().equals(id)).findFirst();
   }

   public PlayerSkin overrideSkin(AbstractClientPlayer player, PlayerSkin original) {
      return this.shouldApply(player) ? this.applyCape(original) : null;
   }

   public PlayerSkin applyCape(PlayerSkin original) {
      if (original == null) {
         return null;
      } else {
         String selected = this.config.selectedCape();
         if ("vanilla".equals(selected)) {
            return null;
         } else if ("none".equals(selected)) {
            return PlayerSkin.insecure(original.body(), null, null, original.model());
         } else {
            Optional<Texture> cape = this.textures.resolveTexture(selected);
            if (cape.isEmpty()) {
               return null;
            } else {
               Texture texture = cape.get();
               return PlayerSkin.insecure(original.body(), texture, texture, original.model());
            }
         }
      }
   }

   private boolean shouldApply(AbstractClientPlayer player) {
      Minecraft client = Minecraft.getInstance();
      LocalPlayer self = client.player;
      boolean isSelf = self != null && self.getUUID().equals(player.getUUID());
      return isSelf ? this.config.applyToSelf() : this.config.applyToOthers();
   }
}
