package net.limit.cubliminal.client.render.entity;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.render.entity.model.HoundModel;
import net.limit.cubliminal.client.render.entity.state.BackroomEntityRenderState;
import net.limit.cubliminal.entity.hostile.HoundEntity;
import net.limit.cubliminal.init.CubliminalModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class HoundRenderer extends MobEntityRenderer<HoundEntity, BackroomEntityRenderState, HoundModel> {
    public static final Identifier TEXTURE = Cubliminal.id("textures/entity/hound.png");

    public HoundRenderer(EntityRendererFactory.Context context) {
        super(context, new HoundModel(context.getPart(CubliminalModelLayers.HOUND)), 0.5f);
    }

    @Override
    public BackroomEntityRenderState createRenderState() {
        return new BackroomEntityRenderState();
    }

    @Override
    public Identifier getTexture(BackroomEntityRenderState state) {
        return TEXTURE;
    }
}
