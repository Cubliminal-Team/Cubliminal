package net.limit.cubliminal.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.gen.feature.FeatureConfig;

public record FluidTickSchedulerFeatureConfig(FluidState fluidState) implements FeatureConfig {
    public static final Codec<FluidTickSchedulerFeatureConfig> CODEC = FluidState.CODEC.fieldOf("state").codec().xmap(
            FluidTickSchedulerFeatureConfig::new, FluidTickSchedulerFeatureConfig::fluidState
    );
}
