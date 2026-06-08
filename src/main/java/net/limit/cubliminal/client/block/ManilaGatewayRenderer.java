package net.limit.cubliminal.client.block;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.custom.TheLobbyGatewayBlock;
import net.limit.cubliminal.block.entity.TheLobbyGatewayBlockEntity;
import net.limit.cubliminal.client.render.RenderLayers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ManilaGatewayRenderer implements BlockEntityRenderer<TheLobbyGatewayBlockEntity> {

    public ManilaGatewayRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(TheLobbyGatewayBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getCachedState().get(TheLobbyGatewayBlock.LIT)) {
            matrices.push();

            MinecraftClient client = MinecraftClient.getInstance();

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayers.getManilaSkybox());
            ShaderProgram shader = RenderSystem.setShader(RenderLayers.RENDERTYPE_CUBLIMINAL_MANILA_SKYBOX);


            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            this.renderSide(entity, positionMatrix, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
            this.renderSide(entity, positionMatrix, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
            this.renderSide(entity, positionMatrix, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
            this.renderSide(entity, positionMatrix, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
            this.renderSide(entity, positionMatrix, vertexConsumer, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
            this.renderSide(entity, positionMatrix, vertexConsumer, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);

            Camera camera = client.gameRenderer.getCamera();
            Matrix4f rotation = new MatrixStack().peek().getPositionMatrix();
            rotation.rotate(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            rotation.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

            if (shader.getUniform("RotMat") != null) {
                shader.getUniform("RotMat").set(rotation);
            }

            matrices.pop();
        }
    }

    private void renderSide(TheLobbyGatewayBlockEntity entity, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction side) {
        if (entity.shouldDrawSide(side)) {
            vertices.vertex(model, x1, y1, z1);
            vertices.vertex(model, x2, y1, z2);
            vertices.vertex(model, x2, y2, z3);
            vertices.vertex(model, x1, y2, z4);
        }
    }

}
