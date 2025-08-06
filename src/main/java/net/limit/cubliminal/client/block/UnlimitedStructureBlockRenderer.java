package net.limit.cubliminal.client.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.entity.USBlockBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

import java.util.Iterator;

@Environment(EnvType.CLIENT)
public class UnlimitedStructureBlockRenderer implements BlockEntityRenderer<USBlockBlockEntity> {

    public UnlimitedStructureBlockRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(USBlockBlockEntity structureBlockBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        if (MinecraftClient.getInstance().player.isCreativeLevelTwoOp() || MinecraftClient.getInstance().player.isSpectator()) {
            BlockPos blockPos = structureBlockBlockEntity.getOffset();
            Vec3i vec3i = structureBlockBlockEntity.getSize();
            if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1) {
                if (structureBlockBlockEntity.getMode() == StructureBlockMode.SAVE || structureBlockBlockEntity.getMode() == StructureBlockMode.LOAD) {
                    double d = blockPos.getX();
                    double e = blockPos.getZ();
                    double g = blockPos.getY();
                    double h = g + (double)vec3i.getY();
                    double k;
                    double l;
                    switch (structureBlockBlockEntity.getMirror()) {
                        case LEFT_RIGHT:
                            k = vec3i.getX();
                            l = -vec3i.getZ();
                            break;
                        case FRONT_BACK:
                            k = -vec3i.getX();
                            l = vec3i.getZ();
                            break;
                        default:
                            k = vec3i.getX();
                            l = vec3i.getZ();
                    }

                    double m;
                    double n;
                    double o;
                    double p;
                    switch (structureBlockBlockEntity.getRotation()) {
                        case CLOCKWISE_90:
                            m = l < 0.0 ? d : d + 1.0;
                            n = k < 0.0 ? e + 1.0 : e;
                            o = m - l;
                            p = n + k;
                            break;
                        case CLOCKWISE_180:
                            m = k < 0.0 ? d : d + 1.0;
                            n = l < 0.0 ? e : e + 1.0;
                            o = m - k;
                            p = n - l;
                            break;
                        case COUNTERCLOCKWISE_90:
                            m = l < 0.0 ? d + 1.0 : d;
                            n = k < 0.0 ? e : e + 1.0;
                            o = m + l;
                            p = n - k;
                            break;
                        default:
                            m = k < 0.0 ? d + 1.0 : d;
                            n = l < 0.0 ? e + 1.0 : e;
                            o = m + k;
                            p = n + l;
                    }

                    if (structureBlockBlockEntity.getMode() == StructureBlockMode.SAVE || structureBlockBlockEntity.shouldShowBoundingBox()) {
                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
                        VertexRendering.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
                    }

                    if (structureBlockBlockEntity.getMode() == StructureBlockMode.SAVE && structureBlockBlockEntity.shouldShowAir()) {
                        this.renderInvisibleBlocks(structureBlockBlockEntity, vertexConsumerProvider, matrixStack);
                    }

                }
            }
        }
    }

    private void renderInvisibleBlocks(USBlockBlockEntity entity, VertexConsumerProvider vertexConsumers, MatrixStack matrices) {
        BlockView blockView = entity.getWorld();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        BlockPos blockPos = entity.getPos();
        BlockPos blockPos2 = entity.getPos().add(entity.getOffset());
        Iterator<BlockPos> var8 = BlockPos.iterate(blockPos2, blockPos2.add(entity.getSize()).add(-1, -1, -1)).iterator();

        while (true) {
            BlockPos blockPos3;
            boolean bl;
            boolean bl2;
            boolean bl3;
            boolean bl4;
            boolean bl5;
            do {
                if (!var8.hasNext()) {
                    return;
                }

                blockPos3 = var8.next();
                BlockState blockState = blockView.getBlockState(blockPos3);
                bl = blockState.isAir();
                bl2 = blockState.isOf(Blocks.STRUCTURE_VOID);
                bl3 = blockState.isOf(Blocks.BARRIER);
                bl4 = blockState.isOf(Blocks.LIGHT);
                bl5 = bl2 || bl3 || bl4;
            } while (!bl && !bl5);

            float f = bl ? 0.05F : 0.0F;
            double d = (float)(blockPos3.getX() - blockPos.getX()) + 0.45F - f;
            double e = (float)(blockPos3.getY() - blockPos.getY()) + 0.45F - f;
            double g = (float)(blockPos3.getZ() - blockPos.getZ()) + 0.45F - f;
            double h = (float)(blockPos3.getX() - blockPos.getX()) + 0.55F + f;
            double i = (float)(blockPos3.getY() - blockPos.getY()) + 0.55F + f;
            double j = (float)(blockPos3.getZ() - blockPos.getZ()) + 0.55F + f;
            if (bl) {
                VertexRendering.drawBox(matrices, vertexConsumer, d, e, g, h, i, j, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
            } else if (bl2) {
                VertexRendering.drawBox(matrices, vertexConsumer, d, e, g, h, i, j, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
            } else if (bl3) {
                VertexRendering.drawBox(matrices, vertexConsumer, d, e, g, h, i, j, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
            } else if (bl4) {
                VertexRendering.drawBox(matrices, vertexConsumer, d, e, g, h, i, j, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
            }
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(USBlockBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return USBlockBlockEntity.structureSizeLimit() * 2;
    }
}
