package net.limit.cubliminal.init;

import com.mojang.datafixers.util.Pair;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.level.LevelWithMaze;
import net.limit.cubliminal.level.Levels;
import net.limit.cubliminal.world.biome.*;
import net.limit.cubliminal.world.biome.noise.RegistryNoisePreset;
import net.limit.cubliminal.world.biome.source.LevelOneBiomeSource;
import net.limit.cubliminal.world.biome.source.SimplexBiomeSource;
import net.limit.cubliminal.world.chunk.LevelOneChunkGenerator;
import net.limit.cubliminal.world.chunk.LevelZeroChunkGenerator;
import net.ludocrypt.limlib.api.LimlibRegistrar;
import net.ludocrypt.limlib.api.LimlibRegistryHooks;
import net.ludocrypt.limlib.api.LimlibWorld;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.ludocrypt.limlib.api.effects.post.StaticPostEffect;
import net.ludocrypt.limlib.api.effects.sky.LiminalDimensionEffects;
import net.ludocrypt.limlib.api.effects.sky.StaticDimensionEffects;
import net.ludocrypt.limlib.api.effects.sound.SoundEffects;
import net.ludocrypt.limlib.api.effects.sound.reverb.StaticReverbEffect;
import net.ludocrypt.limlib.api.skybox.Skybox;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionType.MonsterSettings;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;


public class CubliminalRegistrar implements LimlibRegistrar {

	private static final List<Pair<RegistryKey<LimlibWorld>, LimlibWorld>> WORLDS = new ArrayList<>();
	private static final List<Pair<RegistryKey<SoundEffects>, SoundEffects>> SOUND_EFFECTS = new ArrayList<>();
	private static final List<Pair<RegistryKey<Skybox>, Skybox>> SKYBOXES = new ArrayList<>();
	private static final List<Pair<RegistryKey<LiminalDimensionEffects>, LiminalDimensionEffects>> DIMENSION_EFFECTS = new ArrayList<>();
	private static final List<Pair<RegistryKey<PostEffect>, PostEffect>> POST_EFFECTS = new ArrayList<>();

	public static String THE_LOBBY = "the_lobby";
	public static final RegistryKey<World> THE_LOBBY_KEY = RegistryKey.of(RegistryKeys.WORLD, Cubliminal.id(THE_LOBBY));

	public static String HABITABLE_ZONE = "habitable_zone";
	public static final RegistryKey<World> HABITABLE_ZONE_KEY = RegistryKey.of(RegistryKeys.WORLD, Cubliminal.id(HABITABLE_ZONE));

	@Override
	public void registerHooks() {
		// sound effects
		getSoundEffects(THE_LOBBY,
			new SoundEffects(Optional.of(new StaticReverbEffect.Builder().setDecayTime(2f).setDensity(0.073f).build()),
				Optional.empty(), Optional.empty()));

		getSoundEffects(HABITABLE_ZONE,
				new SoundEffects(Optional.of(new StaticReverbEffect.Builder().setDecayTime(2f).setDensity(0.05f).build()),
						Optional.empty(), Optional.empty()));

		// dim effects
		getDimEffects(THE_LOBBY, new StaticDimensionEffects(Optional.empty(), false, "NONE", false, true, false, 0f));

		getDimEffects(HABITABLE_ZONE, new StaticDimensionEffects(Optional.empty(), false, "NONE", false, true, false, 0.5f));

		// post effects
		getPostEffects("paranoia", new StaticPostEffect(Cubliminal.id("paranoia")));

		getPostEffects("noclip", new StaticPostEffect(Cubliminal.id("noclip")));

		// worlds
		getWorld(THE_LOBBY,
				new LimlibWorld(
						() -> new DimensionType(OptionalLong.of(15500), false, false, false, false, 1.0, false, false, 0, 32, 32,
								TagKey.of(RegistryKeys.BLOCK, Cubliminal.id(THE_LOBBY)), Cubliminal.id(THE_LOBBY),
								0f, new MonsterSettings(false, false, ConstantIntProvider.ZERO, 0)),
						(registry) -> new DimensionOptions(
								registry
										.get(RegistryKeys.DIMENSION_TYPE)
										.getOptional(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Cubliminal.id(THE_LOBBY)))
										.orElseThrow(),
								new LevelZeroChunkGenerator(
										new SimplexBiomeSource(THE_LOBBY_KEY, Levels.LEVEL_0.getLevel(), 0.007f),
										LevelZeroChunkGenerator.createGroup(), Levels.LEVEL_0.getLevel()))));

