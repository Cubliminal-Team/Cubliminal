package net.limit.cubliminal.client.entity;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.entity.model.SkinStealerModel;
import net.limit.cubliminal.client.entity.state.SkinStealerRenderState;
import net.limit.cubliminal.entity.hostile.SkinStealerEntity;
import net.limit.cubliminal.init.CubliminalModelLayers;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class SkinStealerRenderer extends MobEntityRenderer<SkinStealerEntity, SkinStealerRenderState, SkinStealerModel> {

    public static final Identifier TEXTURE = Cubliminal.id("textures/entity/skin_stealer.png");

    public SkinStealerRenderer(EntityRendererFactory.Context context) {
        super(context, new SkinStealerModel(context.getPart(CubliminalModelLayers.SKIN_STEALER)), 0.5f);
    }

    @Override
    public Identifier getTexture(SkinStealerRenderState state) {
        return TEXTURE;
    }

    @Override
    public SkinStealerRenderState createRenderState() {
        return new SkinStealerRenderState();
    }

    @Override
    public void updateRenderState(SkinStealerEntity entity, SkinStealerRenderState renderState, float f) {
        super.updateRenderState(entity, renderState, f);
        BipedEntityRenderer.updateBipedRenderState(entity, renderState, f);
        renderState.angry = entity.isAngry();
    }
}
