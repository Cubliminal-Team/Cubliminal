package net.limit.cubliminal.client.block;

import net.limit.cubliminal.block.entity.PlayerModelBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class PlayerModelBlockRenderer implements BlockEntityRenderer<PlayerModelBlockEntity> {
    private OtherClientPlayerEntity cachedPlayer;

    @Override
    public void render(PlayerModelBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        OtherClientPlayerEntity player = getOrCreatePlayer(entity);
        if (player == null) return;

        matrices.push();

        matrices.translate(0.5f, 0, 0.5f);
        matrices.scale(0.9375f, 0.9375f, 0.9375f);

        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();

        dispatcher.render(
                player,
                0, 0, 0,
                0.0f,
                matrices,
                vertexConsumers,
                light
        );
    }

    private OtherClientPlayerEntity getOrCreatePlayer(
            PlayerModelBlockEntity be) {

        if (cachedPlayer == null && be.getProfile() != null) {
            MinecraftClient client = MinecraftClient.getInstance();

            cachedPlayer = new OtherClientPlayerEntity(
                    client.world,
                    be.getProfile()
            );
        }

        return cachedPlayer;
    }
}
