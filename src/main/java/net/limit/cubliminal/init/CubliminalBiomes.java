package net.limit.cubliminal.init;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
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

public class CubliminalBiomes implements Initer {
    public static final RegistryKey<Biome> THE_LOBBY_BIOME = of(CubliminalRegistrar.THE_LOBBY);

	public static final RegistryKey<Biome> PILLAR_BIOME = of("pillar_biome");

	public static final RegistryKey<Biome> REDROOMS_BIOME = of("redrooms");

	public static final RegistryKey<Biome> HABITABLE_ZONE_BIOME = of(CubliminalRegistrar.HABITABLE_ZONE);

	public static final RegistryKey<Biome> PARKING_ZONE_BIOME = of("parking_zone");

	public static final RegistryKey<Biome> AQUILA_SECTOR_BIOME = of("aquila_sector");
	public static final RegistryKey<Biome> DEEP_AQUILA_SECTOR_BIOME = of("deep_aquila_sector");

	public static final RegistryKey<Biome> GUILD_SECTOR_BIOME = of("guild_sector");
	public static final RegistryKey<Biome> DEEP_GUILD_SECTOR_BIOME = of("deep_guild_sector");

	public static final RegistryKey<Biome> GOTHIC_SECTOR_BIOME = of("gothic_sector");
	public static final RegistryKey<Biome> DEEP_GOTHIC_SECTOR_BIOME = of("deep_gothic_sector");

	public static final RegistryKey<Biome> OUROBOROS_SECTOR_BIOME = of("ouroboros_sector");
	public static final RegistryKey<Biome> DEEP_OUROBOROS_SECTOR_BIOME = of("deep_ouroboros_sector");



	public static final TagKey<Biome> CAN_NOCLIP_TO = TagKey.of(RegistryKeys.BIOME, Cubliminal.id("can_noclip_to"));
	public static final TagKey<Biome> DEEP_LEVEL_ONE = TagKey.of(RegistryKeys.BIOME, Cubliminal.id("deep_level_one"));

	@Override
    public void init() {
		getBiomeSource("simplex_biome_source", SimplexBiomeSource.CODEC);
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

	public static RegistryKey<Biome> of(String id) {
		return RegistryKey.of(RegistryKeys.BIOME, Cubliminal.id(id));
	}

}
