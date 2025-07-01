package net.limit.cubliminal.world.biome;

import net.limit.cubliminal.init.CubliminalSounds;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public class TheLobbyBiome {
	public static Biome create(RegistryEntryLookup<PlacedFeature> features, RegistryEntryLookup<ConfiguredCarver<?>> carvers) {
		Biome.Builder biome = new Biome.Builder();

		SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();
		GenerationSettings.Builder generationSettings = new GenerationSettings.LookupBackedBuilder(features, carvers);
		BiomeEffects.Builder biomeEffects = new BiomeEffects.Builder();

		biomeEffects.loopSound(CubliminalSounds.AMBIENT_LEVEL_O);
		biomeEffects.skyColor(197378);
		biomeEffects.fogColor(1315584);
		biomeEffects.waterColor(15660426);
		biomeEffects.waterFogColor(12499526);
		BiomeEffects effects = biomeEffects.build();

		biome.spawnSettings(spawnSettings.build());
		biome.generationSettings(generationSettings.build());
		biome.temperature(0.8f);
		biome.downfall(0.4f);
		biome.precipitation(false);
		biome.effects(effects);

        return biome.build();
    }
}
