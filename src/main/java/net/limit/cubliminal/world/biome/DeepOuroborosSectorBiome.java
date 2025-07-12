package net.limit.cubliminal.world.biome;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public class DeepOuroborosSectorBiome {
    public static Biome create(RegistryEntryLookup<PlacedFeature> features, RegistryEntryLookup<ConfiguredCarver<?>> carvers) {
        Biome.Builder biome = new Biome.Builder();

        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();
        GenerationSettings.Builder generationSettings = new GenerationSettings.LookupBackedBuilder(features, carvers);
        BiomeEffects.Builder biomeEffects = new BiomeEffects.Builder();

        //biomeEffects.loopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP);
        biomeEffects.skyColor(1315860);
        biomeEffects.fogColor(3024671);
        biomeEffects.waterColor(4801836);
        biomeEffects.waterFogColor(5658174);
        BiomeEffects effects = biomeEffects.build();

        biome.spawnSettings(spawnSettings.build());
        biome.generationSettings(generationSettings.build());
        biome.temperature(0.6f);
        biome.downfall(0.65f);
        biome.precipitation(false);
        biome.effects(effects);

        return biome.build();
    }
}
