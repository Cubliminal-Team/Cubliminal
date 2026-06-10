package net.limit.cubliminal.client.entity;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.entity.model.HoundModel;
import net.limit.cubliminal.client.entity.state.CustomEntityRenderState;
import net.limit.cubliminal.entity.hostile.HoundEntity;
import net.limit.cubliminal.init.CubliminalModelLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class HoundRenderer extends MobEntityRenderer<HoundEntity, CustomEntityRenderState, HoundModel> {
    public static final Identifier TEXTURE = Cubliminal.id("textures/entity/hound.png");

    public HoundRenderer(EntityRendererFactory.Context context) {
        super(context, new HoundModel(context.getPart(CubliminalModelLayers.HOUND)), 0.5f);
    }

    @Override
    public CustomEntityRenderState createRenderState() {
        return new CustomEntityRenderState();
    }

    @Override
    public Identifier getTexture(CustomEntityRenderState state) {
        return TEXTURE;
    }
}
