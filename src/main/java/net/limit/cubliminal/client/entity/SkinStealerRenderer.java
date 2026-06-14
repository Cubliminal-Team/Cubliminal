package net.limit.cubliminal.client.entity;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.entity.model.SkinStealerModel;
import net.limit.cubliminal.client.entity.state.SkinStealerRenderState;
import net.limit.cubliminal.entity.hostile.SkinStealerEntity;
import net.limit.cubliminal.init.CubliminalModelLayers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.UUID;

public class SkinStealerRenderer extends MobEntityRenderer<SkinStealerEntity, SkinStealerRenderState, EntityModel<SkinStealerRenderState>> {

    public static final Identifier TEXTURE = Cubliminal.id("textures/entity/skin_stealer.png");

    private final PlayerEntityModel playerEntityModel;
    private final PlayerEntityModel playerSlimEntityModel;
    private final SkinStealerModel skinStealerModel;

    public SkinStealerRenderer(EntityRendererFactory.Context context) {
        super(context, new SkinStealerModel(context.getPart(CubliminalModelLayers.SKIN_STEALER)), 0.5f);
        this.skinStealerModel = (SkinStealerModel) this.model;
        this.playerEntityModel = new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), false);
        this.playerSlimEntityModel = new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public Identifier getTexture(SkinStealerRenderState state) {
        return state.disguised ? state.disguiseTexture.texture() : TEXTURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(SkinStealerRenderState livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();

        if (livingEntityRenderState.disguised) {
            // scale the player model correctly
            matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
            this.model = (EntityModel<SkinStealerRenderState>) (EntityModel<?>) (livingEntityRenderState.disguiseTexture.model() == SkinTextures.Model.WIDE ? this.playerEntityModel : this.playerSlimEntityModel);
        } else {
            this.model = this.skinStealerModel;
        }

        super.render(livingEntityRenderState, matrixStack, vertexConsumerProvider, i);

        matrixStack.pop();
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
        renderState.disguised = entity.isInDisguised();

        Optional<UUID> disguise = entity.getDisguisedAs();
        if (disguise.isPresent()) {
            PlayerListEntry playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(disguise.get());
            renderState.disguiseTexture = playerListEntry == null ? DefaultSkinHelper.getSkinTextures(disguise.get()) : playerListEntry.getSkinTextures();
        }
    }
}
