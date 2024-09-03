package net.camotoy.bedrockskinutility.client;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.bedrock.model.Bone;
import org.oryxel.cube.model.bedrock.model.Cube;
import org.oryxel.cube.parser.bedrock.BedrockGeometrySerializer;
import org.oryxel.cube.util.ArrayUtil;
import org.oryxel.cube.util.UVUtil;

import java.util.*;

public class GeometryUtil {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    // Copied from CubeListBuilder
    private static final Set<Direction> ALL_VISIBLE = EnumSet.allOf(Direction.class);

    public static BedrockPlayerEntityModel<AbstractClientPlayer> bedrockGeoToJava(SkinInfo info) {
        if (info.getGeometryRaw() == null || info.getGeometryRaw().isEmpty())
            return null;

        BedrockGeometry geometry = null;

        try {
            geometry = BedrockGeometrySerializer.deserialize(info.getGeometryRaw());
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }

        if (geometry == null)
            return null;

        int uvHeight = geometry.textureHeight();
        int uvWidth = geometry.textureWidth();

        final Map<String, Bone> boneMap = new HashMap<>();
        for (Bone bone : geometry.bones()) {
            boneMap.put(bone.name(), bone);
        }

        final Map<String, PartInfo> stringToPart = new HashMap<>();
        for (Bone bone : geometry.bones()) {
            final List<ModelPart.Cube> cuboids = new ArrayList<>();
            float pivotX = (float) bone.pivot()[0], pivotY = (float) bone.pivot()[1], pivotZ = (float) bone.pivot()[2];
            Bone parentBone = null;
            if (!bone.parent().isEmpty())
                parentBone = boneMap.get(bone.parent());
            String parent = bone.parent(), name = bone.name();

            for (Cube cube : bone.cubes()) {
                double[] uv = new double[3];
                if (cube instanceof Cube.PerFaceCube perFaceCube) { // support for perface uv?
                    uv = UVUtil.portToBoxUv(perFaceCube.uvMap(), perFaceCube.origin(),
                            ArrayUtil.combineArray(perFaceCube.origin(), perFaceCube.size()));
                } else if (cube instanceof Cube.BoxCube boxCube) {
                    uv = boxCube.uvOffset();
                }

                float originX = (float) cube.origin()[0], originY = (float) cube.origin()[1], originZ = (float) cube.origin()[2];
                float sizeX = (float) cube.size()[0], sizeY = (float) cube.size()[1], sizeZ = (float) cube.size()[2];
                float inflate = (float) cube.inflate();
                // I didn't use the below, but it may be a helpful reference in the future
                // The Y needs to be inverted, for whatever reason
                // https://github.com/JannisX11/blockbench/blob/8529c0adee8565f8dac4b4583c3473b60679966d/js/transform.js#L148
                cuboids.add(new ModelPart.Cube((int) uv[0], (int) uv[1],
                        (originX - pivotX), (-(originY + sizeY) + pivotY), (originZ - pivotZ),
                        sizeX, sizeY, sizeZ, inflate, inflate, inflate, cube.mirror(), uvHeight, uvWidth, ALL_VISIBLE));
            }

            Map<String, ModelPart> children = new HashMap<>();
            ModelPart part = new ModelPart(cuboids, children);
            // set rotation (if there is one)
            part.setRotation((float) -bone.rotation()[0], (float) -bone.rotation()[1], (float) bone.rotation()[2]);

            if (parentBone != null) {
                // This appears to be a difference between Bedrock and Java - pivots are carried over for us
                part.setPos((float) (pivotX - parentBone.pivot()[0]), (float) (pivotY - parentBone.pivot()[1]), (float) (pivotZ - parentBone.pivot()[2]));
            } else part.setPos(pivotX, pivotY, pivotZ);

            // Please lowercase this, it can be lowercase...
            switch (name.toLowerCase()) { // Also do this with the overlays? Those are final, though.
                case "head", "hat", "rightarm", "body", "leftarm", "leftleg", "rightleg" -> parent = "root";
            }

            name = adjustFormatting(name);

            stringToPart.put(name, new PartInfo(adjustFormatting(parent), part, children));
        }

        PartInfo root = stringToPart.get("root");

        for (Map.Entry<String, PartInfo> entry : stringToPart.entrySet()) {
            if (entry.getValue().parent != null) {
                PartInfo parentPart = stringToPart.get(entry.getValue().parent);
                if (parentPart != null)
                    parentPart.children.put(entry.getKey(), entry.getValue().part);
                else {
                    if (root != null && entry.getValue().part != root.part) // put to root if you can't find the parent.
                        root.children.put(entry.getKey(), entry.getValue().part);
                }
            }
        }

        if (root == null)
            return null;

        ensureAvailable(root.children, "ear");
        ensureAvailable(root.children, "cloak");
        root.children.computeIfAbsent("cloak", (string) -> // Required to allow a cape to render
                HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F).getRoot().addOrReplaceChild(string,
                        CubeListBuilder.create()
                                .texOffs(0, 0)
                                .addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, CubeDeformation.NONE, 1.0F, 0.5F),
                        PartPose.offset(0.0F, 0.0F, 0.0F)).bake(64, 64));
        ensureAvailable(root.children, "left_sleeve");
        ensureAvailable(root.children, "right_sleeve");
        ensureAvailable(root.children, "left_pants");
        ensureAvailable(root.children, "right_pants");
        ensureAvailable(root.children, "jacket");

        // Just to be safe, some model seems to have only head, arm, etc. I mean...
        ensureAvailable(root.children, "hat");
        ensureAvailable(root.children, "left_arm");
        ensureAvailable(root.children, "right_arm");
        ensureAvailable(root.children, "left_leg");
        ensureAvailable(root.children, "right_leg");

        return new BedrockPlayerEntityModel<>(root.part, false);
    }

    private static String adjustFormatting(String name) {
        if (name == null) {
            return null;
        }

        // it can be lowercase, use lowercase...
        return switch (name.toLowerCase()) {
            case "leftarm" -> "left_arm";
            case "rightarm" -> "right_arm";
            case "leftleg" -> "left_leg";
            case "rightleg" -> "right_leg";
            default -> name;
        };
    }

    /**
     * Ensure a part is created, or else the geometry will not load in 1.17.
     */
    private static void ensureAvailable(Map<String, ModelPart> children, String name) {
        children.computeIfAbsent(name, (string) -> new ModelPart(Collections.emptyList(), Maps.newHashMap()));
    }

    private record PartInfo(String parent, ModelPart part, Map<String, ModelPart> children) {
    }
}
