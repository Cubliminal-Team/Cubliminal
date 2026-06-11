package net.limit.cubliminal.client.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.custom.FluxCapacitorBlock;
import net.limit.cubliminal.block.entity.FluxCapacitorBlockEntity;
import net.limit.cubliminal.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;

/**
 * Uhh we seriously need to rewrite this using rotation matrices.
 */

@Environment(EnvType.CLIENT)
public class FluxCapacitorRenderer implements BlockEntityRenderer<FluxCapacitorBlockEntity> {

    public FluxCapacitorRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(FluxCapacitorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getCachedState().get(FluxCapacitorBlock.POWERED)) {
            matrices.push();

            Direction direction = entity.getCachedState().get(FluxCapacitorBlock.FACING);
            float x1 = 0.5f;
            float z1 = switch (direction) {
                case NORTH -> {
                    x1 = 0.0f;
                    yield -0.5f;
                }
                case SOUTH -> {
                    x1 = 0.0f;
                    yield 0.5f;
                }
                case WEST -> {
                    x1 = -0.5f;
                    yield 0.0F;
                }
                default -> 0.0f;
            };

            float progress = (float) Math.max(entity.getRealityTicks() - 100, 0) / 100f;
            float offset = 0.1f - (float) Math.max(entity.getRealityTicks() - 100, 0) / 1000f;
            if (entity.getRealityTicks() > 200) {
                progress -= 400f / entity.getRealityTicks();
                if (entity.getRealityTicks() > 250) progress = 0;
            }
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.translate(x1, 0.0f, z1);
            if (direction.getAxis().equals(Direction.Axis.X)) {
                matrices.scale(1.0f, progress, progress);
            } else {
                matrices.scale(progress, progress, 1.0f);
            }

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayers.getFluxCapacitor());
            switch (direction) {
                case NORTH -> renderNorth(matrices, vertexConsumer, offset, light);
                case WEST -> renderWest(matrices, vertexConsumer, offset, light);
                case EAST -> renderEast(matrices, vertexConsumer, offset, light);
                default -> renderSouth(matrices, vertexConsumer, offset, light);
            }

            matrices.pop();
        }

    }

    private void renderNorth(MatrixStack matrices, VertexConsumer vertexConsumer, float offset, int light) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(positionMatrix, 0.1f, -0.1f + offset, 0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, -0.1f, -0.1f + offset, 0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, -0.1f, 0.1f + offset, 0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, 0.1f, 0.1f + offset, 0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
    }

    private void renderWest(MatrixStack matrices, VertexConsumer vertexConsumer, float offset, int light) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(positionMatrix, 0.12f, -0.1f + offset, -0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, 0.12f, -0.1f + offset, 0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, 0.12f, 0.1f + offset, 0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, 0.12f, 0.1f + offset, -0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
    }

    private void renderEast(MatrixStack matrices, VertexConsumer vertexConsumer, float offset, int light) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(positionMatrix, -0.12f, -0.1f + offset, 0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, -0.12f, -0.1f + offset, -0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, -0.12f, 0.1f + offset, -0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, -0.12f, 0.1f + offset, 0.1f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
    }

    private void renderSouth(MatrixStack matrices, VertexConsumer vertexConsumer, float offset, int light) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(positionMatrix, -0.1f, -0.1f + offset, -0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, 0.1f, -0.1f + offset, -0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, 0.1f, 0.1f + offset, -0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
        vertexConsumer.vertex(positionMatrix, -0.1f, 0.1f + offset, -0.12f)
                .color(1.0f, 1.0f, 1.0f, 1.0f).light(light);
    }
}
