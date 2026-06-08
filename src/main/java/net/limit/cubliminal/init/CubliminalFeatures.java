package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.world.feature.FluidTickSchedulerFeature;
import net.limit.cubliminal.world.feature.FluidTickSchedulerFeatureConfig;
import net.limit.cubliminal.world.feature.WoodenCrateFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.CountConfig;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;

public class CubliminalFeatures implements Initer {

    public static final RegistryEntry<Feature<?>> WOODEN_CRATE = register(Cubliminal.id("wooden_crate"), new WoodenCrateFeature(CountConfig.CODEC));

    public static final RegistryEntry<Feature<?>> FLUID_TICK_SCHEDULER = register(Cubliminal.id("fluid_tick_scheduler"), new FluidTickSchedulerFeature(FluidTickSchedulerFeatureConfig.CODEC));

    private static <T extends Feature<?>> RegistryEntry<Feature<?>> register(Identifier id, T feature) {
        return Registry.registerReference(Registries.FEATURE, id, feature);
    }

    @Override
    public void init() {
        BiomeModifications.addFeature(
                ctx -> ctx.getBiomeRegistryEntry().isIn(CubliminalBiomes.LEVEL_ONE),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, Cubliminal.id("wooden_crate_wrapper"))
        );
        BiomeModifications.addFeature(
                ctx -> ctx.getBiomeRegistryEntry().isIn(CubliminalBiomes.LEVEL_ONE),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, Cubliminal.id("contaminated_water_wrapper"))
        );
        BiomeModifications.addFeature(
                ctx -> ctx.getBiomeRegistryEntry().isIn(CubliminalBiomes.LEVEL_ONE),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, Cubliminal.id("mold"))
        );
    }
}
