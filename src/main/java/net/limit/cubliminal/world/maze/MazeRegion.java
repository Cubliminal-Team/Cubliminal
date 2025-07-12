package net.limit.cubliminal.world.maze;

import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;

public abstract class MazeRegion<M extends MazeComponent> {

    protected final M[] floorStorage;
    protected final int layerHeight;

    public MazeRegion(int layerHeight, M[] floorStorage) {
        this.floorStorage = floorStorage;
        this.layerHeight = layerHeight;
    }

    protected final void decorateColumn(ChunkRegion region, BlockPos regionPos, int spacingX, int layerHeight, int spacingZ, BlockPos at, MazeRegionGenerator.Decorator<M> cellDecorator, long seedModifier) {
        int cellX = (at.getX() - regionPos.getX()) / spacingX;
        int cellZ = (at.getZ() - regionPos.getZ()) / spacingZ;
        for (int i = 0; i < floorStorage.length; i++) {
            M maze = floorStorage[i];
            if (maze != null) {
                BlockPos mazePos = regionPos.add(0, i * layerHeight, 0);
                BlockPos cellPos = new BlockPos(at.getX(), mazePos.getY(), at.getZ());
                cellDecorator.decorate(region, maze, mazePos, maze.cellState(cellX, cellZ), cellPos, Random.create(LimlibHelper.blockSeed(cellPos) + seedModifier + region.getSeed()));
            }
        }
    }
}
