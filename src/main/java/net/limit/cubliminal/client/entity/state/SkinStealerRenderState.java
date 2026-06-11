package net.limit.cubliminal.client.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;

@Environment(EnvType.CLIENT)
public class SkinStealerRenderState extends BipedEntityRenderState {
    public boolean angry = false;
}
