package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.util.DebugLogger;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class CubliminalFluidTags {
    public static final TagKey<Fluid> CUSTOM_FLUIDS = of("custom_fluids");

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, Cubliminal.id(id));
    }

    public static void init() {
        DebugLogger.displayRegisteredSectors(CubliminalFluidTags.class);
    }
}
