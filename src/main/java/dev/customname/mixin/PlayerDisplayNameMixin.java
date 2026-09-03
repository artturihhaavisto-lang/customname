package dev.customname.mixin;

import dev.customname.util.DisplayNameBuilder;
import dev.customname.config.NameConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerDisplayNameMixin {
   public PlayerDisplayNameMixin() {
   }

   @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
   private void customname$onGetDisplayName(CallbackInfoReturnable<Component> cir) {
      Player self = (Player)(Object)this;
      if (DisplayNameBuilder.appliesTo(self.getUUID()) && NameConfig.get().enabled) {
         cir.setReturnValue(DisplayNameBuilder.build(self.getGameProfile().name()));
      }
   }
}
