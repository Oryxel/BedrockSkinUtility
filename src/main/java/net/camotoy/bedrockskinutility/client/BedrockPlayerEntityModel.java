package net.camotoy.bedrockskinutility.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class BedrockPlayerEntityModel<T extends LivingEntity> extends PlayerModel<T> {

    public BedrockPlayerEntityModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

}
