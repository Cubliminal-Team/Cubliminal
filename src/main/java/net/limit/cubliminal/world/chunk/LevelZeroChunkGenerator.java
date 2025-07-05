package net.limit.cubliminal.world.chunk;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.CustomProperties;
import net.limit.cubliminal.init.CubliminalBiomes;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.limit.cubliminal.init.CubliminalRegistrar;
import net.limit.cubliminal.access.ChunkAccessor;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.world.biome.source.SimplexBiomeSource;
import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.ludocrypt.limlib.api.world.NbtGroup;
import net.ludocrypt.limlib.api.world.chunk.AbstractNbtChunkGenerator;
import net.minecraft.block.*;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class LevelZeroChunkGenerator extends AbstractNbtChunkGenerator implements BackroomsLevel {
	public static final MapCodec<LevelZeroChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			SimplexBiomeSource.CODEC.codec().fieldOf("biome_source").stable().forGetter(chunkGenerator -> chunkGenerator.biomeSource),
			NbtGroup.CODEC.fieldOf("group").stable().forGetter(chunkGenerator -> chunkGenerator.nbtGroup),
			Level.LEVEL_CODEC.fieldOf("level").stable().forGetter(chunkGenerator -> chunkGenerator.level)
	).apply(instance, instance.stable(LevelZeroChunkGenerator::new)));

	private final SimplexBiomeSource biomeSource;
	private final Level level;
	private final int layerCount;
	private final int layerHeight;
	private final int thicknessX;
	private final int thicknessZ;

	public LevelZeroChunkGenerator(SimplexBiomeSource biomeSource, NbtGroup group, Level level) {
		super(biomeSource, group);
		this.biomeSource = biomeSource;
		this.level = level;
		this.layerCount = level.layer_count;
		this.layerHeight = level.layer_height;
		this.thicknessX = level.spacing_x;
		this.thicknessZ = level.spacing_z;
	}

	public static NbtGroup createGroup() {
		return NbtGroup.Builder
				.create(Cubliminal.id(CubliminalRegistrar.THE_LOBBY))
				.with("0column", 1, 2)
				.with("0corner", 1, 1)
				.with("0corridor", 1, 1)
				.with("0space", 1, 1)
				.with("0thickcorner", 1, 1)
				.with("0thickwall", 1, 1)
				.with("0tinywall", 1, 2)
				.with("0twowalls", 1, 2)
				.with("0wall", 1, 1)
				.with("manila_room")
				.with("pillars", 1, 1)
				.with("r_column", 1, 2)
				.with("r_corner", 1, 1)
				.with("r_corridor", 1, 2)
				.with("r_space", 1, 1)
				.with("r_thickcorner", 1, 1)
				.with("r_thickwall", 1, 1)
				.with("r_twowalls", 1, 3)
				.with("r_wall", 1, 1)
				.with("special", 1, 4)
			.build();
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

    private void decorateLobby(ChunkRegion region, BlockPos pos) {
		Random random = Random.create(region.getSeed() + LimlibHelper.blockSeed(pos));
		int randomInt = random.nextInt(15999);

		if (randomInt < 3200) {
			// 1 : 5
			generateNbt(region, pos, nbtGroup.nbtId("0space", "0space_1"));
		} else if (randomInt < 5760) {
			// 1 : 5
			generateNbt(region, pos, nbtGroup.pick(nbtGroup
				.chooseGroup(random, "0column", "0corridor"), random), Manipulation.random(random));
		} else if (randomInt < 15880) {
			// 79 : 80
			generateNbt(region, pos, nbtGroup.pick(nbtGroup
				.chooseGroup(random, "0corner", "0wall",
					 "0thickcorner", "0thickwall", "0twowalls"), random), Manipulation.random(random));
		} else {
			generateNbt(region, pos, nbtGroup.pick("0tinywall", random), Manipulation.random(random));
		}
	}

	private void decorateRedrooms(ChunkRegion region, BlockPos pos) {
		Random random = Random.create(region.getSeed() + LimlibHelper.blockSeed(pos));
		int randomInt = random.nextInt(25);

		if (randomInt < 5) {
			// 1 : 5
			generateNbt(region, pos, nbtGroup.nbtId("r_space", "r_space_1"));
		} else if (randomInt < 9) {
			// 1 : 5
			generateNbt(region, pos, nbtGroup.pick(nbtGroup
					.chooseGroup(random, "r_column", "r_corridor"), random), Manipulation.random(random));
		} else {
			generateNbt(region, pos, nbtGroup.pick(nbtGroup
					.chooseGroup(random, "r_corner", "r_wall",
							"r_thickcorner", "r_thickwall", "r_twowalls"), random), Manipulation.random(random));
		}
	}

	@Override
	public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
		return CompletableFuture.supplyAsync(() -> {
			((ChunkAccessor) chunk).cubliminal$populateBiomes(this.biomeSource);
			return chunk;
		}, Util.getMainWorkerExecutor().named("init_biomes"));
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(ChunkRegion region, ChunkGenerationContext context, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
		BlockPos startPos = chunk.getPos().getStartPos();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				region.setBlockState(startPos.add(x, 0, z), CubliminalBlocks.GABBRO.getDefaultState(), 0);
			}
		}

		if (startPos.equals(BlockPos.ZERO)) {
			generateNbt(region, startPos.up(layerHeight * layerCount + 1), nbtGroup.nbtId("manila_room", "manila_room"));
		}

		RegistryEntry<Biome> biome = this.biomeSource.calcBiome(startPos);

		if (biome.matchesKey(CubliminalBiomes.PILLAR_BIOME)) {
			for (int layer = 0; layer < this.layerCount; layer++) {
				BlockPos placingPos = startPos.up(layer * this.layerHeight + 1);
				Random random = Random.create(region.getSeed() + LimlibHelper.blockSeed(placingPos));
				generateNbt(region, placingPos, nbtGroup.pick("pillars", random));
			}
		} else {
			boolean redrooms = biome.matchesKey(CubliminalBiomes.REDROOMS_BIOME);
			for (int layer = 0; layer < this.layerCount; layer++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						BlockPos placingPos = startPos.add(x, layer * this.layerHeight + 1, z);
						if (Math.floorMod(placingPos.getX(), this.thicknessX) == 0 && Math.floorMod(placingPos.getZ(), this.thicknessZ) == 0) {
							if (!redrooms) {
								decorateLobby(region, placingPos);
							} else {
								decorateRedrooms(region, placingPos);
							}
						}
					}
				}
			}
		}

		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	protected void modifyStructure(ChunkRegion region, BlockPos pos, BlockState state, Optional<NbtCompound> blockEntityNbt) {
		if (state.isAir() || state.isOf(Blocks.LIGHT)) {
			return;
		}

		super.modifyStructure(region, pos, state, blockEntityNbt);

		Supplier<Random> random = () -> Random.create(region.getSeed() + LimlibHelper.blockSeed(pos));

		if (state.isOf(CubliminalBlocks.FLUORESCENT_LIGHT)) {
			if (random.get().nextFloat() > 0.9 || region.getStatesInBox(new Box(pos).expand(1))
					.anyMatch(blockState -> blockState.isOf(CubliminalBlocks.FUSED_FLUORESCENT_LIGHT))) {
				region.setBlockState(pos, CubliminalBlocks.FUSED_FLUORESCENT_LIGHT.getDefaultState()
						.with(HorizontalFacingBlock.FACING, state.get(HorizontalFacingBlock.FACING))
								.with(CustomProperties.RED, state.get(CustomProperties.RED)), 0);
			} else if (random.get().nextFloat() < 0.1) {
				region.setBlockState(pos, CubliminalBlocks.FLICKERING_FLUORESCENT_LIGHT.getDefaultState()
						.with(HorizontalFacingBlock.FACING, state.get(HorizontalFacingBlock.FACING))
								.with(CustomProperties.RED, state.get(CustomProperties.RED)), 0);
			}
		} else if (state.isOf(CubliminalBlocks.SOCKET)) {
			if (random.get().nextFloat() < 0.9) {
				region.setBlockState(pos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 3), 0);
			}
		} else if (state.isOf(CubliminalBlocks.DAMAGED_YELLOW_WALLPAPERS)) {
			if (random.get().nextFloat() < 0.95) {
				region.setBlockState(pos, CubliminalBlocks.YELLOW_WALLPAPERS.getDefaultState(), 0);
			}
		} else if (state.isOf(CubliminalBlocks.DIRTY_DAMP_CARPET)) {
			if (random.get().nextFloat() < 0.95) {
				region.setBlockState(pos, CubliminalBlocks.DAMP_CARPET.getDefaultState(), 0);
			}
		} else if (state.isOf(CubliminalBlocks.SMOKE_DETECTOR)) {
			if (random.get().nextFloat() < 0.9) {
				region.setBlockState(pos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 3), 0);
			}
		} else if (state.isOf(Blocks.BROWN_MUSHROOM)) {
			float randomFloat = random.get().nextFloat();
			if (randomFloat > 0.9) {
				region.setBlockState(pos, Blocks.RED_MUSHROOM.getDefaultState(), 0);
			} else if (randomFloat > 0.1) {
				region.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
			}
		} else if (state.isOf(CubliminalBlocks.MOLD)) {
			if (random.get().nextFloat() < 0.9) {
				region.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
			}
		}
	}

	@Override
	protected RegistryKey<LootTable> getContainerLootTable(LootableContainerBlockEntity container) {
		if (container.getLootTable() != null) {
			return RegistryKey.of(RegistryKeys.LOOT_TABLE, Cubliminal.id("barrels/the_lobby/0"));
		} else {
			return null;
		}
	}

	@Override
	public int getPlacementRadius() {
		return 1;
	}

	@Override
	public int getMinimumY() {
		return this.level.min_y;
	}

	@Override
	public int getWorldHeight() {
		return this.level.world_height;
	}

	@Override
	public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
	}

	@Override
	public Level getLevel() {
		return this.level;
	}
}
