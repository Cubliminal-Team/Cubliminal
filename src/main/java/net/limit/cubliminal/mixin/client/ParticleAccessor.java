package net.limit.cubliminal.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(Particle.class)
public interface ParticleAccessor {

    @Accessor("gravityStrength")
    float getGravityStrength();

    @Accessor("gravityStrength")
    void setGravityStrength(float value);

    @Accessor("maxAge")
    void setMaxAge(int value);
}
