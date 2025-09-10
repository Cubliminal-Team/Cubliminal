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
        final int posX = pos.getX();
        final int posY = pos.getY();
        final int posZ = pos.getZ();
        for (int x = 0; x < 16; ++x) {
            int inX = posX + x;
            if (Math.floorMod(inX, spacingX) != 0) continue;

            for (int z = 0; z < 16; ++z) {
                int inZ = posZ + z;
                if (Math.floorMod(inZ, spacingZ) != 0) continue;

                BlockPos regionPos = new BlockPos(
                        inX - Math.floorMod(inX, width * spacingX),
                        posY - Math.floorMod(posY, layerHeight * layerCount),
                        inZ - Math.floorMod(inZ, height * spacingZ)
                );

                R mazeRegion = mazeRegions.computeIfAbsent(regionPos, posx -> regionCreator
                        .newRegion(
                                region, posx, width, height,
                                Random.create(LimlibHelper.blockSeed(posx) + seedModifier + region.getSeed())
                        )
                );

                mazeRegion.decorateColumn(region, regionPos, spacingX, layerHeight, spacingZ, new BlockPos(inX, posY, inZ), cellDecorator, seedModifier);
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
