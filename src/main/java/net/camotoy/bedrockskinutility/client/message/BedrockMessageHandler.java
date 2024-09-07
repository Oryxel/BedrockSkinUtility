package net.camotoy.bedrockskinutility.client.message;

import com.mojang.blaze3d.platform.NativeImage;
import net.camotoy.bedrockskinutility.client.*;
import net.camotoy.bedrockskinutility.client.mixin.PlayerEntityRendererChangeModel;
import net.camotoy.bedrockskinutility.client.message.data.BaseSkinInfo;
import net.camotoy.bedrockskinutility.client.message.data.CapeData;
import net.camotoy.bedrockskinutility.client.message.data.SkinData;
import net.camotoy.bedrockskinutility.client.data.CustomModelData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;

public final class BedrockMessageHandler {
    private final Logger logger;
    private final SkinManager skinManager;

    public BedrockMessageHandler(Logger logger, SkinManager skinManager) {
        this.logger = logger;
        this.skinManager = skinManager;
    }

    public void handle(CapeData payload, ClientPlayNetworking.Context context) {
        NativeImage capeImage = toNativeImage(payload.capeData(), payload.width(), payload.height());

        context.client().getTextureManager().register(payload.identifier(), new DynamicTexture(capeImage));
        this.skinManager.getCapeData().put(payload.playerUuid(), payload.identifier());
    }

    public void handle(BaseSkinInfo payload) {
        if (payload != null) {
            skinManager.getSkinInfo().put(payload.playerUuid(), new SkinInfo(payload.skinWidth(), payload.skinHeight(), payload.geometry(), payload.chunkCount()));
        }
    }

    public void handle(SkinData payload, ClientPlayNetworking.Context context) {
        if (payload != null) {
            SkinInfo info = skinManager.getSkinInfo().get(payload.playerUuid());
            if (info == null) {
                this.logger.error("Skin info was null!!!");
                return;
            }
            info.setData(payload.skinData(), payload.chunkPosition());
            this.logger.info("Skin chunk {} received for {}", payload.chunkPosition(), payload.playerUuid());

            if (info.isComplete()) {
                // All skin data has been received
                skinManager.getSkinInfo().remove(payload.playerUuid());
            } else {
                return;
            }

            NativeImage skinImage = toNativeImage(info.getData(), info.getWidth(), info.getHeight());

            PlayerRenderer renderer;
            Minecraft client = context.client();

            ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath("geyserskinmanager", payload.playerUuid().toString());
            client.getTextureManager().register(identifier, new DynamicTexture(skinImage));

            boolean isValid = info.getGeometryRaw() != null && !info.getGeometryRaw().isEmpty();

            if (isValid) {
                // Convert Bedrock JSON geometry into a class format that Java understands
                BedrockPlayerEntityModel<AbstractClientPlayer> model = GeometryUtil.bedrockGeoToJava(info);
                if (model != null) {
                    EntityRendererProvider.Context entityContext = new EntityRendererProvider.Context(client.getEntityRenderDispatcher(),
                            client.getItemRenderer(), client.getBlockRenderer(), client.getEntityRenderDispatcher().getItemInHandRenderer(),
                            client.getResourceManager(), client.getEntityModels(), client.font);
                    renderer = new PlayerRenderer(entityContext, false);
                    ((PlayerEntityRendererChangeModel) renderer).bedrockskinutility$setModel(model);

                    CustomModelData custom = new CustomModelData(renderer, identifier);
                    SkinManager.getInstance().getModelData().put(payload.playerUuid(), custom);
                }
            }
        }
    }

    private NativeImage toNativeImage(byte[] data, int width, int height) {
        BufferedImage bufferedImage = SkinUtils.toBufferedImage(data, width, height);

        NativeImage nativeImage = new NativeImage(width, height, true);
        for (int currentWidth = 0; currentWidth < width; currentWidth++) {
            for (int currentHeight = 0; currentHeight < height; currentHeight++) {
                int rgba = bufferedImage.getRGB(currentWidth, currentHeight);
                nativeImage.setPixelRGBA(currentWidth, currentHeight, FastColor.ARGB32.color(
                        (rgba >> 24) & 0xFF, rgba & 0xFF, (rgba >> 8) & 0xFF, (rgba >> 16) & 0xFF));
            }
        }
        return nativeImage;
    }
}
