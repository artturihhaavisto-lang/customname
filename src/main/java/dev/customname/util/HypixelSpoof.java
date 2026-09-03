package dev.customname.util;

import dev.customname.config.NameConfig;
import dev.customname.engine.Segments;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class HypixelSpoof {
   private static final Pattern PURSE = Pattern.compile("(?i)((?:purse|piggy)\\s*:\\s*)([0-9][0-9,]*(?:\\.[0-9]+)?\\s*[kmb]?)");
   private static final Pattern COINS = Pattern.compile("(?i)([0-9][0-9,]*(?:\\.[0-9]+)?)(\\s+coins)");
   private static final Pattern LOBBY_LABEL = Pattern.compile("(?i)(lobby(?:\\s*#\\s*|\\s+))(\\d+)");
   private static final Pattern SERVER_TOKEN = Pattern.compile("(?i)\\b(?:mini|mega|lobby|limbo|prototype|instance)\\d+[A-Za-z]?\\b|\\bm\\d+[A-Za-z]?\\b");
   private static final Pattern AMOUNT = Pattern.compile("(?i)^\\$?\\s*([0-9]*\\.?[0-9]+)\\s*([kmb])?$");
   private static final DecimalFormat COIN_FORMAT;

   private HypixelSpoof() {
   }

   public static boolean active() {
      NameConfig var0 = NameConfig.get();
      return var0.spoofPurse || var0.spoofLobby;
   }

   public static boolean inSidebar() {
      return active();
   }

   public static Component rewriteSidebar(Component var0) {
      if (var0 != null && active()) {
         Component var1 = var0;
         NameConfig var2 = NameConfig.get();
         if (var2.spoofPurse) {
            var1 = rewritePurse(var0, var2);
         }

         if (var2.spoofLobby) {
            var1 = rewriteLobby(var1, var2);
         }

         return var1;
      } else {
         return var0;
      }
   }

   public static MutableComponent rewriteSidebar(MutableComponent var0) {
      Component var1 = rewriteSidebar((Component)var0);
      if (var1 == var0) {
         return var0;
      } else {
         return var1 instanceof MutableComponent var2 ? var2 : Component.empty().append(var1);
      }
   }

   public static Component rewriteOverlay(Component var0) {
      if (var0 != null && NameConfig.get().spoofPurse) {
         NameConfig var1 = NameConfig.get();
         // The SkyBlock action bar shows the purse as "Purse: N" / "Piggy: N";
         // "N coins" only appears in bazaar/auction style lines. Try both.
         Component var2 = rewritePurse(var0, var1);
         return rewriteCoins(var2, var1);
      } else {
         return var0;
      }
   }

   public static Component rewriteTab(Component var0) {
      return var0 != null && NameConfig.get().spoofLobby ? rewriteLobby(var0, NameConfig.get()) : var0;
   }

   public static String formattedPurse(NameConfig var0) {
      return formatAmount(var0.spoofPurseAmount);
   }

   public static String serverToken(NameConfig var0) {
      String var1 = var0.spoofLobbyId == null ? "" : var0.spoofLobbyId.trim();
      if (var1.isEmpty()) {
         return "lobby1";
      } else {
         return var1.matches("\\d+") ? "lobby" + var1 : var1;
      }
   }

   public static String lobbyNumber(NameConfig var0) {
      Matcher var1 = Pattern.compile("(\\d+)").matcher(serverToken(var0));
      return var1.find() ? var1.group(1) : "1";
   }

   private static Component rewritePurse(Component var0, NameConfig var1) {
      String var2 = formattedPurse(var1);
      return Segments.replaceAll(
         var0, PURSE, var1x -> Component.literal(var1x.group(1)).withStyle(ChatFormatting.WHITE).append(Component.literal(var2).withStyle(ChatFormatting.GOLD))
      );
   }

   private static Component rewriteCoins(Component var0, NameConfig var1) {
      String var2 = formattedPurse(var1);
      return Segments.replaceAll(
         var0, COINS, var1x -> Component.literal(var2).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)).append(Component.literal(var1x.group(2)))
      );
   }

   private static Component rewriteLobby(Component var0, NameConfig var1) {
      String var2 = serverToken(var1);
      String var3 = lobbyNumber(var1);
      Component var4 = Segments.replaceAll(var0, LOBBY_LABEL, var1x -> Component.literal(var1x.group(1)).append(Component.literal(var3)));
      return Segments.replaceAll(
         var4,
         SERVER_TOKEN,
         var1x -> var2.equalsIgnoreCase(var1x.group()) ? Component.literal(var1x.group()) : Component.literal(var2).withStyle(ChatFormatting.DARK_GRAY)
      );
   }

   static String formatAmount(String var0) {
      if (var0 == null) {
         return "0";
      } else {
         String var1 = var0.trim();
         if (var1.isEmpty()) {
            return "0";
         } else {
            String var2 = var1.replace(",", "").replace("_", "").replace(" ", "");
            Matcher var3 = AMOUNT.matcher(var2);
            if (!var3.matches()) {
               return var1.length() > 24 ? var1.substring(0, 24) : var1;
            } else {
               double var4;
               try {
                  var4 = Double.parseDouble(var3.group(1));
               } catch (NumberFormatException var7) {
                  return var1;
               }

               String var6 = var3.group(2);
               if (var6 != null) {
                  var4 *= switch (Character.toLowerCase(var6.charAt(0))) {
                     case 'b' -> 1.0E9;
                     case 'k' -> 1000.0;
                     case 'm' -> 1000000.0;
                     default -> 1.0;
                  };
               }

               if (var4 < 0.0) {
                  var4 = 0.0;
               }

               return var4 > 9.999999999999E12 ? "9,999,999,999,999" : COIN_FORMAT.format(var4);
            }
         }
      }
   }

   static {
      DecimalFormatSymbols var0 = DecimalFormatSymbols.getInstance(Locale.US);
      COIN_FORMAT = new DecimalFormat("#,##0.##", var0);
      COIN_FORMAT.setGroupingUsed(true);
      COIN_FORMAT.setMaximumFractionDigits(1);
   }
}
