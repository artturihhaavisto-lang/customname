package dev.customname.mixin;

import dev.customname.engine.TabNameCache;
import dev.customname.util.TabDisplayRewriter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
   public PlayerTabOverlayMixin() {
   }

   @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
   private void customname$onGetNameForDisplay(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
      Component original = (Component)cir.getReturnValue();
      if (TabDisplayRewriter.appliesTo(entry, original)) {
         Component rewritten = TabDisplayRewriter.rewrite(original);
         if (entry.getGameMode() == GameType.SPECTATOR && rewritten instanceof MutableComponent mutable) {
            rewritten = mutable.copy().withStyle(ChatFormatting.ITALIC);
         }

         TabNameCache.put(entry.getProfile().id(), rewritten);
         cir.setReturnValue(rewritten);
      }
   }
}
