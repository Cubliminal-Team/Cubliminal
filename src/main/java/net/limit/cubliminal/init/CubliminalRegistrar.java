package net.limit.cubliminal.init;

import com.mojang.datafixers.util.Pair;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.shader.CustomPostEffect;
import net.limit.cubliminal.client.shader.NoclipPostEffect;
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
import net.ludocrypt.limlib.api.effects.sky.LiminalDimensionEffects;
import net.ludocrypt.limlib.api.effects.sky.StaticDimensionEffects;
import net.ludocrypt.limlib.api.effects.sound.SoundEffects;
import net.ludocrypt.limlib.api.effects.sound.reverb.StaticReverbEffect;
import net.ludocrypt.limlib.api.skybox.Skybox;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.MusicType;
import net.minecraft.util.Identifier;
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

/**
 * This class holds dimension related registry utilities and all the custom dimension registries.
 * Limlib is an api used to facilitate dimension customization, providing easy registration and lookup
 * of various dimension dependent features such as:
 * <ul>
 *     <li>World generation, including dimension types, chunk generators, biomes and world generation features. You want to take a look at {@link LimlibWorld}.</li>
 *     <li>Sound effects, including reverb, distortion and background music. You want to take a look at {@link SoundEffects}.</li>
 *     <li>Post effects that use glsl shaders. These would be turned on and off depending on the dimension you registered them with. You want to take a look at {@link PostEffect}.</li>
 *     <li>Skyboxes, including support for textured skyboxes. You want to take a look at {@link Skybox}.</li>
 *     <li>Miscellaneous dimension effects, primarily sky settings, fog settings and cloud settings. You want to take a look at {@link LiminalDimensionEffects}.</li>
 * </ul>
 * To register either of these, look at the functions all the way below. They require you to provide a {@code String} representing
 * the name of your dimension and then an instance of the feature you want to associate to the dimension. Keep in mind that
 * you can extend the class of the feature and override the methods for further customization.
 * <p>
 *     The way Limlib works is by manually registering into Minecraft's registries where possible and creating new registries
 *     for features that didn't exist by default. This step is done in the {@link #registerHooks()} function, where our approach
 *     is to store the features in several {@code ArrayList's} as a {@code Pair} identified by the dimension's {@code RegistryKey} to later
 *     one by one hook them into the registries. Limlib manages the rest, reducing the amount of boilerplate code and painful trial and error.
 *     It's important to note that {@link #registerHooks()} is run every time that a world is opened, making it a reliable
 *     way to reload features before even datapacks register their resources.
 * </p>
 * <p>
 *     There are also {@code Json} hooks in Limlib to modify declarations stored in the {@code resources/data} folder,
 *     but due to the way Limlib was ported from Quilt 1.20.1 to Fabric 1.21+ and them remaining unused we don't know
 *     for certain the way they work.
 * </p>
 * <p>
 *     Another important concept to be aware of is the way biome noise settings are loaded. They need to be reloaded
 *     every time that the json might change, even before biome source creation. In addition, loading them
 *     requires a registry lookup to grab the biomes from registries, so {@link RegistryNoisePreset#initNoisePresets(RegistryOps.RegistryInfoGetter)}
 *     runs inside of Limlib's biome registry hook right after biome creation.
 * </p>
 */

public class CubliminalRegistrar implements LimlibRegistrar {

	private static final List<Pair<RegistryKey<LimlibWorld>, LimlibWorld>> WORLDS = new ArrayList<>();
	private static final List<Pair<RegistryKey<SoundEffects>, SoundEffects>> SOUND_EFFECTS = new ArrayList<>();
	private static final List<Pair<RegistryKey<Skybox>, Skybox>> SKYBOXES = new ArrayList<>();
	private static final List<Pair<RegistryKey<LiminalDimensionEffects>, LiminalDimensionEffects>> DIMENSION_EFFECTS = new ArrayList<>();
	private static final List<Pair<RegistryKey<PostEffect>, PostEffect>> POST_EFFECTS = new ArrayList<>();

	private static final Identifier THE_LOBBY = Levels.LEVEL_0.name;
	public static final RegistryKey<World> THE_LOBBY_KEY = RegistryKey.of(RegistryKeys.WORLD, THE_LOBBY);

	private static final Identifier HABITABLE_ZONE = Levels.LEVEL_1.name;
	public static final RegistryKey<World> HABITABLE_ZONE_KEY = RegistryKey.of(RegistryKeys.WORLD, HABITABLE_ZONE);

