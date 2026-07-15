package net.limit.cubliminal.world.maze;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import io.github.jdiemke.triangulation.Edge2D;
import net.limit.cubliminal.world.room.Door;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class LevelOneMaze extends SpecialMaze {

    private final boolean[] filter;
    private final float bias;
    private final boolean simple;
    private final Random random;
    private List<Edge2D> mst;
    private SetMultimap<Vec2i, Door.Instance> doors;

    public LevelOneMaze(int width, int height, boolean[] filter, float bias, boolean simple, Random random) {
        super(width, height);
        this.filter = filter;
        this.bias = bias;
        this.simple = simple;
        this.random = random;
    }

    @Override
    public void create() {
        this.connectDoors();
        if (simple) {
            this.generateCorridors();
        } else {
            this.generateHalls();
        }
    }

    private void generateCorridors() {
        // Generate maze using dfs (copy of Limlib's DepthFirstMaze)
        visit(Vec2i.ZERO);
        this.visitedCells++;
        this.stack.push(Vec2i.ZERO);

        while (!this.stack.isEmpty() && visitedCells < this.width * this.height) {
            List<Face> neighbours = Lists.newArrayList();

            for (Face face : Face.values()) {
                if (this.hasNeighbour(this.stack.peek(), face)) {
                    neighbours.add(face);
                }
            }

            if (!neighbours.isEmpty()) {
                Face nextFace = neighbours.get(random.nextInt(neighbours.size()));

                this.cellState(this.stack.peek()).go(nextFace);
                this.cellState(this.stack.peek().go(nextFace)).go(nextFace.mirror());
                this.visit(this.stack.peek().go(nextFace));
                this.stack.push(this.stack.peek().go(nextFace));

                this.visitedCells++;

            } else {
                this.stack.pop();
            }
        }
    }

    private void generateHalls() {
        if (mst == null) return;

        for (Edge2D edge : mst) {
            generateHall(
                    new Vec2i((int) edge.a.x, (int) edge.a.y),
                    new Vec2i((int) edge.b.x, (int) edge.b.y)
            );
        }
    }

    private void generateHall(Vec2i start, Vec2i end) {
        visitedCells++;
        visit(start);

        Stack<Vec2i> corridor = new Stack<>();
        stack.push(start);
        corridor.push(start);

        while (!corridor.isEmpty()) {
            Vec2i current = corridor.peek();

            if (current.equals(end)) {
                resetVisited();
                break;
            }

            Face next = getNextDirection(current, end);

            if (next == null) {
                stack.pop();
                corridor.pop();
                continue;
            }

            moveTo(current, next, corridor);
        }
    }

    private Face getNextDirection(Vec2i cell, Vec2i end) {
        List<Face> options = new ArrayList<>();
        List<Face> preferred = new ArrayList<>();

        int smallestDistance = Integer.MAX_VALUE;
        boolean followingPath = false;

        for (Face face : Face.values()) {
            if (!hasNeighbour(cell, face)) continue;

            int distance = manhattanDistance(cell.go(face), end);
            boolean visited = stack.contains(cell.go(face));

            if (distance <= smallestDistance) {
                if (distance < smallestDistance || (!followingPath && visited)) {
                    smallestDistance = distance;
                    preferred.clear();
                    preferred.add(face);
                    followingPath = visited;
                } else if (visited == followingPath) {
                    preferred.add(face);
                }
            }

            options.add(face);
        }

        if (options.isEmpty()) return null;

        if (followingPath || random.nextInt(8) > 0) {
            options = preferred;
        }

        return chooseDirection(cell, options);
    }

    private Face chooseDirection(Vec2i cell, List<Face> options) {
        Face direction = dir(cell);

        if (random.nextFloat() > bias && options.contains(direction)) {
            return direction;
        }

        return options.get(random.nextInt(options.size()));
    }

    private void moveTo(Vec2i cell, Face direction, Stack<Vec2i> corridor) {
        Vec2i next = cell.go(direction);

        cellState(cell).go(direction);
        cellState(next).go(direction.mirror());

        visit(next);

        stack.push(next);
        corridor.push(next);
        visitedCells++;
    }

    private void resetVisited() {
        for (CellState cellState : maze) {
            visit(cellState.getPosition(), false);
        }
    }

    @Override
    public boolean hasNeighbour(Vec2i vec, Face face) {
        Vec2i adj = vec.go(face);
        return super.hasNeighbour(vec, face) && !this.filter[adj.y() * this.width + adj.x()];
    }

    public void setMst(Collection<Edge2D> mst) {
        this.mst = new ArrayList<>(mst);
    }

    public void setDoors(SetMultimap<Vec2i, Door.Instance> doors) {
        this.doors = HashMultimap.create(doors);
    }

    public void connectDoors() {
        if (this.doors != null) {
            this.doors.forEach((doorPos, door) -> this.cellState(doorPos).go(door.facing().mirror()));
        }
    }
}
