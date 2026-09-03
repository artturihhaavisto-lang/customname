package dev.customname.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class ColorCodes {
   private static final Pattern CODE_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})|[&\u00a7]([0-9a-fk-orA-FK-OR])");

   private ColorCodes() {
   }

   public static MutableComponent parse(String input) {
      if (input != null && !input.isEmpty()) {
         MutableComponent result = Component.empty();
         Matcher matcher = CODE_PATTERN.matcher(input);
         int lastEnd = 0;

         Style currentStyle;
         for (currentStyle = Style.EMPTY; matcher.find(); lastEnd = matcher.end()) {
            if (matcher.start() > lastEnd) {
               result.append(Component.literal(input.substring(lastEnd, matcher.start())).withStyle(currentStyle));
            }

            if (matcher.group(1) != null) {
               int rgb = Integer.parseInt(matcher.group(1), 16);
               currentStyle = currentStyle.withColor(TextColor.fromRgb(rgb));
            } else {
               char code = Character.toLowerCase(matcher.group(2).charAt(0));
               currentStyle = applyLegacy(currentStyle, code);
            }
         }

         if (lastEnd < input.length()) {
            result.append(Component.literal(input.substring(lastEnd)).withStyle(currentStyle));
         }

         return result;
      } else {
         return Component.empty();
      }
   }

   public static MutableComponent coloredLiteral(String text, String colorCode) {
      if (text == null || text.isEmpty()) {
         return Component.empty();
      } else if (hasColorCodes(text)) {
         return parse(text);
      } else {
         TextColor color = resolveTextColor(colorCode);
         MutableComponent literal = Component.literal(text);
         return color != null ? literal.withStyle(Style.EMPTY.withColor(color)) : literal;
      }
   }

   public static MutableComponent chroma(String text, String startCode, String endCode) {
      if (text != null && !text.isEmpty()) {
         TextColor start = resolveTextColor(startCode);
         TextColor end = resolveTextColor(endCode);
         int startRgb = start != null ? start.getValue() : 16733525;
         int endRgb = end != null ? end.getValue() : 5636095;
         int length = Math.max(1, text.codePointCount(0, text.length()));
         double cycleChars = Math.max(18.0, length * 2.75);
         double phase = System.currentTimeMillis() % 4500L / 4500.0;
         MutableComponent result = Component.empty();
         int position = 0;

         for (int offset = 0; offset < text.length(); position++) {
            int codePoint = text.codePointAt(offset);
            double t = phase + position / cycleChars;
            double amount = 0.5 - 0.5 * Math.cos(t * Math.PI * 2.0);
            int rgb = interpolate(startRgb, endRgb, amount);
            result.append(Component.literal(new String(Character.toChars(codePoint))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
            offset += Character.charCount(codePoint);
         }

         return result;
      } else {
         return Component.empty();
      }
   }

   private static int interpolate(int start, int end, double amount) {
      amount = Math.max(0.0, Math.min(1.0, amount));
      float[] a = rgbToHsl(start);
      float[] b = rgbToHsl(end);
      boolean dull = a[1] < 0.08F || b[1] < 0.08F || a[2] < 0.05F || b[2] < 0.05F;
      if (dull) {
         return lerpLinearRgb(start, end, amount);
      } else {
         float dh = b[0] - a[0];
         if (dh > 0.5F) {
            dh--;
         }

         if (dh < -0.5F) {
            dh++;
         }

         float h = a[0] + dh * (float)amount;
         if (h < 0.0F) {
            h++;
         }

         if (h > 1.0F) {
            h--;
         }

         float s = a[1] + (b[1] - a[1]) * (float)amount;
         float l = a[2] + (b[2] - a[2]) * (float)amount;
         return hslToRgb(h, s, l);
      }
   }

   private static int lerpLinearRgb(int start, int end, double amount) {
      int red = lerpGamma(start >> 16 & 0xFF, end >> 16 & 0xFF, amount);
      int green = lerpGamma(start >> 8 & 0xFF, end >> 8 & 0xFF, amount);
      int blue = lerpGamma(start & 0xFF, end & 0xFF, amount);
      return red << 16 | green << 8 | blue;
   }

   private static int lerpGamma(int from, int to, double amount) {
      double a = Math.pow(from / 255.0, 2.2);
      double b = Math.pow(to / 255.0, 2.2);
      double value = Math.pow(a + (b - a) * amount, 0.45454545454545453);
      return (int)Math.round(Math.max(0.0, Math.min(255.0, value * 255.0)));
   }

   private static float[] rgbToHsl(int rgb) {
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float l = (max + min) / 2.0F;
      if (max == min) {
         return new float[]{0.0F, 0.0F, l};
      } else {
         float d = max - min;
         float s = l > 0.5F ? d / (2.0F - max - min) : d / (max + min);
         float h;
         if (max == r) {
            h = (g - b) / d + (g < b ? 6.0F : 0.0F);
         } else if (max == g) {
            h = (b - r) / d + 2.0F;
         } else {
            h = (r - g) / d + 4.0F;
         }

         return new float[]{h / 6.0F, s, l};
      }
   }

   private static int hslToRgb(float h, float s, float l) {
      float r;
      float g;
      float b;
      if (s == 0.0F) {
         b = l;
         g = l;
         r = l;
      } else {
         float q = l < 0.5F ? l * (1.0F + s) : l + s - l * s;
         float p = 2.0F * l - q;
         r = hueToRgb(p, q, h + 0.33333334F);
         g = hueToRgb(p, q, h);
         b = hueToRgb(p, q, h - 0.33333334F);
      }

      return Math.round(r * 255.0F) << 16 | Math.round(g * 255.0F) << 8 | Math.round(b * 255.0F);
   }

   private static float hueToRgb(float p, float q, float t) {
      if (t < 0.0F) {
         t++;
      }

      if (t > 1.0F) {
         t--;
      }

      if (t < 0.16666667F) {
         return p + (q - p) * 6.0F * t;
      } else if (t < 0.5F) {
         return q;
      } else {
         return t < 0.6666667F ? p + (q - p) * (0.6666667F - t) * 6.0F : p;
      }
   }

   public static TextColor resolveTextColor(String colorCode) {
      String normalized = normalizeColorCode(colorCode);
      if (normalized.isEmpty()) {
         return null;
      } else if (normalized.startsWith("&#") && normalized.length() >= 8) {
         try {
            return TextColor.fromRgb(Integer.parseInt(normalized.substring(2, 8), 16));
         } catch (NumberFormatException var3) {
            return null;
         }
      } else {
         if (normalized.length() >= 2 && normalized.charAt(0) == '&') {
            char code = Character.toLowerCase(normalized.charAt(1));
            if (code >= '0' && code <= '9' || code >= 'a' && code <= 'f') {
               return TextColor.fromRgb(legacyRgb(code));
            }
         }

         return null;
      }
   }

   public static String strip(String input) {
      return input != null && !input.isEmpty() ? CODE_PATTERN.matcher(input).replaceAll("") : "";
   }

   public static boolean hasColorCodes(String input) {
      return input != null && !input.isEmpty() && CODE_PATTERN.matcher(input).find();
   }

   public static int legacyRgb(char code) {
      return switch (Character.toLowerCase(code)) {
         case '0' -> 0;
         case '1' -> 170;
         case '2' -> 43520;
         case '3' -> 43690;
         case '4' -> 11141120;
         case '5' -> 11141290;
         case '6' -> 16755200;
         case '7' -> 11184810;
         case '8' -> 5592405;
         case '9' -> 5592575;
         default -> 16777215;
         case 'a' -> 5635925;
         case 'b' -> 5636095;
         case 'c' -> 16733525;
         case 'd' -> 16733695;
         case 'e' -> 16777045;
         case 'f' -> 16777215;
      };
   }

   public static String normalizeColorCode(String raw) {
      if (raw == null) {
         return "";
      } else {
         String trimmed = raw.trim();
         if (trimmed.isEmpty()) {
            return "";
         } else if (trimmed.startsWith("&#") && trimmed.length() >= 8) {
            return "&#" + trimmed.substring(2, 8);
         } else if (trimmed.startsWith("#") && trimmed.length() >= 7) {
            return "&#" + trimmed.substring(1, 7);
         } else if (trimmed.length() == 6 && trimmed.matches("[0-9A-Fa-f]{6}")) {
            return "&#" + trimmed;
         } else if (trimmed.startsWith("&") && trimmed.length() >= 2) {
            return "&" + Character.toLowerCase(trimmed.charAt(1));
         } else {
            return trimmed.length() == 1 ? "&" + Character.toLowerCase(trimmed.charAt(0)) : "";
         }
      }
   }

   private static Style applyLegacy(Style style, char code) {
      return switch (code) {
         case '0' -> style.withColor(TextColor.fromRgb(0));
         case '1' -> style.withColor(TextColor.fromRgb(170));
         case '2' -> style.withColor(TextColor.fromRgb(43520));
         case '3' -> style.withColor(TextColor.fromRgb(43690));
         case '4' -> style.withColor(TextColor.fromRgb(11141120));
         case '5' -> style.withColor(TextColor.fromRgb(11141290));
         case '6' -> style.withColor(TextColor.fromRgb(16755200));
         case '7' -> style.withColor(TextColor.fromRgb(11184810));
         case '8' -> style.withColor(TextColor.fromRgb(5592405));
         case '9' -> style.withColor(TextColor.fromRgb(5592575));
         default -> style;
         case 'a' -> style.withColor(TextColor.fromRgb(5635925));
         case 'b' -> style.withColor(TextColor.fromRgb(5636095));
         case 'c' -> style.withColor(TextColor.fromRgb(16733525));
         case 'd' -> style.withColor(TextColor.fromRgb(16733695));
         case 'e' -> style.withColor(TextColor.fromRgb(16777045));
         case 'f' -> style.withColor(TextColor.fromRgb(16777215));
         case 'k' -> style.withObfuscated(true);
         case 'l' -> style.withBold(true);
         case 'm' -> style.withStrikethrough(true);
         case 'n' -> style.withUnderlined(true);
         case 'o' -> style.withItalic(true);
         case 'r' -> Style.EMPTY;
      };
   }
}
