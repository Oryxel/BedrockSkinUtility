package net.camotoy.bedrockskinutility.client.message.data;

import net.camotoy.bedrockskinutility.client.message.BedrockMessageHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;

import java.util.UUID;

public record BaseSkinInfo(UUID playerUuid, int skinWidth, int skinHeight, String geometry, int chunkCount) implements BedrockData {
    public static final StreamDecoder<FriendlyByteBuf, BaseSkinInfo> STREAM_DECODER = buf -> {
        int version = buf.readInt();
        if (version != 1) { // Version 2 is probably going to be reserved for persona skins
            throw new RuntimeException("Could not load skin info! Is the mod and plugin updated?");
        }

        UUID playerUuid = new UUID(buf.readLong(), buf.readLong());

        int skinWidth = buf.readInt();
        int skinHeight = buf.readInt();

        String geometry = null;

        if (buf.readBoolean()) { // is geometry present
            try {
                geometry = BedrockData.readString(buf);
                BedrockData.readString(buf);
            } catch (Exception e) {
            }
        }

        int chunkCount = buf.readInt();

        return new BaseSkinInfo(playerUuid, skinWidth, skinHeight, geometry, chunkCount);
    };

    @Override
    public void handle(ClientPlayNetworking.Context context, BedrockMessageHandler handler) {
        handler.handle(this);
    }
}
