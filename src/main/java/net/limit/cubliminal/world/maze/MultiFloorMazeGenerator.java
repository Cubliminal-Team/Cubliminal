package net.limit.cubliminal.world.maze;

import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;

import java.util.HashMap;

public class MultiFloorMazeGenerator<M extends MazeComponent> {
    private final HashMap<BlockPos, M> mazes = new HashMap<>(30);
    public final int width;
    public final int height;
    public final int thicknessX;
    public final int layerThickness;
    public final int thicknessZ;
    public final long seedModifier;

    public MultiFloorMazeGenerator(int width, int height, int thicknessX, int layerThickness, int thicknessZ, long seedModifier) {
        this.width = width;
        this.height = height;
        this.thicknessX = thicknessX;
        this.layerThickness = layerThickness;
        this.thicknessZ = thicknessZ;
        this.seedModifier = seedModifier;
    }

    public void generateMaze(BlockPos pos, ChunkRegion region, int worldHeight, MultiFloorMazeGenerator.MazeCreator<M> mazeCreator, MultiFloorMazeGenerator.CellDecorator<M> cellDecorator) {
        for (int x = 0; x < 16; ++x) {
            for (int y = pos.getY(); y < worldHeight; y += this.layerThickness) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos inPos = pos.add(x, y, z);
                    if (Math.floorMod(inPos.getX(), this.thicknessX) == 0 && Math.floorMod(inPos.getZ(), this.thicknessZ) == 0) {
                        BlockPos mazePos = new BlockPos(inPos.getX() - Math.floorMod(inPos.getX(), this.width * this.thicknessX), inPos.getY(), inPos.getZ() - Math.floorMod(inPos.getZ(), this.height * this.thicknessZ));
                        M maze;
                        if (this.mazes.containsKey(mazePos)) {
                            maze = this.mazes.get(mazePos);
                        } else {
                            maze = mazeCreator.newMaze(region, mazePos, this.width, this.height, Random.create(LimlibHelper.blockSeed(mazePos.getX(), mazePos.getY() + this.seedModifier + region.getSeed(), mazePos.getZ())));
                            this.mazes.put(mazePos, maze);
                        }

                        int mazeX = (inPos.getX() - mazePos.getX()) / this.thicknessX;
                        int mazeZ = (inPos.getZ() - mazePos.getZ()) / this.thicknessZ;
                        MazeComponent.CellState originCell = maze.cellState(mazeX, mazeZ);
                        cellDecorator.generate(region, inPos, mazePos, maze, originCell, new BlockPos(this.thicknessX, this.layerThickness, this.thicknessZ), Random.create(LimlibHelper.blockSeed(mazePos.getX(), mazePos.getY(), mazePos.getZ()) + this.seedModifier + region.getSeed()));
                    }
                }
            }
        }

    }

    @FunctionalInterface
    public interface MazeCreator<M extends MazeComponent> {
        M newMaze(ChunkRegion var1, BlockPos var2, int var3, int var4, Random var5);
    }

    @FunctionalInterface
    public interface CellDecorator<M extends MazeComponent> {
        void generate(ChunkRegion var1, BlockPos var2, BlockPos var3, M var4, MazeComponent.CellState var5, BlockPos var6, Random var7);
    }
}
