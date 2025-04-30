package net.limit.cubliminal.init;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.world.biome.source.ClusteredBiomeSource;
import net.limit.cubliminal.world.biome.source.LevelOneBiomeSource;
import net.limit.cubliminal.world.biome.source.SimplexBiomeSource;
import net.limit.cubliminal.world.chunk.LevelOneChunkGenerator;
import net.limit.cubliminal.world.chunk.LevelZeroChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class CubliminalBiomes {
    public static final RegistryKey<Biome> THE_LOBBY_BIOME = RegistryKey
		.of(RegistryKeys.BIOME, Cubliminal.id(CubliminalRegistrar.THE_LOBBY));

	public static final RegistryKey<Biome> PILLAR_BIOME = RegistryKey
			.of(RegistryKeys.BIOME, Cubliminal.id("pillar_biome"));

	public static final RegistryKey<Biome> REDROOMS_BIOME = RegistryKey
			.of(RegistryKeys.BIOME, Cubliminal.id("redrooms"));

	public static final RegistryKey<Biome> HABITABLE_ZONE_BIOME = RegistryKey
			.of(RegistryKeys.BIOME, Cubliminal.id(CubliminalRegistrar.HABITABLE_ZONE));

	public static final RegistryKey<Biome> PARKING_ZONE_BIOME = RegistryKey
			.of(RegistryKeys.BIOME, Cubliminal.id("parking_zone"));

	public static final TagKey<Biome> CAN_NOCLIP_TO = of("can_noclip_to");


    public static void init() {
		getBiomeSource("simplex_biome_source", SimplexBiomeSource.CODEC);
		getBiomeSource("clustered_biome_source", ClusteredBiomeSource.CODEC);
		getBiomeSource("level_one_biome_source", LevelOneBiomeSource.CODEC);
		getChunkGenerator("the_lobby_chunk_generator", LevelZeroChunkGenerator.CODEC);
		getChunkGenerator("habitable_zone_chunk_generator", LevelOneChunkGenerator.CODEC);
    }

	public static <C extends ChunkGenerator, D extends MapCodec<C>> D getChunkGenerator(String id, D chunkGeneratorCodec) {
		return Registry.register(Registries.CHUNK_GENERATOR, Cubliminal.id(id), chunkGeneratorCodec);
	}
	public static <C extends BiomeSource, D extends MapCodec<C>> D getBiomeSource(String id, D biomeSourceCodec) {
		return Registry.register(Registries.BIOME_SOURCE, Cubliminal.id(id), biomeSourceCodec);
	}

	public static TagKey<Biome> of(String id) {
		return TagKey.of(RegistryKeys.BIOME, Cubliminal.id(id));
	}

}
