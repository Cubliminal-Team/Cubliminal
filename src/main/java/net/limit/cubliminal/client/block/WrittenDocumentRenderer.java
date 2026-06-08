package net.limit.cubliminal.client.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.entity.WrittenDocumentBlockEntity;
import net.limit.cubliminal.client.render.RenderLayers;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class WrittenDocumentRenderer implements BlockEntityRenderer<WrittenDocumentBlockEntity> {

    public WrittenDocumentRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(WrittenDocumentBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.isInImageMode() && entity.hasTexture()) {
            matrices.push();

            matrices.translate(0.5, -0.93, 0.5);
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(entity.getRotationDeg()));
            matrices.translate(0, 0, -0.03);
            matrices.scale(0.5f, 1.0f, 0.5f);
            matrices.translate(-0.5, 0, -0.5);

            RenderLayer renderLayer = RenderLayers.createWrittenDocument(entity.getTexture());
            VertexConsumer consumer = vertexConsumers.getBuffer(renderLayer);
            Matrix4f position = matrices.peek().getPositionMatrix();
            consumer.vertex(position, 0.0f, 1.0f, 0.0f).texture(0.0f, 0.0f).color(Colors.WHITE).light(light);
            consumer.vertex(position, 0.0f, 1.0f, 1.0f).texture(0.0f, 1.0f).color(Colors.WHITE).light(light);
            consumer.vertex(position, 1.0f, 1.0f, 1.0f).texture(1.0f, 1.0f).color(Colors.WHITE).light(light);
            consumer.vertex(position, 1.0f, 1.0f, 0.0f).texture(1.0f, 0.0f).color(Colors.WHITE).light(light);

            matrices.pop();
        }
    }

}