package dev.customcape;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomCape {
   public static final String MOD_ID = "customcape";
   public static final Logger LOGGER = LoggerFactory.getLogger("customcape");
   public static final String VANILLA_ID = "vanilla";
   public static final String NONE_ID = "none";

   private CustomCape() {
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("customcape", path);
   }
}
