package net.limit.cubliminal.world.chunk;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.custom.template.BlockVariantHolder;
import net.limit.cubliminal.init.CubliminalBiomes;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.limit.cubliminal.init.CubliminalRegistrar;
import net.limit.cubliminal.access.ChunkAccessor;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.level.LevelWithMaze;
import net.limit.cubliminal.world.biome.source.LevelOneBiomeSource;
import net.limit.cubliminal.world.maze.*;
import net.limit.cubliminal.world.placement.PoissonDiskSampler;
import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.ludocrypt.limlib.api.world.NbtGroup;
import net.ludocrypt.limlib.api.world.chunk.AbstractNbtChunkGenerator;
import net.ludocrypt.limlib.api.world.maze.*;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LevelOneChunkGenerator extends AbstractNbtChunkGenerator implements BackroomsLevel {
	public static final MapCodec<LevelOneChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			LevelOneBiomeSource.CODEC.fieldOf("biome_source").stable().forGetter(chunkGenerator -> chunkGenerator.biomeSource),
			NbtGroup.CODEC.fieldOf("group").stable().forGetter(chunkGenerator -> chunkGenerator.nbtGroup),
			LevelWithMaze.LEVEL_WITH_MAZE_CODEC.fieldOf("level").stable().forGetter(chunkGenerator -> chunkGenerator.level)
	).apply(instance, instance.stable(LevelOneChunkGenerator::new)));

	private final LevelOneBiomeSource biomeSource;
	private final MazeRegionGenerator<LevelOneMaze, LevelOneMazeRegion> mazeGenerator;
    private final PoissonDiskSampler poissonDiskSampler;
	private final LevelWithMaze level;
	private final int spacingX;
	private final int layerHeight;
	private final int spacingZ;
	private final int layerCount;

    public LevelOneChunkGenerator(LevelOneBiomeSource biomeSource, NbtGroup group, LevelWithMaze level) {
		super(biomeSource, group);
		this.biomeSource = biomeSource;
		this.level = level;
        this.mazeGenerator = MazeRegionGenerator.create(level);
		this.spacingX = level.spacing_x;
		this.layerHeight = level.layer_height;
		this.spacingZ = level.spacing_z;
		this.layerCount = level.layer_count;
		this.poissonDiskSampler = new PoissonDiskSampler(level.maze_width, level.maze_height, 30);
	}

	public static NbtGroup createGroup() {
		return NbtGroup.Builder
				.create(Cubliminal.id(CubliminalRegistrar.HABITABLE_ZONE))
				.with("corridor_dark", "corridor_dark_i", "corridor_dark_l", "corridor_dark_f", "corridor_dark_t", "corridor_dark_n")
				.with("corridor_i_normal", 1, 6)
				.with("corridor_i_variated", "fire_alarm", "open_1", "open_2", "pipes", "room_1", "room_2", "wall_1", "wall_2",
						"remains_1", "remains_2", "remains_3", "remains_4", "remains_5", "remains_6", "remains_7", "smoke_detector_1", "smoke_detector_2",
						"tent_1", "tent_2", "tent_3", "tent_4", "window_1", "window_2")
				.with("corridor_l_normal", 1, 6)
				.with("corridor_l_variated", "remains_1", "remains_2", "remains_3", "remains_4", "remains_5", "remains_6", "remains_7")
				.with("corridor_f_normal", 1, 6)
				.with("corridor_f_variated", "remains_1", "remains_2", "remains_3", "remains_4", "remains_5", "remains_6", "remains_7")
				.with("corridor_t_normal", 1, 6)
				.with("corridor_t_variated", "remains_1", "remains_2", "remains_3", "remains_4", "remains_5", "remains_6", "remains_7")
				.with("corridor_n_normal", 1, 5)
				.with("corridor_n_variated", "remains_1", "remains_2", "remains_3", "remains_4", "remains_5", "remains_6", "remains_7")
				.with("corridor_door", "corridor_door_i", "corridor_door_l", "corridor_door_f", "corridor_door_t", "corridor_door_n")
				.with("f", "aquila_sector_f")
				.with("i_aquila", "normal", "doorway", "door", "corridor", "pipes", "sludge", "painting", "maintenance_on", "maintenance_off",
						"damaged_1", "damaged_2", "high_1", "no_lights", "sus_door_1", "fire_alarm_button", "fuse_box", "column_1", "column_2")
				.with("l", "aquila_sector_l")
				.with("n", "aquila_sector_n")
				.with("t", "aquila_sector_t")
				.with("parking", 1, 10)
				.with("ramp", "n_1", "n_2", "n_3", "s_1", "s_2", "s_3", "w_1", "w_2", "w_3", "e_1", "e_2", "e_3")
				.with("entrance")
				.with("room", "room_1_0", "room_2_0", "room_2_1", "room_3_0", "room_3_1", "small", "medium", "pk_0", "pk_1", "pk_2", "pk_3")
				.with("connection", "test_connection_0", "test_connection_1", "corridor_connection_1_0", "corridor_connection_1_1")
				.build();
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	public LevelOneMazeRegion createRegion(ChunkRegion region, BlockPos regionPos, int width, int height, Random random) {
        LevelOneMazeRegion mazeRegion = new LevelOneMazeRegion(layerHeight, layerCount);
		mazeRegion.generateMazes(
				biomeSource, poissonDiskSampler, region, regionPos, width, layerCount,
				height, spacingX, spacingZ, level.maze_seed_modifier, random
		);
		return mazeRegion;
	}

	public void decorateMaze(ChunkRegion region, LevelOneMaze maze, BlockPos mazePos, CellState cellState, BlockPos cellPos, Random random) {
		BlockPos placementPos = cellPos.up();
		if (cellState instanceof SpecialCellState special) {
			special.decorate(manipulation -> generateNbt(region, placementPos, special.nbtId(nbtGroup, random), manipulation));
		} else {
			Pair<MazePiece, Manipulation> piece = MazePiece.getFromCell(cellState, random);
			MazePiece type = piece.getFirst();
			if (type != MazePiece.E) {
				RegistryKey<Biome> biome = biomeSource.calcBiome(placementPos).getKey().orElseThrow();
				if (biome.equals(CubliminalBiomes.HABITABLE_CORRIDORS_BIOME)) {
					float r = random.nextFloat();
					if (r > 0.3) {
						generateNbt(region, placementPos, nbtGroup.pick("corridor_" + type.getAsLetter() + "_normal", random), piece.getSecond());
					} else if (r > 0.12) {
						generateNbt(region, placementPos, nbtGroup.nbtId("corridor_door", "corridor_door_" + type.getAsLetter()), piece.getSecond());
					} else if (r > 0.8) {
						generateNbt(region, placementPos, nbtGroup.nbtId("corridor_dark", "corridor_dark_" + type.getAsLetter()), piece.getSecond());
					} else {
						generateNbt(region, placementPos, nbtGroup.pick("corridor_" + type.getAsLetter() + "_variated", random), piece.getSecond());
					}
				} else {
					if (type == MazePiece.I) {
						generateNbt(region, placementPos, random.nextFloat() < 0.1
										? this.nbtGroup.pick("i_aquila", random)
										: this.nbtGroup.nbtId("i_aquila", "normal"),
								piece.getSecond());
					} else {
						generateNbt(region, placementPos, this.nbtGroup.pick(piece.getFirst().getAsLetter(), random), piece.getSecond());
					}
				}
			}
		}
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(ChunkRegion region, ChunkGenerationContext context,
												  BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
		BlockPos startPos = chunk.getPos().getStartPos();
		int startX = startPos.getX();
		int startY = startPos.getY();
		int startZ = startPos.getZ();
        // Fill air gaps
		Random chunkRandom = Random.create(LimlibHelper.blockSeed(startX, startY, startZ));
		BlockPos.Mutable mutable = startPos.mutableCopy();
		for (int dy = 0; dy < this.getWorldHeight(); dy++) {
			mutable.setY(startY + dy);
			for (int dx = 0; dx < 16; dx++) {
				mutable.setX(startX + dx);
				for (int dz = 0; dz < 16; dz++) {
					mutable.setZ(startZ + dz);
					region.setBlockState(mutable, chunkRandom.nextFloat() < 0.3 ? Blocks.BLACKSTONE.getDefaultState() : Blocks.DEEPSLATE.getDefaultState(), Block.FORCE_STATE, 0);
				}
			}
		}

		// Indestructible floor
		for (int layer = 0; layer < layerCount; layer++) {
			mutable.setY(startY + layer * layerHeight);
			for (int dx = 0; dx < 16; dx++) {
				mutable.setX(startX + dx);
				for (int dz = 0; dz < 16; dz++) {
					mutable.setZ(startZ + dz);
					region.setBlockState(mutable, CubliminalBlocks.GABBRO.getDefaultState(), Block.FORCE_STATE, 0);
				}
			}
		}
		mutable.setY(this.getWorldHeight() - 1);
		for (int dx = 0; dx < 16; dx++) {
			mutable.setX(startX + dx);
			for (int dz = 0; dz < 16; dz++) {
				mutable.setZ(startZ + dz);
				region.setBlockState(mutable, CubliminalBlocks.GABBRO.getDefaultState(), Block.FORCE_STATE, 0);
			}
		}

		this.mazeGenerator.generateMazeRegion(startPos, region, layerCount, this::createRegion, this::decorateMaze);

		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
		return CompletableFuture.supplyAsync(() -> {
			((ChunkAccessor) chunk).cubliminal$sectionPopulateBiomes(this.biomeSource);
			return chunk;
		}, Util.getMainWorkerExecutor().named("init_biomes"));
	}

	@Override
	protected void modifyStructure(ChunkRegion region, BlockPos pos, BlockState state, Optional<NbtCompound> blockEntityNbt, int update) {
		if (state.isAir()) {
			return;
		}

		super.modifyStructure(region, pos, state, blockEntityNbt, update);

		if (state.getBlock() instanceof BlockVariantHolder holder) {
			holder.changeToVariant(region, state, pos);
		} else if (state.isOf(CubliminalBlocks.CRACKED_WHITE_BRICKS)) {
			if (blockSeed(region, pos).nextFloat() > 0.4) {
				region.setBlockState(pos, CubliminalBlocks.WHITE_BRICKS.getDefaultState(), Block.FORCE_STATE);
			}
		} else if (state.isOf(CubliminalBlocks.WET_GRAY_ASPHALT)) {
			if (blockSeed(region, pos).nextFloat() > 0.3) {
				region.setBlockState(pos, CubliminalBlocks.GRAY_ASPHALT.getDefaultState(), Block.FORCE_STATE);
			}
		} else if (state.isOf(Blocks.DIORITE)) {
			if (blockSeed(region, pos).nextFloat() > 0.7) {
				region.setBlockState(pos, CubliminalBlocks.WHITE_BRICKS.getDefaultState(), Block.FORCE_STATE);
			}
		} else if (state.isOf(CubliminalBlocks.WOODEN_PLANK)) {
			if (blockSeed(region, pos).nextFloat() > 0.5) {
				region.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
			}
		} else if (state.isOf(Blocks.CRIMSON_TRAPDOOR)) {
			region.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
		}
	}

	@Override
	protected RegistryKey<LootTable> getContainerLootTable(LootableContainerBlockEntity container) {
		return container.getLootTable();
	}

	@Override
	public int getPlacementRadius() {
		return 6;
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
