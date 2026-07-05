package net.limit.cubliminal.client.shader;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.config.CubliminalConfig;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.ludocrypt.limlib.api.effects.post.StaticPostEffect;
import net.minecraft.util.Identifier;

public class CustomPostEffect extends StaticPostEffect {

    public static final MapCodec<CustomPostEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("shader_name").stable().forGetter(CustomPostEffect::getShaderName)
    ).apply(instance, instance.stable(CustomPostEffect::new)));

    public CustomPostEffect(Identifier shaderLocation) {
        super(shaderLocation);
    }

    @Override
    public MapCodec<? extends PostEffect> getCodec() {
        return CustomPostEffect.CODEC;
    }

    @Override
    public boolean shouldRender() {
        return !CubliminalConfig.get().disableCustomShaders;
    }
}
