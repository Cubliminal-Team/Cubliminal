package net.limit.cubliminal.client.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.entity.SeatEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class SeatRenderer extends EntityRenderer<SeatEntity, EntityRenderState> {
    public SeatRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }



    @Override
    public boolean shouldRender(SeatEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(EntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
    }

    @Override
    public void updateRenderState(SeatEntity entity, EntityRenderState state, float tickDelta) {
    }
}
