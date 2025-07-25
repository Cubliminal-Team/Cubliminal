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
        if (mst != null) {
            for (Edge2D edge : mst) {
                Vec2i cell = new Vec2i((int) edge.a.x, (int) edge.a.y);
                Vec2i end = new Vec2i((int) edge.b.x, (int) edge.b.y);
                visitedCells++;
                this.visit(cell);
                Stack<Vec2i> corridor = new Stack<>();
                stack.push(cell);
                corridor.push(cell);
                while (!corridor.isEmpty()) {
                    // Reassign current cell
                    cell = corridor.peek();
                    // If it is the desired end, reassign and remove the new one
                    if (cell.equals(end)) {
                        for (CellState cellState : maze) {
                            this.visit(cellState.getPosition(), false);
                        }
                        break;
                    }

                    List<Face> neighbours = new ArrayList<>(4);
                    List<Face> optNeighbours = new ArrayList<>(4);
                    int smallestDistance = Integer.MAX_VALUE;
                    boolean followingPath = false;

                    for (Face face : Face.values()) {
                        if (this.hasNeighbour(cell, face)) {
                            // Create two lists: one including the available neighbours and another the closest to the end
                            int distance = this.manhattanDistance(cell.go(face), end);
                            if (distance <= smallestDistance) {
                                // If a single cell is in the stack, remove those that aren't
                                boolean visited = stack.contains(cell.go(face));
                                if (distance < smallestDistance || (!followingPath && visited)) {
                                    smallestDistance = distance;
                                    optNeighbours.clear();
                                    optNeighbours.add(face);
                                    followingPath = visited;
                                } else if (visited == followingPath) {
                                    optNeighbours.add(face);
                                }
                            }
                            neighbours.add(face);
                        }
                    }

                    if (!neighbours.isEmpty()) {
                        // If a short path has already been generated, follow it
                        if (followingPath || random.nextInt(8) > 0) neighbours = optNeighbours;

                        Face nextFace;
                        // Determine whether the next cell is going to continue straight ahead
                        if (random.nextFloat() > bias && neighbours.contains(this.dir(cell))) {
                            nextFace = this.dir(cell);
                        } else {
                            nextFace = neighbours.get(random.nextInt(neighbours.size()));
                        }

                        Vec2i nextCell = cell.go(nextFace);
                        this.cellState(cell).go(nextFace);
                        this.cellState(nextCell).go(nextFace.mirror());
                        this.visit(nextCell);
                        stack.push(nextCell);
                        corridor.push(nextCell);
                        visitedCells++;
                    } else {
                        stack.pop();
                        corridor.pop();
                    }
                }
            }
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
