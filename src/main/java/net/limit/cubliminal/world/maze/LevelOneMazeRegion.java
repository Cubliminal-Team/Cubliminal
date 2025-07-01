package net.limit.cubliminal.world.maze;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.Edge2D;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Vector2D;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.init.CubliminalBiomes;
import net.limit.cubliminal.world.biome.source.LevelOneBiomeSource;
import net.limit.cubliminal.world.placement.MSTree;
import net.limit.cubliminal.world.placement.PoissonDiskSampler;
import net.limit.cubliminal.world.room.Door;
import net.limit.cubliminal.world.room.Room;
import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Vec2i;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;

import java.util.*;

public class LevelOneMazeRegion extends MazeRegion<LevelOneMaze> {

    private final boolean[][] floorCache;
    private RegistryEntry<Biome>[] biomeGrid;
    private List<Vec2i> validRoomPos;
    private final Int2ObjectOpenHashMap<Set<Vec2i>> structurePositions;
    private final Int2ObjectOpenHashMap<Set<Room.Instance>> structures;

    public LevelOneMazeRegion(int width, int layerHeight, int height, int floorCount) {
        super(layerHeight, new LevelOneMaze[floorCount]);
        this.floorCache = new boolean[floorCount][width * height];
        this.structurePositions = new Int2ObjectOpenHashMap<>(floorCount);
        this.structures = new Int2ObjectOpenHashMap<>(floorCount);
        for (int i = 0; i < floorCount; i++) {
            this.structurePositions.put(i, new HashSet<>(3));
            this.structures.put(i, new HashSet<>(3));
        }
    }

    public void cacheRoom(Vec3i cell, int width, Room.Instance room) {
        boolean[] roomCache = floorCache[cell.getY()];
        for (int row = 0; row < room.height(); row++) {
            for (int column = 0; column < room.width(); column++) {
                roomCache[(column + cell.getZ()) * width + row + cell.getX()] = true;
            }
        }
        //structurePositions.get(cell.getY()).add(new Vec2i(cell));
        //structures.get(cell.getY()).add(room);
    }

    public void generateMazes(LevelOneBiomeSource biomeSource, PoissonDiskSampler sampler, ChunkRegion region, BlockPos regionPos,
                              int width, int height, int thicknessX, int thicknessZ, long seedModifier, Random random) {
        if (generated) {
            throw new IllegalStateException("Maze region: " + regionPos + " was generated twice");
        }
        for (int i = 0; i < floorStorage.length; i++) {
            BlockPos mazePos = regionPos.add(0, i * layerHeight, 0);
            Random mazeRandom = Random.create(LimlibHelper.blockSeed(mazePos) + seedModifier + region.getSeed());
            floorStorage[i] = this.generateMaze(biomeSource, sampler, region, i, mazePos, width, height, thicknessX, thicknessZ, mazeRandom);
        }
        biomeGrid = null;
        validRoomPos = null;
        generated = true;
    }

    @SuppressWarnings("unchecked")
    private LevelOneMaze generateMaze(LevelOneBiomeSource biomeSource, PoissonDiskSampler sampler, ChunkRegion region, int floor,
                                      BlockPos mazePos, int width, int height, int thicknessX, int thicknessZ, Random random) {
        // Cache per-cell biome
        if (biomeGrid == null && validRoomPos == null) {
            this.biomeGrid = new RegistryEntry[width * height];
            this.validRoomPos = new ArrayList<>(width * height);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    RegistryEntry<Biome> biome = biomeSource.calcBiome(x * thicknessX + mazePos.getX(), mazePos.getY(), y * thicknessZ + mazePos.getZ());
                    if (biome.isIn(CubliminalBiomes.DEEP_LEVEL_ONE)) {
                        biomeGrid[y * width + x] = biome;
                        validRoomPos.add(new Vec2i(x, y));
                    }
                }
            }
        }
        Collections.shuffle(validRoomPos, new java.util.Random(LimlibHelper.blockSeed(mazePos)));

        // Run poisson disk sampler to find a position for each room
        List<Room.Instance> roomInstances = new ArrayList<>();
        boolean[] roomCache = floorCache[floor];
        List<Vec2i> roomPositions = sampler.generate(roomInstances, roomCache, biomeGrid, validRoomPos, random);
        roomPositions.addAll(structurePositions.get(floor));
        roomInstances.addAll(structures.get(floor));
        Set<Vector2D> nodes = new HashSet<>();
        SetMultimap<Vec2i, Door.Instance> doors = HashMultimap.create();
        LevelOneMaze maze = new LevelOneMaze(width, height, roomCache, 0.2f, random);

        // Mark room origin cells for generation and add their doors' positions as nodes
        if (roomInstances.size() == roomPositions.size()) {
            for (int i = 0; i < roomInstances.size(); i++) {
                Room.Instance room = roomInstances.get(i);
                Vec2i roomPos = roomPositions.get(i);
                room.place(maze, roomPos.x(), roomPos.y())
                        .forEach(door -> {
                            Door.Instance instance = Door.Instance.of(roomPos, door.facing());
                            Vec2i vec = instance.entry(
                                    door.relativePos().x() + roomPos.x(),
                                    door.relativePos().y() + roomPos.y());
                            Vector2D entryPos = new Vector2D(vec.x(), vec.y());
                            doors.put(vec, instance);
                            nodes.add(entryPos);
                        });
            }
        }

        // Add connections to other mazes in form of doors
        List<Vector2D> connections = this.addConnections(mazePos, width, height, thicknessX, thicknessZ);
        for (int i = 0; i < connections.size(); ++i) {
            MazeComponent.Face face = MazeComponent.Face.values()[i];
            Vector2D vec = connections.get(i);
            Vec2i entryPos = new Vec2i((int) vec.x, (int) vec.y);
            doors.put(entryPos, new Door.Instance(entryPos, face.mirror()));
            nodes.add(vec);
        }

        // Triangulate room positions to create a graph-based layout
        try {
            DelaunayTriangulator triangulator = new DelaunayTriangulator(nodes.stream().toList());
            triangulator.triangulate();
            // Collect all the unique edges
            List<Edge2D> mst = MSTree.buildCorridors(nodes, doors, triangulator.getTriangles(), connections, random);
            maze.setMst(mst);
            maze.setDoors(doors);
            maze.generateMaze();
        } catch (NotEnoughPointsException e) {
            Cubliminal.LOGGER.error(e.getMessage());
        }

        return maze;
    }

    private List<Vector2D> addConnections(BlockPos mazePos, int width, int height, int thicknessX, int thicknessZ) {
        Random randomUp = Random.create(LimlibHelper.blockSeed(mazePos.add(height * thicknessZ - 1, 0, 0)));
        Random randomDown = Random.create(LimlibHelper.blockSeed(mazePos.add(-1, 0, 0)));
        Random randomLeft = Random.create(LimlibHelper.blockSeed(mazePos));
        Random randomRight = Random.create(LimlibHelper.blockSeed(mazePos.add(0, 0, width * thicknessX)));
        List<Vector2D> connections = new ArrayList<>();
        // East
        connections.add(new Vector2D(width - 1, randomUp.nextInt(height)));
        // West
        connections.add(new Vector2D(0, randomDown.nextInt(height)));
        // North
        connections.add(new Vector2D(randomLeft.nextInt(width), 0));
        // South
        connections.add(new Vector2D(randomRight.nextInt(width), height - 1));
        return connections;
    }
}