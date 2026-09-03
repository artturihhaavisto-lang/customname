package dev.customname.gui;

import dev.customcape.CapeCatalog;
import dev.customcape.CapeManager;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CapePickList extends ObjectSelectionList<CapePickList.Entry> {
   public CapePickList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
      super(minecraft, width, height, y, itemHeight);
      this.centerListVertically = false;
      CapeManager manager = CapeManager.get();
      if (manager != null) {
         String selected = manager.config().selectedCape();
         this.addHeader("official");

         for (CapeCatalog.Entry cape : manager.allEntries()) {
            if (cape.kind() != CapeCatalog.Kind.CUSTOM) {
               CapePickList.Entry entry = new CapePickList.Entry(cape, true);
               this.addEntry(entry);
               if (cape.id().equals(selected)) {
                  this.setSelected(entry);
               }
            }
         }

         this.addHeader("custom");
         List<CapeCatalog.Entry> customs = manager.textures().customEntries();
         if (customs.isEmpty()) {
            this.addEntry(new CapePickList.Entry(new CapeCatalog.Entry("_empty", "upload a 64\u00d732 png", CapeCatalog.Kind.CUSTOM), false));
         } else {
            for (CapeCatalog.Entry capex : customs) {
               CapePickList.Entry entry = new CapePickList.Entry(capex, true);
               this.addEntry(entry);
               if (capex.id().equals(selected)) {
                  this.setSelected(entry);
               }
            }
         }
      }
   }

   private void addHeader(String label) {
      this.addEntry(new CapePickList.Entry(new CapeCatalog.Entry("_hdr_" + label, label, CapeCatalog.Kind.OFFICIAL), false));
   }

   public void setSelected(CapePickList.Entry entry) {
      super.setSelected(entry);
      if (entry != null && entry.selectable) {
         CapeManager manager = CapeManager.get();
         if (manager != null) {
            manager.select(entry.cape.id());
         }
      }
   }

   public int getRowWidth() {
      // Leave room so AbstractSelectionList's scrollbar sits inside the widget.
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

   protected void extractSelection(GuiGraphicsExtractor graphics, CapePickList.Entry entry, int color) {
      graphics.fill(entry.getX(), entry.getY(), entry.getX() + entry.getWidth(), entry.getY() + entry.getHeight(), 587202559);
   }

   public class Entry extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<CapePickList.Entry> {
      private final CapeCatalog.Entry cape;
      private final boolean selectable;

      Entry(CapeCatalog.Entry cape, boolean selectable) {
         Objects.requireNonNull(CapePickList.this);
         super();
         this.cape = cape;
         this.selectable = selectable;
      }

      public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
         boolean header = !this.selectable;
         int color = header ? -9537660 : (!hovered && !this.isFocused() ? -3090980 : -526086);
         int textX = this.getContentX() + 4;
         if (this.selectable) {
            CapeManager.get().textures().resolveTexture(this.cape.id()).ifPresent(texture -> {
               Identifier path = texture.texturePath();
               graphics.blit(RenderPipelines.GUI_TEXTURED, path, this.getContentX() + 2, this.getContentY() + 2, 1.0F, 1.0F, 8, 16, 64, 32);
            });
            if (this.cape.kind() == CapeCatalog.Kind.OFFICIAL || this.cape.kind() == CapeCatalog.Kind.CUSTOM) {
               textX += 14;
            }
         }

         graphics.text(Minecraft.getInstance().font, this.cape.displayName(), textX, this.getContentYMiddle() - 4, color, false);
      }

      public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
         if (!this.selectable) {
            return false;
         } else {
            CapePickList.this.setSelected(this);
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            return true;
         }
      }

      public Component getNarration() {
         return Component.literal(this.cape.displayName());
      }
   }
}
