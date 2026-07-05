package net.limit.cubliminal.client.shader;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.access.PEAccessor;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.util.Identifier;

public class NoclipPostEffect extends CustomPostEffect {

    public static final MapCodec<NoclipPostEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("shader_name").stable().forGetter(NoclipPostEffect::getShaderName)
    ).apply(instance, instance.stable(NoclipPostEffect::new)));

    public NoclipPostEffect(Identifier shaderLocation) {
        super(shaderLocation);
    }

    @Override
    public MapCodec<? extends PostEffect> getCodec() {
        return NoclipPostEffect.CODEC;
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
