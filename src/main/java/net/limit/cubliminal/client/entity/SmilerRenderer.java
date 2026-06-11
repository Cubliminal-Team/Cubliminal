package net.limit.cubliminal.client.entity;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.init.CubliminalModelLayers;
import net.limit.cubliminal.client.entity.model.SmilerModel;
import net.limit.cubliminal.client.entity.state.CustomEntityRenderState;
import net.limit.cubliminal.entity.hostile.SmilerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.http.annotation.Obsolete;

public class SmilerRenderer extends MobEntityRenderer<SmilerEntity, CustomEntityRenderState, SmilerModel> {
    public static final Identifier TEXTURE = Cubliminal.id("textures/entity/smiler_face.png");

    public SmilerRenderer(EntityRendererFactory.Context context) {
        super(context, new SmilerModel(context.getPart(CubliminalModelLayers.SMILER)), 0.5f);
    }

    @Override
    public CustomEntityRenderState createRenderState() {
        return new CustomEntityRenderState();
    }

    @Obsolete
    protected int getBlockLight(SmilerEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public Identifier getTexture(CustomEntityRenderState state) {
        return TEXTURE;
    }
}
