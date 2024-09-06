package net.camotoy.bedrockskinutility.client.mixin;

import net.camotoy.bedrockskinutility.client.SkinManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends EntityMixin {

    @Shadow private PlayerInfo playerInfo;

    @Inject(method = "getSkin", at = @At(value = "TAIL"), cancellable = true)
    public void injectGetSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        ResourceLocation cape = SkinManager.getInstance().getCapeData().get(getUUID());
        if (cape != null) {
            PlayerSkin skin = playerInfo == null ? DefaultPlayerSkin.get(this.getUUID()) : playerInfo.getSkin();
            cir.setReturnValue(new PlayerSkin(
                    skin.texture(),
                    skin.textureUrl(),
                    cape,
                    skin.elytraTexture(),
                    skin.model(),
                    skin.secure()
            ));
        }
    }

}
