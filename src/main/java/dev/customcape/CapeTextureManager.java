package dev.customcape;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.ClientAsset.DownloadedTexture;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.ClientAsset.Texture;
import net.minecraft.resources.Identifier;

public final class CapeTextureManager {
   public static final String CUSTOM_PREFIX = "custom:";
   private final Map<String, CapeCatalog.Entry> customEntries = new LinkedHashMap<>();
   private final Map<String, Texture> textures = new LinkedHashMap<>();

   public CapeTextureManager() {
   }

   public void initFolders() {
      try {
         Files.createDirectories(CapeConfig.customDir());
         this.copyTemplateIfMissing();
         Path readme = CapeConfig.configDir().resolve("README.txt");
         if (!Files.exists(readme)) {
            Files.writeString(
               readme,
               "Custom Cape \u2014 64x32 template\n============================\n1. In the cape menu, click upload and pick a 64x32 PNG (or drop the file on the menu).\n2. You can also copy PNGs into this custom/ folder and press reload.\n3. Select your cape from the Custom Capes section.\n\nUV layout (standard Minecraft cape):\n- Left block: cape front/back faces\n- Right column: cape edges + elytra wing texture\n"
            );
         }
      } catch (IOException var21) {
         CustomCape.LOGGER.error("Failed to create config folders", var21);
      }
   }

   private void copyTemplateIfMissing() {
      Path templateOut = CapeConfig.configDir().resolve("cape_template.png");
      if (!Files.exists(templateOut)) {
         FabricLoader.getInstance().getModContainer("customname").or(() -> FabricLoader.getInstance().getModContainer("customcape")).ifPresent(container -> {
            Optional<Path> inMod = container.findPath("assets/customcape/textures/cape_template.png");
            if (inMod.isPresent()) {
               try {
                  Files.copy(inMod.get(), templateOut, StandardCopyOption.REPLACE_EXISTING);
               } catch (IOException var4) {
                  CustomCape.LOGGER.warn("Could not copy cape template", var4);
               }
            }
         });
      }
   }

   public CapeTextureManager.ImportResult importPng(Path source) {
      if (source != null && Files.isRegularFile(source)) {
         String fileName = source.getFileName().toString();
         if (!fileName.toLowerCase(Locale.ROOT).endsWith(".png")) {
            return CapeTextureManager.ImportResult.fail("Cape files must be PNG.");
         } else {
            int width;
            int height;
            try (InputStream in = Files.newInputStream(source)) {
               NativeImage image = NativeImage.read(in);

               try {
                  width = image.getWidth();
                  height = image.getHeight();
               } finally {
                  image.close();
               }
            } catch (Exception var18) {
               CustomCape.LOGGER.warn("Could not read cape {}", source, var18);
               return CapeTextureManager.ImportResult.fail("Could not read that PNG.");
            }

            if (width == 64 && height == 32) {
               try {
                  Files.createDirectories(CapeConfig.customDir());
                  Path dest = destFor(source);
                  Path from = source.toAbsolutePath().normalize();
                  Path to = dest.toAbsolutePath().normalize();
                  if (!from.equals(to)) {
                     Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                  }

                  this.reload();
                  String id = "custom:" + sanitize(stripPng(dest.getFileName().toString()));
                  return !this.customEntries.containsKey(id)
                     ? CapeTextureManager.ImportResult.fail("Copied, but the cape did not load.")
                     : CapeTextureManager.ImportResult.ok(id, this.customEntries.get(id).displayName());
               } catch (IOException var15) {
                  CustomCape.LOGGER.error("Failed to import cape {}", source, var15);
                  return CapeTextureManager.ImportResult.fail("Could not copy the PNG.");
               }
            } else {
               return CapeTextureManager.ImportResult.fail("Need 64\u00d732, got " + width + "\u00d7" + height + ".");
            }
         }
      } else {
         return CapeTextureManager.ImportResult.fail("Choose a PNG file.");
      }
   }

   private static Path destFor(Path source) {
      String base = stripPng(source.getFileName().toString()).replaceAll("[\\\\/:*?\"<>|]", "_");
      if (base.isBlank()) {
         base = "cape";
      }

      return CapeConfig.customDir().resolve(base + ".png");
   }

   private static String stripPng(String fileName) {
      return fileName.substring(0, fileName.length() - 4);
   }

   public void reload() {
      this.clearDynamicTextures();
      this.customEntries.clear();
      this.textures.clear();
      Path dir = CapeConfig.customDir();
      if (Files.isDirectory(dir)) {
         try (Stream<Path> stream = Files.list(dir)) {
            for (Path file : stream.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")).sorted().toList()) {
               this.loadCustom(file);
            }
         } catch (IOException var7) {
            CustomCape.LOGGER.error("Failed to scan custom capes", var7);
         }

         CustomCape.LOGGER.info("Loaded {} custom cape(s)", this.customEntries.size());
      }
   }

   private void loadCustom(Path file) {
      String fileName = file.getFileName().toString();
      String base = fileName.substring(0, fileName.length() - 4);
      String id = "custom:" + sanitize(base);
      Identifier textureId = CustomCape.id("dynamic/" + sanitize(base));

      try {
         try (InputStream in = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            if (image.getWidth() != 64 || image.getHeight() != 32) {
               CustomCape.LOGGER.warn("Skipping {}: expected 64x32, got {}x{}", new Object[]{fileName, image.getWidth(), image.getHeight()});
               image.close();
               return;
            }

            DynamicTexture dynamic = new DynamicTexture(() -> "customcape/" + base, image);
            Minecraft.getInstance().getTextureManager().register(textureId, dynamic);
            this.textures.put(id, new DownloadedTexture(textureId, file.toUri().toString()));
            this.customEntries.put(id, new CapeCatalog.Entry(id, base, CapeCatalog.Kind.CUSTOM));
         }
      } catch (Exception var111) {
         CustomCape.LOGGER.error("Failed to load custom cape {}", fileName, var111);
      }
   }

   private void clearDynamicTextures() {
      TextureManager textureManager = Minecraft.getInstance().getTextureManager();

      for (Texture texture : this.textures.values()) {
         textureManager.release(texture.texturePath());
      }
   }

   private static String sanitize(String name) {
      return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
   }

   public List<CapeCatalog.Entry> customEntries() {
      return Collections.unmodifiableList(new ArrayList<>(this.customEntries.values()));
   }

   public Optional<Texture> getCustomTexture(String id) {
      return Optional.ofNullable(this.textures.get(id));
   }

   public Texture getOfficialTexture(String id) {
      Identifier path = CustomCape.id("textures/cape/" + id + ".png");
      return new ResourceTexture(CustomCape.id("cape/" + id), path);
   }

   public Optional<Texture> resolveTexture(String id) {
      if (id == null || "vanilla".equals(id) || "none".equals(id)) {
         return Optional.empty();
      } else if (id.startsWith("custom:")) {
         return this.getCustomTexture(id);
      } else {
         return CapeCatalog.official(id).isPresent() ? Optional.of(this.getOfficialTexture(id)) : Optional.empty();
      }
   }

   public record ImportResult(boolean success, String id, String displayName, String message) {
      static CapeTextureManager.ImportResult ok(String id, String displayName) {
         return new CapeTextureManager.ImportResult(true, id, displayName, "Uploaded " + displayName);
      }

      static CapeTextureManager.ImportResult fail(String message) {
         return new CapeTextureManager.ImportResult(false, null, null, message);
      }
   }
}
