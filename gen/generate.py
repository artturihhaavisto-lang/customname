# -*- coding: utf-8 -*-
import sys
sys.path.insert(0, 'gen')
from emblems import EMBLEMS

def esc(s):
    out = []
    for ch in s:
        cp = ord(ch)
        if cp < 0x80:
            if cp == 0x5c:
                out.append("\\\\")
            elif cp == 0x22:
                out.append("\\\"")
            else:
                out.append(ch)
        elif cp > 0xFFFF:
            cp -= 0x10000
            hi = 0xD800 + (cp >> 10)
            lo = 0xDC00 + (cp & 0x3FF)
            out.append("\\u%04X\\u%04X" % (hi, lo))
        else:
            out.append("\\u%04X" % cp)
    return "".join(out)

def q(s):
    return '"' + esc(s) + '"'

lines = []
lines.append("package dev.customname.config;")
lines.append("")
lines.append("import java.util.ArrayList;")
lines.append("import java.util.Collections;")
lines.append("import java.util.LinkedHashMap;")
lines.append("import java.util.List;")
lines.append("import java.util.Map;")
lines.append("import java.util.Optional;")
lines.append("")
lines.append("/**")
lines.append(" * SkyBlock emblem presets (dungeon, skill, leveling, slayer, achievement, MVP++, special).")
lines.append(" * Each preset is a color-coded glyph ({@code format}) that can be shown beside the")
lines.append(" * local player's name in chat, the tab list and the name tag.")
lines.append(" */")
lines.append("public final class EmblemPresets {")
lines.append("   private static final Map<String, EmblemPreset> PRESETS = new LinkedHashMap<>();")
lines.append("   private static final List<String> CATEGORIES = new ArrayList<>();")
lines.append("")
lines.append("   private EmblemPresets() {")
lines.append("   }")
lines.append("")
lines.append("   private static void register(String id, String category, String label, String format, String requirement) {")
lines.append("      if (!CATEGORIES.contains(category)) {")
lines.append("         CATEGORIES.add(category);")
lines.append("      }")
lines.append("      PRESETS.put(id, new EmblemPreset(id, category, label, format, requirement));")
lines.append("   }")
lines.append("")
lines.append("   public static Map<String, EmblemPreset> all() {")
lines.append("      return Collections.unmodifiableMap(PRESETS);")
lines.append("   }")
lines.append("")
lines.append("   public static List<String> categories() {")
lines.append("      return Collections.unmodifiableList(CATEGORIES);")
lines.append("   }")
lines.append("")
lines.append("   public static List<EmblemPreset> inCategory(String category) {")
lines.append("      List<EmblemPreset> out = new ArrayList<>();")
lines.append("      for (EmblemPreset preset : PRESETS.values()) {")
lines.append("         if (preset.category().equals(category)) {")
lines.append("            out.add(preset);")
lines.append("         }")
lines.append("      }")
lines.append("      return out;")
lines.append("   }")
lines.append("")
lines.append("   public static Optional<EmblemPreset> get(String id) {")
lines.append("      if (id == null) {")
lines.append("         return Optional.empty();")
lines.append("      } else {")
lines.append("         EmblemPreset exact = PRESETS.get(id.toLowerCase());")
lines.append("         if (exact != null) {")
lines.append("            return Optional.of(exact);")
lines.append("         } else {")
lines.append("            for (EmblemPreset preset : PRESETS.values()) {")
lines.append("               if (preset.id().equalsIgnoreCase(id) || preset.label().equalsIgnoreCase(id)) {")
lines.append("                  return Optional.of(preset);")
lines.append("               }")
lines.append("            }")
lines.append("            return Optional.empty();")
lines.append("         }")
lines.append("      }")
lines.append("   }")
lines.append("")
lines.append("   static {")
for eid, cat, label, fmt, req in EMBLEMS:
    lines.append("      register(%s, %s, %s, %s, %s);" % (q(eid.lower()), q(cat), q(label), q(fmt), q(req)))
lines.append("   }")
lines.append("")
lines.append("   public record EmblemPreset(String id, String category, String label, String format, String requirement) {")
lines.append("   }")
lines.append("}")

dst = "src/main/java/dev/customname/config/EmblemPresets.java"
open(dst, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", dst)
