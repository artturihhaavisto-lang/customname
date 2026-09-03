package dev.customcape.mixin;

import dev.customcape.CapeManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
   public AbstractClientPlayerMixin() {
   }

   @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
   private void customcape$overrideCape(CallbackInfoReturnable<PlayerSkin> cir) {
      CapeManager manager = CapeManager.get();
      if (manager != null) {
         AbstractClientPlayer player = (AbstractClientPlayer)(Object)this;
         PlayerSkin overridden = manager.overrideSkin(player, (PlayerSkin)cir.getReturnValue());
         if (overridden != null) {
            cir.setReturnValue(overridden);
         }
      }
   }
}
