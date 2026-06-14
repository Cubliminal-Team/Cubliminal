package net.limit.cubliminal.client.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;

@Environment(EnvType.CLIENT)
// This have to extends PlayerEntityRenderState
public class SkinStealerRenderState extends PlayerEntityRenderState {
    public boolean angry = false;
    public boolean disguised = false;
    public SkinTextures disguiseTexture = DefaultSkinHelper.getSteve();;
}
