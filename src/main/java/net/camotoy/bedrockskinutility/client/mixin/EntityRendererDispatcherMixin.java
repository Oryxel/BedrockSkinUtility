package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.SkinManager;
import net.camotoy.bedrockskinutility.client.data.CustomModelData;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
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
        CustomModelData skin = SkinManager.getInstance().getModelData().get(entity.getUUID());
        if (skin != null)
            cir.setReturnValue(skin.renderer());
    }
}
