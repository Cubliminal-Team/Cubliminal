package net.limit.cubliminal.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.IniterClient;
import net.limit.cubliminal.client.render.entity.SmilerRenderer;
import net.limit.cubliminal.client.render.entity.model.SmilerModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

@Environment(EnvType.CLIENT)
public class CubliminalModelLayers implements IniterClient {

    public static final EntityModelLayer SMILER =
            new EntityModelLayer(Cubliminal.id("smiler"), "main");

    @Override
    public void init() {
        register(CubliminalEntities.SMILER, SmilerRenderer::new, CubliminalModelLayers.SMILER, SmilerModel::getTexturedModelData);
    }

    public <E extends Entity> void register(EntityType<? extends E> entityType, EntityRendererFactory<E> entityRendererFactory, EntityModelLayer modelLayer, EntityModelLayerRegistry.TexturedModelDataProvider provider){
        EntityRendererRegistry.register(entityType, entityRendererFactory);
        EntityModelLayerRegistry.registerModelLayer(modelLayer, provider);
    }
}
