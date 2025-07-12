package net.limit.cubliminal.world.chunk;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.custom.template.RotatableLightBlock;
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
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
				.with("f", 1, 1)
				.with("i", 1, 1)
				.with("l", 1, 1)
				.with("n", 1, 1)
				.with("t", 1, 1)
				.with("e", 1, 1)
				.with("parking", 1, 10)
				.with("ramp", "n_1", "n_2", "n_3", "s_1", "s_2", "s_3", "w_1", "w_2", "w_3", "e_1", "e_2", "e_3")
				.with("entrance")
				.with("room", "room_1_0", "room_2_0", "room_2_1", "room_3_0", "room_3_1", "small", "medium", "pk_0", "pk_1", "pk_2", "pk_3")
				.with("connection", "test_connection", 0, 1)
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
		if (cellState instanceof SpecialCellState special) {
			special.decorate(manipulation -> generateNbt(region, cellPos, special.nbtId(nbtGroup, random), manipulation));
		} else {
			Pair<MazePiece, Manipulation> piece = MazePiece.getFromCell(cellState, random);
			if (piece.getFirst() != MazePiece.E) {
				generateNbt(region, cellPos, this.nbtGroup.pick(piece.getFirst().getAsLetter(), random), piece.getSecond());
			}
		}
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(ChunkRegion region, ChunkGenerationContext context,
												  BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
		BlockPos startPos = chunk.getPos().getStartPos();
		this.mazeGenerator.generateMazeRegion(startPos, region, layerCount, this::createRegion, this::decorateMaze);
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
		return CompletableFuture.supplyAsync(() -> {
			((ChunkAccessor) chunk).cubliminal$populateBiomes(this.biomeSource);
			return chunk;
		}, Util.getMainWorkerExecutor().named("init_biomes"));
	}

	@Override
	protected void modifyStructure(ChunkRegion region, BlockPos pos, BlockState state, Optional<NbtCompound> blockEntityNbt, int update) {
		if (state.isAir()) {
			return;
		}

		super.modifyStructure(region, pos, state, blockEntityNbt, update);

		Supplier<Random> random = () -> Random.create(region.getSeed() + LimlibHelper.blockSeed(pos));

		if (state.isOf(Blocks.TUFF_BRICKS)) {
			if (random.get().nextFloat() > 0.8) {
				region.setBlockState(pos, Blocks.POLISHED_TUFF.getDefaultState(), 0);
			}
		} else if (state.isOf(Blocks.YELLOW_CONCRETE)) {
			if (random.get().nextFloat() < 0.8) {
				region.setBlockState(pos, Blocks.STONE.getDefaultState(), 0);
			} else {
				region.setBlockState(pos, Blocks.ANDESITE.getDefaultState(), 0);
			}
		} else if (state.isOf(CubliminalBlocks.WET_GRAY_ASPHALT)) {
			if (random.get().nextFloat() > 0.4) {
				region.setBlockState(pos, CubliminalBlocks.GRAY_ASPHALT.getDefaultState(), 0);
			}
		} else if (state.getBlock() instanceof RotatableLightBlock) {
			if (random.get().nextFloat() > 0.9) {
				region.setBlockState(pos, state.with(Properties.LIT, false), 0);
			}
		} else if (state.isOf(CubliminalBlocks.SMOKE_DETECTOR)) {
			if (random.get().nextFloat() > 0.1) {
				region.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
			}
		} else if (state.isOf(Blocks.ANDESITE_STAIRS)) {
			if (random.get().nextFloat() > 0.5) {
				region.setBlockState(pos, Blocks.ANDESITE.getDefaultState(), 0);
			}
		} else if (state.isOf(Blocks.STONE_STAIRS)) {
			if (random.get().nextFloat() > 0.5) {
				region.setBlockState(pos, Blocks.STONE.getDefaultState(), 0);
			}
		} else if (state.isOf(Blocks.BLUE_CONCRETE)) {
			if (random.get().nextFloat() > 0.03) {
				region.setBlockState(pos, Blocks.STONE.getDefaultState(), 0);
			} else {
				region.setBlockState(pos, Blocks.WATER.getDefaultState(), 0);
				region.scheduleFluidTick(pos, Fluids.WATER, 0);
			}
		} else if (state.isOf(CubliminalBlocks.WOODEN_CRATE)) {
			if (random.get().nextFloat() > 0.7) {
				region.setBlockState(pos, Blocks.DARK_OAK_PLANKS.getDefaultState(), 0);
			}
		}
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
