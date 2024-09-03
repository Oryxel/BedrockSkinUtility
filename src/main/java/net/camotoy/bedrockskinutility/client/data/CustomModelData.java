package net.camotoy.bedrockskinutility.client.data;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public record CustomModelData(PlayerRenderer renderer, ResourceLocation location) {
}
