package dev.customname.gui;

import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class GlassButton extends AbstractWidget {
   private static final int IDLE = 352321535;
   private static final int HOVER = 721420287;
   private static final int ON = 872415231;
   private static final int LINE = -655100166;
   private static final int TEXT_ON = -526086;
   private static final int TEXT_OFF = -6642768;
   private final Runnable onPress;
   private final BooleanSupplier selected;
   private final Font font;
   private final boolean preserveColor;

   public GlassButton(int x, int y, int w, int h, Component label, BooleanSupplier selected, Runnable onPress) {
      this(x, y, w, h, label, selected, onPress, false);
   }

   public GlassButton(int x, int y, int w, int h, Component label, BooleanSupplier selected, Runnable onPress, boolean preserveColor) {
      super(x, y, w, h, label);
      this.selected = selected;
      this.onPress = onPress;
      this.font = Minecraft.getInstance().font;
      this.preserveColor = preserveColor;
   }

   public GlassButton tooltip(String text) {
      this.setTooltip(Tooltip.create(Component.literal(text)));
      return this;
   }

   public void onClick(MouseButtonEvent event, boolean doubleClick) {
      this.playDownSound(Minecraft.getInstance().getSoundManager());
      this.onPress.run();
   }

   protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      boolean on = this.selected.getAsBoolean();
      int bg = on ? 872415231 : (this.isHoveredOrFocused() ? 721420287 : 352321535);
      graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), bg);
      if (on) {
         graphics.fill(this.getX() + 6, this.getY() + this.getHeight() - 1, this.getX() + this.getWidth() - 6, this.getY() + this.getHeight(), -655100166);
      }

      if (this.preserveColor) {
         int tx = this.getX() + (this.getWidth() - this.font.width(this.getMessage())) / 2;
         int ty = this.getY() + (this.getHeight() - 8) / 2;
         graphics.text(this.font, this.getMessage(), tx, ty, -1, false);
      } else {
         int color = on ? -526086 : -6642768;
         int tx = this.getX() + (this.getWidth() - this.font.width(this.getMessage())) / 2;
         int ty = this.getY() + (this.getHeight() - 8) / 2;
         graphics.text(this.font, this.getMessage(), tx, ty, color, false);
      }
   }

   protected void updateWidgetNarration(NarrationElementOutput output) {
      this.defaultButtonNarrationText(output);
   }
}
