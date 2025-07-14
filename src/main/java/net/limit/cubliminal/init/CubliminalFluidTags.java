package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.util.Debug;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.List;

public class CubliminalFluidTags {
    public static final TagKey<Fluid> CUSTOM_FLUIDS = of("custom_fluids");

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, Cubliminal.id(id));
    }

    public static void init() {
        Debug.displayRegisteredSectors(CubliminalFluidTags.class);
    }
}
