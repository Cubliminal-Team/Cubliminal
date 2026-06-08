package net.limit.cubliminal.mixin;

import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleEffect;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Fluid.class)
public interface FluidAccessor {
    @Nullable
    @Invoker("getParticle")
    ParticleEffect invokeGetParticle();
}
