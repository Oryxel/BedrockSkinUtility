package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.SkinManager;
import net.camotoy.bedrockskinutility.client.interfaces.BedrockPlayerInfo;
import net.camotoy.bedrockskinutility.client.skin.CustomModelSkin;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererDispatcherMixin {

    @Inject(
            method = "getRenderer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSkin()Lnet/minecraft/client/resources/PlayerSkin;"),
            cancellable = true
    )
    public void getRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?>> cir) {
        PlayerInfo playerListEntry = ((BedrockAbstractClientPlayerEntity) entity).bedrockskinutility$getPlayerListEntry();
        PlayerRenderer renderer = null;
        if (playerListEntry != null) {
            renderer = ((BedrockPlayerInfo) playerListEntry).bedrockskinutility$getModel();
        } else {
            CustomModelSkin skin = SkinManager.getInstance().getModelMap().get(entity.getUUID());
            if (skin != null)
                renderer = skin.renderer();
        }

        if (renderer != null) {
            cir.setReturnValue(renderer);
        }
    }
}
