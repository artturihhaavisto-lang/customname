package dev.customname.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class SkyblockLevels {
   private static final int BRACKET_COLOR = 0x555555;

   private SkyblockLevels() {
   }

   public static Component buildLevelTag(int level) {
      String digits = Integer.toString(Math.max(0, level));
      MutableComponent tag = Component.empty();
      Style bracket = Style.EMPTY.withColor(TextColor.fromRgb(BRACKET_COLOR));
      tag.append(Component.literal("[").withStyle(bracket));
      tag.append(Component.literal(digits).withStyle(Style.EMPTY.withColor(levelNumberColor(level))));
      tag.append(Component.literal("]").withStyle(bracket));
      return tag;
   }

   public static TextColor levelNumberColor(int level) {
      if (level >= 480) {
         return TextColor.fromRgb(0xAA0000);
      } else if (level >= 440) {
         return TextColor.fromRgb(0xFF5555);
      } else if (level >= 400) {
         return TextColor.fromRgb(0xFFAA00);
      } else if (level >= 360) {
         return TextColor.fromRgb(0xAA00AA);
      } else if (level >= 320) {
         return TextColor.fromRgb(0xFF55FF);
      } else if (level >= 280) {
         return TextColor.fromRgb(0x5555FF);
      } else if (level >= 240) {
         return TextColor.fromRgb(0x55FFFF);
      } else if (level >= 200) {
         return TextColor.fromRgb(0x55FFFF);
      } else if (level >= 160) {
         return TextColor.fromRgb(0x00AA00);
      } else if (level >= 120) {
         return TextColor.fromRgb(0x55FF55);
      } else if (level >= 80) {
         return TextColor.fromRgb(0xFFFF55);
      } else if (level >= 40) {
         return TextColor.fromRgb(0xFFFFFF);
      } else {
         return TextColor.fromRgb(0xAAAAAA);
      }
   }
}
