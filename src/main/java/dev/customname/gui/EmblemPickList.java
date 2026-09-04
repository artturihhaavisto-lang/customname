package dev.customname.gui;

import dev.customname.config.EmblemPresets;
import dev.customname.config.NameConfig;
import dev.customname.util.ColorCodes;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Scrollable picker of SkyBlock emblems, grouped by category, exactly like the
 * rank picker. Selecting an emblem sets the draft's {@code emblem} (color-coded
 * glyph) and {@code emblemId} (preset id); selecting "none" clears both.
 */
public class EmblemPickList extends ObjectSelectionList<EmblemPickList.Entry> {
   private final NameConfig draft;
   private final EmblemPickList.Listener listener;

   public EmblemPickList(Minecraft minecraft, int width, int height, int y, NameConfig draft, EmblemPickList.Listener listener) {
      super(minecraft, width, height, y, 18);
      this.centerListVertically = false;
      this.draft = draft;
      this.listener = listener;
      this.addEntry(this.selectIf(new EmblemPickList.Entry(EmblemPickList.Kind.NONE, "none", Component.literal("none").withStyle(muted()), null)));

      for (String category : EmblemPresets.categories()) {
         this.addHeader(category);
         for (EmblemPresets.EmblemPreset preset : EmblemPresets.inCategory(category)) {
            this.addEntry(this.selectIf(new EmblemPickList.Entry(EmblemPickList.Kind.EMBLEM, preset.id(), label(preset), preset)));
         }
      }
   }

   private EmblemPickList.Entry selectIf(EmblemPickList.Entry entry) {
      if (entry.matches(this.draft.emblemId)) {
         this.setSelected(entry);
      }

      return entry;
   }

   private void addHeader(String label) {
      this.addEntry(new EmblemPickList.Entry(EmblemPickList.Kind.HEADER, "_hdr_" + label, Component.literal(label).withStyle(muted()), null));
   }

   private static Style muted() {
      return Style.EMPTY.withColor(TextColor.fromRgb(9345192));
   }

   /** Colored glyph followed by the emblem name, for a single-line row. */
   static Component label(EmblemPresets.EmblemPreset preset) {
      MutableComponent out = ColorCodes.parse(preset.format());
      out.append(Component.literal("  ").withStyle(muted()));
      out.append(Component.literal(preset.label()).withStyle(muted()));
      return out;
   }

   public int getRowWidth() {
      return Math.max(40, this.getWidth() - this.scrollbarWidth() - 8);
   }

   protected int scrollBarX() {
      return this.getRight() - this.scrollbarWidth();
   }

   protected void extractListBackground(GuiGraphicsExtractor graphics) {
      graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 671088640);
   }

   protected void extractListSeparators(GuiGraphicsExtractor graphics) {
   }

   protected void extractSelection(GuiGraphicsExtractor graphics, EmblemPickList.Entry entry, int color) {
      if (entry.kind.selectable) {
         graphics.fill(entry.getX(), entry.getY(), entry.getX() + entry.getWidth(), entry.getY() + entry.getHeight(), 587202559);
      }
   }

   public class Entry extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<EmblemPickList.Entry> {
      private final EmblemPickList.Kind kind;
      private final String id;
      private final Component label;
      private final EmblemPresets.EmblemPreset emblem;

      Entry(EmblemPickList.Kind kind, String id, Component label, EmblemPresets.EmblemPreset emblem) {
         Objects.requireNonNull(EmblemPickList.this);
         super();
         this.kind = kind;
         this.id = id;
         this.label = label;
         this.emblem = emblem;
      }

      boolean matches(String selectedId) {
         return this.kind != EmblemPickList.Kind.NONE ? this.id.equals(selectedId) : selectedId == null || selectedId.isBlank();
      }

      public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
         int textX = this.getContentX() + 4;
         graphics.text(Minecraft.getInstance().font, this.label, textX, this.getContentYMiddle() - 4, -1, false);
      }

      public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
         if (!this.kind.selectable) {
            return false;
         }

         EmblemPickList.this.setSelected(this);
         if (this.kind == EmblemPickList.Kind.NONE) {
            EmblemPickList.this.listener.onSelectNone();
         } else if (this.kind == EmblemPickList.Kind.EMBLEM) {
            EmblemPickList.this.listener.onSelectEmblem(this.emblem);
         }

         return true;
      }

      public Component getNarration() {
         return this.label;
      }
   }

   public static enum Kind {
      HEADER(false),
      NONE(true),
      EMBLEM(true);

      final boolean selectable;

      private Kind(boolean selectable) {
         this.selectable = selectable;
      }
   }

   public interface Listener {
      void onSelectNone();

      void onSelectEmblem(EmblemPresets.EmblemPreset var1);
   }
}
