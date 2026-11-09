package net.limit.cubliminal.client.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.entity.SocketBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class SocketBlockRenderer implements BlockEntityRenderer<SocketBlockEntity> {

    final BlockRenderManager renderManager;

    public SocketBlockRenderer(BlockEntityRendererFactory.Context ctx) {
        this.renderManager = ctx.getRenderManager();
    }

    @Override
    public void render(SocketBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState support = entity.getSupport();
        if (support != null) {
            RenderLayer layer = RenderLayers.getBlockLayer(support);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(layer);
            matrices.push();
            this.renderManager.renderBlock(support, entity.getPos(), entity.getWorld(), matrices, vertexConsumer, true, Random.create());
            matrices.pop();
        }
    }

    @Override
    public int getRenderDistance() {
        return MinecraftClient.getInstance().options.getClampedViewDistance() * 16;
    }
}
