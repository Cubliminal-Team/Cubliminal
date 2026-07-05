package net.limit.cubliminal.init;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.client.shader.CustomPostEffect;
import net.limit.cubliminal.client.shader.NoclipPostEffect;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CubliminalPostEffects implements Initer {

    @Override
    public void init() {
        register("custom", CustomPostEffect.CODEC);
        register("noclip", NoclipPostEffect.CODEC);
    }

    private static void register(String id, MapCodec<? extends PostEffect> mapCodec) {
        Registry.register(PostEffect.POST_EFFECT_CODEC, Identifier.of("limlib", id), mapCodec);
    }
}