		getWorld(HABITABLE_ZONE,
				new LimlibWorld(
						() -> new DimensionType(OptionalLong.of(15500), false, false, false, false, 1.0, false, false, 0, 32, 32,
								TagKey.of(RegistryKeys.BLOCK, Cubliminal.id(HABITABLE_ZONE)), Cubliminal.id(HABITABLE_ZONE),
								0f, new MonsterSettings(false, false, ConstantIntProvider.ZERO, 0)),
						(registry) -> new DimensionOptions(
								registry
										.get(RegistryKeys.DIMENSION_TYPE)
										.getOptional(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Cubliminal.id(HABITABLE_ZONE)))
										.orElseThrow(),
								new LevelOneChunkGenerator(
										new LevelOneBiomeSource(0.008f),
										LevelOneChunkGenerator.createGroup(), (LevelWithMaze) Levels.LEVEL_1.getLevel()))));


		WORLDS.forEach((pair) -> LimlibWorld.LIMLIB_WORLD.add(pair.getFirst(), pair.getSecond(), RegistryEntryInfo.DEFAULT));


		LimlibRegistryHooks
			.hook(SoundEffects.SOUND_EFFECTS_KEY, (infoLookup, registryKey, registry) -> SOUND_EFFECTS
				.forEach((pair) -> registry.add(pair.getFirst(), pair.getSecond(), RegistryEntryInfo.DEFAULT)));

		LimlibRegistryHooks
			.hook(Skybox.SKYBOX_KEY, (infoLookup, registryKey, registry) -> SKYBOXES
				.forEach((pair) -> registry.add(pair.getFirst(), pair.getSecond(), RegistryEntryInfo.DEFAULT)));

		LimlibRegistryHooks
			.hook(LiminalDimensionEffects.DIMENSION_EFFECTS_KEY, (infoLookup, registryKey, registry) -> DIMENSION_EFFECTS
				.forEach((pair) -> registry.add(pair.getFirst(), pair.getSecond(), RegistryEntryInfo.DEFAULT)));

		LimlibRegistryHooks
			.hook(PostEffect.POST_EFFECT_KEY, (infoLookup, registryKey, registry) -> POST_EFFECTS
				.forEach((pair) -> registry.add(pair.getFirst(), pair.getSecond(), RegistryEntryInfo.DEFAULT)));


		LimlibRegistryHooks.hook(RegistryKeys.BIOME, (infoLookup, registryKey, registry) -> {
			RegistryEntryLookup<PlacedFeature> features = infoLookup.getRegistryInfo(RegistryKeys.PLACED_FEATURE).orElseThrow().entryLookup();
			RegistryEntryLookup<ConfiguredCarver<?>> carvers = infoLookup.getRegistryInfo(RegistryKeys.CONFIGURED_CARVER).orElseThrow().entryLookup();

			registry.add(CubliminalBiomes.THE_LOBBY_BIOME, TheLobbyBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.PILLAR_BIOME, PillarBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.REDROOMS_BIOME, RedroomsBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);

			registry.add(CubliminalBiomes.AQUILA_SECTOR_BIOME, AquilaSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_AQUILA_SECTOR_BIOME, DeepAquilaSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.GUILD_SECTOR_BIOME, GuildSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_GUILD_SECTOR_BIOME, DeepGuildSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.GOTHIC_SECTOR_BIOME, GothicSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_GOTHIC_SECTOR_BIOME, DeepGothicSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.OUROBOROS_SECTOR_BIOME, OuroborosSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_OUROBOROS_SECTOR_BIOME, DeepOuroborosSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);

			RegistryNoisePreset.initNoisePresets(infoLookup);

		});
	}

	private static <W extends LimlibWorld> W getWorld(String id, W world) {
		WORLDS.add(Pair.of(RegistryKey.of(LimlibWorld.LIMLIB_WORLD_KEY, Cubliminal.id(id)), world));
		return world;
	}

	private static <E extends SoundEffects> E getSoundEffects(String id, E soundEffects) {
		SOUND_EFFECTS.add(Pair.of(RegistryKey.of(SoundEffects.SOUND_EFFECTS_KEY, Cubliminal.id(id)), soundEffects));
		return soundEffects;
	}

	private static <S extends Skybox> S getSkybox(String id, S skybox) {
		SKYBOXES.add(Pair.of(RegistryKey.of(Skybox.SKYBOX_KEY, Cubliminal.id(id)), skybox));
		return skybox;
	}

	private static <D extends LiminalDimensionEffects> D getDimEffects(String id, D dimensionEffects) {
		DIMENSION_EFFECTS
			.add(Pair.of(RegistryKey.of(LiminalDimensionEffects.DIMENSION_EFFECTS_KEY, Cubliminal.id(id)), dimensionEffects));
		return dimensionEffects;
	}

	private static <P extends PostEffect> P getPostEffects(String id, P postEffect) {
		POST_EFFECTS.add(Pair.of(RegistryKey.of(PostEffect.POST_EFFECT_KEY, Cubliminal.id(id)), postEffect));
		return postEffect;
	}
}
