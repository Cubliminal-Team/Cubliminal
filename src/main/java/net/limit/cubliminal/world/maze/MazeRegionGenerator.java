package net.limit.cubliminal.world.maze;

import net.limit.cubliminal.level.LevelWithMaze;
import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;

import java.util.HashMap;

public record MazeRegionGenerator<M extends MazeComponent, R extends MazeRegion<M>>
        (HashMap<BlockPos, R> mazeRegions, int width, int height,
         int spacingX, int layerHeight, int spacingZ, long seedModifier) {

    public static <M extends MazeComponent, R extends MazeRegion<M>> MazeRegionGenerator<M, R> create(LevelWithMaze level) {
        return new MazeRegionGenerator<>(
                new HashMap<>(15),
                level.maze_width,
                level.maze_height,
                level.spacing_x,
                level.layer_height,
                level.spacing_z,
                level.maze_seed_modifier
        );
    }

    public void generateMazeRegion(BlockPos pos, ChunkRegion region, int layerCount, RegionCreator<M, R> regionCreator, Decorator<M> cellDecorator) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                BlockPos inPos = pos.add(x, 0, z);
                if (Math.floorMod(inPos.getX(), spacingX) == 0 && Math.floorMod(inPos.getZ(), spacingZ) == 0) {
                    BlockPos regionPos = new BlockPos(
                            inPos.getX() - Math.floorMod(inPos.getX(), width * spacingX),
                            inPos.getY() - Math.floorMod(inPos.getY(), layerHeight * layerCount),
                            inPos.getZ() - Math.floorMod(inPos.getZ(), height * spacingZ)
                    );
                    R mazeRegion;
                    if (mazeRegions.containsKey(regionPos)) {
                        mazeRegion = mazeRegions.get(regionPos);
                    } else {
                        mazeRegion = regionCreator.newRegion(region, regionPos, width, height, Random.create(LimlibHelper.blockSeed(regionPos) + seedModifier + region.getSeed()));
                        mazeRegions.put(regionPos, mazeRegion);
                    }

                    mazeRegion.decorateColumn(region, regionPos, spacingX, layerHeight, spacingZ, inPos, cellDecorator, seedModifier);
                }
            }
        }
    }

    public BlockPos toRegionPos(BlockPos inPos) {
        return new BlockPos(
                inPos.getX() - Math.floorMod(inPos.getX(), width * spacingX),
                inPos.getY(),
                inPos.getZ() - Math.floorMod(inPos.getZ(), height * spacingZ)
        );
    }

    @FunctionalInterface
    public interface RegionCreator<M extends MazeComponent, R extends MazeRegion<M>> {
        R newRegion(ChunkRegion region, BlockPos regionPos, int width, int height, Random random);
    }

    @FunctionalInterface
    public interface Decorator<M extends MazeComponent> {
        void decorate(ChunkRegion region, M maze, BlockPos mazePos, CellState cellState, BlockPos cellPos, Random random);
    }
}
