package dev.customname.mixin;

import dev.customcape.CapeManager;
import dev.customname.util.NameTagFromTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
   public AvatarRendererMixin() {
   }

   @Inject(
      method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
      at = @At("RETURN")
   )
   private void customname$updateNameTag(Avatar entity, AvatarRenderState state, float tickProgress, CallbackInfo ci) {
      if (entity instanceof AbstractClientPlayer player) {
         CapeManager capes = CapeManager.get();
         if (capes != null) {
            PlayerSkin overridden = capes.overrideSkin(player, state.skin);
            if (overridden != null) {
               state.skin = overridden;
               if (overridden.cape() != null) {
                  state.showCape = true;
               }
            }
         }

         if (NameTagFromTab.isHidden(player)) {
            state.nameTag = null;
            state.scoreText = null;
         } else {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isLocalPlayer(player.getUUID()) || !mc.options.getCameraType().isFirstPerson()) {
               Component tag = NameTagFromTab.resolve(player);
               if (tag != null) {
                  state.nameTag = tag;
                  if (state.nameTagAttachment == null) {
                     state.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(tickProgress));
                  }
               } else if (mc.isLocalPlayer(player.getUUID())) {
                  state.nameTag = player.getDisplayName();
               }
            }
         }
      }
   }

   @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
   private void customname$hidePlayerNameTags(Avatar entity, double distance, CallbackInfoReturnable<Boolean> cir) {
      if (entity instanceof AbstractClientPlayer player && NameTagFromTab.isHidden(player)) {
         cir.setReturnValue(false);
      }
   }
}
