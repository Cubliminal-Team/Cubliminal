package net.limit.cubliminal.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.access.PEAccessor;
import net.ludocrypt.limlib.api.effects.post.StaticPostEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.util.Identifier;

public class NoclipPostEffect extends StaticPostEffect {

    public NoclipPostEffect(Identifier shaderLocation) {
        super(shaderLocation);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PostEffectProcessor postEffectProcessor, Framebuffer framebuffer, Pool pool, RenderTickCounter tickCounter, boolean tick) {
        if (tick) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            postEffectProcessor.setUniforms(
                    "NoclipTicks", (float) Math.abs(((PEAccessor) player).getNoclipEngine().getTicksToNc())
            );
        }
        super.render(postEffectProcessor, framebuffer, pool, tickCounter, tick);
    }
}
