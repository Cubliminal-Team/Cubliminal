package net.limit.cubliminal.world.biome;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public class DeepGothicSectorBiome {
    public static Biome create(RegistryEntryLookup<PlacedFeature> features, RegistryEntryLookup<ConfiguredCarver<?>> carvers) {
        Biome.Builder biome = new Biome.Builder();

        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();
        GenerationSettings.Builder generationSettings = new GenerationSettings.LookupBackedBuilder(features, carvers);
        BiomeEffects.Builder biomeEffects = new BiomeEffects.Builder();

        //biomeEffects.loopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP);
        biomeEffects.skyColor(1184523);
        biomeEffects.fogColor(2434334);
        biomeEffects.waterColor(5131826);
        biomeEffects.waterFogColor(6250319);
        BiomeEffects effects = biomeEffects.build();

        biome.spawnSettings(spawnSettings.build());
        biome.generationSettings(generationSettings.build());
        biome.temperature(0.4f);
        biome.downfall(0.86f);
        biome.precipitation(false);
        biome.effects(effects);

        return biome.build();
    }
}
