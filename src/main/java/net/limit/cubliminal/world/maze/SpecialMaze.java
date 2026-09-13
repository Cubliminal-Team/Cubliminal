package net.limit.cubliminal.world.maze;

import net.ludocrypt.limlib.api.world.maze.DepthLikeMaze;

public abstract class SpecialMaze extends DepthLikeMaze {

    public SpecialMaze(int mazeSizeX, int mazeSizeZ) {
        super(mazeSizeX, mazeSizeZ);
    }

    public void withState(int x, int z, CellState cellState) {
        if (cellState instanceof SpecialCellState) {
            this.maze[z * this.width + x] = cellState;
        }
    }

    public int manhattanDistance(Vec2i start, Vec2i end) {
        return Math.abs(end.x() - start.x()) + Math.abs(end.y() - start.y());
    }

    public Face dir(Vec2i vec) {
        return cellState(vec).getExtra().containsKey("dir") ? Face.values()[cellState(vec).getExtra().get("dir").getByte("dir")] : null;
    }
}