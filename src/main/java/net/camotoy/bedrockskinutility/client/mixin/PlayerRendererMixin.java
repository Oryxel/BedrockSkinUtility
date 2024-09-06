package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.SkinManager;
import net.camotoy.bedrockskinutility.client.data.CustomModelData;
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
        CustomModelData modelSkin = SkinManager.getInstance().getModelData().get(abstractClientPlayer.getUUID());
        if (modelSkin != null) {
            cir.setReturnValue(modelSkin.location());
        }
    }

}
