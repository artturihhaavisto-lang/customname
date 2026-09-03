package dev.customname.gui;

import dev.customname.config.NameConfig;
import dev.customname.config.RankPresets;
import dev.customname.util.ColorCodes;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class PrefixPickList extends ObjectSelectionList<PrefixPickList.Entry> {
   private final NameConfig draft;
   private final PrefixPickList.Listener listener;

   public PrefixPickList(Minecraft minecraft, int width, int height, int y, NameConfig draft, PrefixPickList.Listener listener) {
      super(minecraft, width, height, y, 18);
      this.centerListVertically = false;
      this.draft = draft;
      this.listener = listener;
      this.addHeader("ranks");
      this.addEntry(this.selectIf(new PrefixPickList.Entry(PrefixPickList.Kind.NONE, "none", Component.literal("none").withStyle(muted()), null, null)));

      for (RankPresets.RankPreset preset : RankPresets.all().values()) {
         this.addEntry(this.selectIf(new PrefixPickList.Entry(PrefixPickList.Kind.RANK, preset.id(), ColorCodes.parse(preset.format()), preset, null)));
      }

      this.addHeader("saved");
      if (draft.savedPrefixes != null && !draft.savedPrefixes.isEmpty()) {
         for (NameConfig.SavedPrefix saved : draft.savedPrefixes) {
            this.addEntry(this.selectIf(new PrefixPickList.Entry(PrefixPickList.Kind.SAVED, NameConfig.savedPresetId(saved.id), preview(saved), null, saved)));
         }
      } else {
         this.addEntry(
            new PrefixPickList.Entry(PrefixPickList.Kind.HINT, "_empty", Component.literal("save a prefix to pin it").withStyle(muted()), null, null)
         );
      }
   }

   private PrefixPickList.Entry selectIf(PrefixPickList.Entry entry) {
      if (entry.matches(this.draft.presetId)) {
         this.setSelected(entry);
      }

      return entry;
   }

   private void addHeader(String label) {
      this.addEntry(new PrefixPickList.Entry(PrefixPickList.Kind.HEADER, "_hdr_" + label, Component.literal(label).withStyle(muted()), null, null));
   }

   private static Style muted() {
      return Style.EMPTY.withColor(TextColor.fromRgb(9345192));
   }

   static Component preview(NameConfig.SavedPrefix saved) {
      return saved.chroma ? ColorCodes.chroma(ColorCodes.strip(saved.prefix), saved.chromaEnd, saved.chromaStart) : ColorCodes.parse(saved.prefix);
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

   protected void extractSelection(GuiGraphicsExtractor graphics, PrefixPickList.Entry entry, int color) {
      if (entry.kind.selectable) {
         graphics.fill(entry.getX(), entry.getY(), entry.getX() + entry.getWidth(), entry.getY() + entry.getHeight(), 587202559);
      }
   }

   public class Entry extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<PrefixPickList.Entry> {
      private final PrefixPickList.Kind kind;
      private final String id;
      private final Component label;
      private final RankPresets.RankPreset rank;
      private final NameConfig.SavedPrefix saved;

      Entry(PrefixPickList.Kind kind, String id, Component label, RankPresets.RankPreset rank, NameConfig.SavedPrefix saved) {
         Objects.requireNonNull(PrefixPickList.this);
         super();
         this.kind = kind;
         this.id = id;
         this.label = label;
         this.rank = rank;
         this.saved = saved;
      }

      boolean matches(String presetId) {
         return this.kind != PrefixPickList.Kind.NONE ? this.id.equals(presetId) : presetId == null || presetId.isBlank();
      }

      public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
         int color = this.kind.selectable ? (!hovered && !this.isFocused() ? -3090980 : -526086) : -9537660;
         int textX = this.getContentX() + 4;
         if (this.kind == PrefixPickList.Kind.SAVED) {
            graphics.text(Minecraft.getInstance().font, this.label, textX, this.getContentYMiddle() - 4, -1, false);
            boolean overDelete = this.overDelete(mouseX, mouseY);
            graphics.text(
               Minecraft.getInstance().font, "\u00d7", this.getContentRight() - 10, this.getContentYMiddle() - 4, overDelete ? -526086 : -9537660, false
            );
         } else if (this.kind == PrefixPickList.Kind.RANK) {
            graphics.text(Minecraft.getInstance().font, this.label, textX, this.getContentYMiddle() - 4, -1, false);
         } else {
            graphics.text(Minecraft.getInstance().font, this.label, textX, this.getContentYMiddle() - 4, color, false);
         }
      }

      private boolean overDelete(double mouseX, double mouseY) {
         return this.kind == PrefixPickList.Kind.SAVED
            && mouseX >= this.getContentRight() - 12
            && mouseX < this.getContentRight()
            && mouseY >= this.getY()
            && mouseY < this.getY() + this.getHeight();
      }

      public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
         if (!this.kind.selectable) {
            return false;
         } else if (this.overDelete(event.x(), event.y())) {
            PrefixPickList.this.listener.onDeleteSaved(this.saved);
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            return true;
         } else {
            PrefixPickList.this.setSelected(this);
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            switch (this.kind) {
               case NONE:
                  PrefixPickList.this.listener.onSelectNone();
                  break;
               case RANK:
                  PrefixPickList.this.listener.onSelectRank(this.rank);
                  break;
               case SAVED:
                  PrefixPickList.this.listener.onSelectSaved(this.saved);
            }

            return true;
         }
      }

      public Component getNarration() {
         return this.label;
      }
   }

   public static enum Kind {
      HEADER(false),
      HINT(false),
      NONE(true),
      RANK(true),
      SAVED(true);

      final boolean selectable;

      private Kind(boolean selectable) {
         this.selectable = selectable;
      }
   }

   public interface Listener {
      void onSelectNone();

      void onSelectRank(RankPresets.RankPreset var1);

      void onSelectSaved(NameConfig.SavedPrefix var1);

      void onDeleteSaved(NameConfig.SavedPrefix var1);
   }
}