	@Override
	public void registerHooks() {
		// sound effects
		getSoundEffects(THE_LOBBY,
			new SoundEffects(Optional.of(new StaticReverbEffect.Builder().setDecayTime(2f).setDensity(0.073f).build()),
				Optional.empty(), Optional.empty()));

		getSoundEffects(HABITABLE_ZONE,
				new SoundEffects(Optional.of(new StaticReverbEffect.Builder().setDecayTime(2f).setDensity(0.05f).build()),
						Optional.empty(), Optional.of(MusicType.createIngameMusic(CubliminalSounds.BLUE_HORIZON))));

		// dim effects
		getDimEffects(THE_LOBBY, new StaticDimensionEffects(Optional.empty(), false, "NONE", false, true, false, 0f));

		getDimEffects(HABITABLE_ZONE, new StaticDimensionEffects(Optional.empty(), false, "NONE", false, true, false, 0.5f));

		// post effects
		getPostEffects(Cubliminal.id("paranoia"), new CustomPostEffect(Cubliminal.id("paranoia")));

		getPostEffects(Cubliminal.id("noclip"), new NoclipPostEffect(Cubliminal.id("noclip")));

		getPostEffects(THE_LOBBY, new CustomPostEffect(Cubliminal.id("yellow")));

		// worlds
		getWorld(THE_LOBBY,
				new LimlibWorld(
						() -> new DimensionType(OptionalLong.of(15500), false, false, false, false, 1.0, false, false, Levels.LEVEL_0.min_y, Levels.LEVEL_0.world_height, Levels.LEVEL_0.world_height,
								TagKey.of(RegistryKeys.BLOCK, THE_LOBBY), THE_LOBBY,
								0f, new MonsterSettings(false, false, ConstantIntProvider.ZERO, 0)),
						(registry) -> new DimensionOptions(
								registry
										.get(RegistryKeys.DIMENSION_TYPE)
										.getOptional(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, THE_LOBBY))
										.orElseThrow(),
								new LevelZeroChunkGenerator(
										new SimplexBiomeSource(THE_LOBBY_KEY, Levels.LEVEL_0, 0.007f),
										LevelZeroChunkGenerator.createGroup()))));

		getWorld(HABITABLE_ZONE,
				new LimlibWorld(
						() -> new DimensionType(OptionalLong.of(15500), false, false, false, false, 1.0, false, false, Levels.LEVEL_1.min_y, Levels.LEVEL_1.world_height, Levels.LEVEL_1.world_height,
								TagKey.of(RegistryKeys.BLOCK, HABITABLE_ZONE), HABITABLE_ZONE,
								0f, new MonsterSettings(false, false, ConstantIntProvider.ZERO, 0)),
						(registry) -> new DimensionOptions(
								registry
										.get(RegistryKeys.DIMENSION_TYPE)
										.getOptional(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, HABITABLE_ZONE))
										.orElseThrow(),
								new LevelOneChunkGenerator(
										new LevelOneBiomeSource(0.008f),
										LevelOneChunkGenerator.createGroup()))));


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

			registry.add(CubliminalBiomes.HABITABLE_CORRIDORS_BIOME, HabitableCorridorsBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.AQUILA_SECTOR_BIOME, AquilaSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_AQUILA_SECTOR_BIOME, DeepAquilaSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.GILD_SECTOR_BIOME, GildSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_GILD_SECTOR_BIOME, DeepGildSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.GOTHIC_SECTOR_BIOME, GothicSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_GOTHIC_SECTOR_BIOME, DeepGothicSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.OUROBOROS_SECTOR_BIOME, OuroborosSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);
			registry.add(CubliminalBiomes.DEEP_OUROBOROS_SECTOR_BIOME, DeepOuroborosSectorBiome.create(features, carvers), RegistryEntryInfo.DEFAULT);

			RegistryNoisePreset.initNoisePresets(infoLookup);

		});
	}

	private static <W extends LimlibWorld> void getWorld(Identifier id, W world) {
		WORLDS.add(Pair.of(RegistryKey.of(LimlibWorld.LIMLIB_WORLD_KEY, id), world));
	}

	private static <E extends SoundEffects> void getSoundEffects(Identifier id, E soundEffects) {
		SOUND_EFFECTS.add(Pair.of(RegistryKey.of(SoundEffects.SOUND_EFFECTS_KEY, id), soundEffects));
	}

	private static <S extends Skybox> void getSkybox(Identifier id, S skybox) {
		SKYBOXES.add(Pair.of(RegistryKey.of(Skybox.SKYBOX_KEY, id), skybox));
	}

	private static <D extends LiminalDimensionEffects> void getDimEffects(Identifier id, D dimensionEffects) {
		DIMENSION_EFFECTS.add(Pair.of(RegistryKey.of(LiminalDimensionEffects.DIMENSION_EFFECTS_KEY, id), dimensionEffects));
	}

	private static <P extends PostEffect> void getPostEffects(Identifier id, P postEffect) {
		POST_EFFECTS.add(Pair.of(RegistryKey.of(PostEffect.POST_EFFECT_KEY, id), postEffect));
	}
}
