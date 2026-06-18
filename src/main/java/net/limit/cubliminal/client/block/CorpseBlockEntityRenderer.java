package net.limit.cubliminal.client.block;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.entity.CorpseBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.UUID;

public class CorpseBlockEntityRenderer implements BlockEntityRenderer<CorpseBlockEntity> {
    private static final Identifier SKELETON_TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");
    private static final Identifier MUSCLE_WIDE_TEXTURE = Cubliminal.id("textures/block/corpse_muscle_wide.png");
    private static final Identifier MUSCLE_SLIM_TEXTURE = Cubliminal.id("textures/block/corpse_muscle_slim.png");

    private final PlayerEntityModel playerModel;
    private final PlayerEntityModel playerSlimModel;
    private final SkeletonEntityModel<SkeletonEntityRenderState> skeletonModel;

    public CorpseBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.playerModel = new PlayerEntityModel(ctx.getLayerModelPart(EntityModelLayers.PLAYER), false);
        this.playerSlimModel = new PlayerEntityModel(ctx.getLayerModelPart(EntityModelLayers.PLAYER_SLIM), true);
        this.skeletonModel = new SkeletonEntityModel<>(ctx.getLayerModelPart(EntityModelLayers.SKELETON));
    }

    @Override
    public void render(CorpseBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        Identifier texture = this.getTexture(entity);
        BipedEntityModel<?> model = this.getModel(entity);

        RenderLayer renderLayer = model.getLayer(texture);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        float bodyRotation = 0f; // In degrees

        // Position the model in the center
        matrices.translate(0.5, 0.13f, 0);
        // Set the correct rotation for the model
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(bodyRotation), 0, 0, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        // Set the death pose
        model.resetTransforms();

        model.rightArm.roll = (float) Math.toRadians(65f);
        model.rightArm.yaw = (float) Math.toRadians(5f);
        model.rightArm.pitch = (float) Math.toRadians(5f);

        model.rightLeg.roll = (float) Math.toRadians(45f);
        model.rightLeg.pitch = (float) Math.toRadians(5);

        model.leftArm.roll = (float) Math.toRadians(-165f);
        model.leftArm.yaw = (float) Math.toRadians(10f);
        model.leftArm.pitch = (float) Math.toRadians(5f);

        model.leftLeg.roll = (float) Math.toRadians(-35f);
        model.leftLeg.pitch = (float) Math.toRadians(5f);

        model.head.yaw = (float) Math.toRadians(15f);

        model.render(matrices, vertexConsumer, light, overlay);

        matrices.pop();
    }

    private BipedEntityModel<?> getModel(CorpseBlockEntity entity) {
        // for skeleton
        if (entity.isSkeleton()) {
            return this.skeletonModel;
        }

        // for muscle or player model
        SkinTextures playerSkin = this.getPlayerSkin(entity.getUuid());
        if (playerSkin.model() == SkinTextures.Model.WIDE) {
            return this.playerModel;
        } else  {
            return this.playerSlimModel;
        }
    }

    private Identifier getTexture(CorpseBlockEntity entity) {
        if (entity.isSkeleton()) {
            return SKELETON_TEXTURE;
        }

        SkinTextures playerSkin = this.getPlayerSkin(entity.getUuid());
        boolean muscleOnly = entity.isMuscleOnly();
        if (playerSkin.model() == SkinTextures.Model.WIDE) {
            return muscleOnly ? MUSCLE_WIDE_TEXTURE : playerSkin.texture();
        } else  {
            return muscleOnly ? MUSCLE_SLIM_TEXTURE : playerSkin.texture();
        }
    }

    private SkinTextures getPlayerSkin(UUID uuid) {
        PlayerListEntry playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(uuid);
        return playerListEntry == null ? DefaultSkinHelper.getSkinTextures(uuid) : playerListEntry.getSkinTextures();
    }
}
