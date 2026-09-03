package dev.customname.mixin;

import dev.customname.util.HypixelSpoof;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin {
   public PlayerTeamMixin() {
   }

   @Inject(method = "getFormattedName", at = @At("RETURN"), cancellable = true)
   private void customname$spoofSidebarName(Component name, CallbackInfoReturnable<MutableComponent> cir) {
      if (HypixelSpoof.inSidebar()) {
         MutableComponent rewritten = HypixelSpoof.rewriteSidebar((MutableComponent)cir.getReturnValue());
         if (rewritten != cir.getReturnValue()) {
            cir.setReturnValue(rewritten);
         }
      }
   }
}
