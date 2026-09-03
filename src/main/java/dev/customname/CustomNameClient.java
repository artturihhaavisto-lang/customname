package dev.customname;

import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.customcape.CapeManager;
import dev.customcape.command.CapeCommands;
import dev.customname.command.CustomNameCommands;
import dev.customname.config.NameConfig;
import dev.customname.engine.ChatBridge;
import dev.customname.gui.AppearanceScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomNameClient implements ClientModInitializer {
   public static final String MOD_ID = "customname";
   public static final Logger LOGGER = LoggerFactory.getLogger("customname");
   public static final Category KEY_CATEGORY = Category.register(Identifier.fromNamespaceAndPath("customname", "main"));
   public static KeyMapping openGuiKey;
   public static KeyMapping openCapeKey;
   private static boolean loadedCapeTextures;

   public CustomNameClient() {
   }

   public void onInitializeClient() {
      NameConfig.load();
      CapeManager.init();
      ChatBridge.register();
      CustomNameCommands.register();
      CapeCommands.register();
      openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.customname.open", Type.KEYSYM, 78, KEY_CATEGORY));
      openCapeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.customcape.open", Type.KEYSYM, 75, KEY_CATEGORY));
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (!loadedCapeTextures && client.getTextureManager() != null && CapeManager.get() != null) {
            loadedCapeTextures = true;
            CapeManager.get().textures().initFolders();
            CapeManager.get().reloadCustom();
         }

         while (openGuiKey.consumeClick()) {
            AppearanceScreen.open(AppearanceScreen.Tab.NAME);
         }

         while (openCapeKey.consumeClick()) {
            AppearanceScreen.open(AppearanceScreen.Tab.CAPE);
         }
      });
      LOGGER.info("Custom loaded \u2014 N for name, K for cape");
   }
}
