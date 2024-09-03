package net.camotoy.bedrockskinutility.client.skin;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public record CustomModelSkin(PlayerRenderer renderer, ResourceLocation location) {
}
