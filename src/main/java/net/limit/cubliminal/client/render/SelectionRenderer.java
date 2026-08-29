package net.limit.cubliminal.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class SelectionRenderer {

    public static void renderBoxOld(Box box, WorldRenderContext ctx, Vector4f color, Vector3f axisCol) {
        VertexConsumerProvider vertexConsumerProvider = ctx.consumers();
        if (vertexConsumerProvider == null) return;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());

        MatrixStack matrixStack = ctx.matrixStack();
        if (matrixStack == null) return;
        matrixStack.push();

        Vec3d cameraPos = ctx.camera().getPos();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexRendering.drawBox(
                matrixStack,
                vertexConsumer,
                box.minX,
                box.minY,
                box.minZ,
                box.maxX,
                box.maxY,
                box.maxZ,
                color.x, color.y, color.z, color.w,
                axisCol.x, axisCol.y, axisCol.z
        );

        matrixStack.pop();
    }

    public static void renderBox(Box box, WorldRenderContext ctx, Vector4f color, Vector3f axisCol) {
        VertexConsumerProvider vertexConsumerProvider = ctx.consumers();
        if (vertexConsumerProvider == null) return;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());

        MatrixStack matrixStack = ctx.matrixStack();
        if (matrixStack == null) return;

        Vec3d cameraPos = ctx.camera().getPos();

        VertexRendering.drawBox(
                matrixStack,
                vertexConsumer,
                box.minX - cameraPos.x,
                box.minY - cameraPos.y,
                box.minZ - cameraPos.z,
                box.maxX - cameraPos.x,
                box.maxY - cameraPos.y,
                box.maxZ - cameraPos.z,
                color.x, color.y, color.z, color.w,
                axisCol.x, axisCol.y, axisCol.z
        );
    }

    public static void renderBlockOutline(BlockPos pos, WorldRenderContext ctx, Vector4f color) {
        VertexConsumerProvider vertexConsumerProvider = ctx.consumers();
        if (vertexConsumerProvider == null) return;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());

        MatrixStack matrixStack = ctx.matrixStack();
        if (matrixStack == null) return;

        Vec3d cameraPos = ctx.camera().getPos();

        Box box = new Box(pos);
        VertexRendering.drawBox(
                matrixStack,
                vertexConsumer,
                box.minX - cameraPos.x,
                box.minY - cameraPos.y,
                box.minZ - cameraPos.z,
                box.maxX - cameraPos.x,
                box.maxY - cameraPos.y,
                box.maxZ - cameraPos.z,
                color.x, color.y, color.z, color.w
        );
    }

    public static void renderBlockOutlineOld(BlockPos pos, WorldRenderContext ctx, Vector4f color) {
        VertexConsumerProvider vertexConsumerProvider = ctx.consumers();
        if (vertexConsumerProvider == null) return;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());

        MatrixStack matrixStack = ctx.matrixStack();
        if (matrixStack == null) return;
        matrixStack.push();

        Vec3d cameraPos = ctx.camera().getPos();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Box box = new Box(pos);
        VertexRendering.drawBox(
                matrixStack,
                vertexConsumer,
                box.minX,
                box.minY,
                box.minZ,
                box.maxX,
                box.maxY,
                box.maxZ,
                color.x, color.y, color.z, color.w
        );

        matrixStack.pop();
    }
}
