package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.SkinManager;
import net.camotoy.bedrockskinutility.client.skin.CustomModelSkin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;", at = @At(value = "RETURN"), cancellable = true
    )
    public void getTextureLocation(AbstractClientPlayer abstractClientPlayer, CallbackInfoReturnable<ResourceLocation> cir) {
        CustomModelSkin modelSkin = SkinManager.getInstance().getModelMap().get(abstractClientPlayer.getUUID());
        if (modelSkin == null)
            return;

        cir.setReturnValue(modelSkin.location());
    }

}
