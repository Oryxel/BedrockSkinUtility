package net.camotoy.bedrockskinutility.client;

import net.camotoy.bedrockskinutility.client.data.CustomModelData;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager {
    private static SkinManager instance;

    private final Map<UUID, CustomModelData> modelData = new HashMap<>();
    private final Map<UUID, ResourceLocation> capeData = new HashMap<>();

    private final Map<UUID, SkinInfo> skinInfo = new ConcurrentHashMap<>();

    public SkinManager() {
        instance = this;
    }

    public Map<UUID, SkinInfo> getSkinInfo() {
        return skinInfo;
    }

    public Map<UUID, CustomModelData> getModelData() {
        return modelData;
    }

    public Map<UUID, ResourceLocation> getCapeData() {
        return capeData;
    }

    public static SkinManager getInstance() {
        return instance;
    }

}
