package net.limit.cubliminal.world.maze;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.Edge2D;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Vector2D;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;

import java.util.*;

public class LevelOneMazeRegion extends MazeRegion<LevelOneMaze> {

    private RegistryEntry<Biome>[] biomeGrid;
    private List<Vec2i> validRoomPos;
    private final Int2ObjectOpenHashMap<Object2ObjectArrayMap<Vec2i, Room.Instance>> connectionCache;

    public LevelOneMazeRegion(int layerHeight, int floorCount) {
        super(layerHeight, new LevelOneMaze[floorCount]);
        this.connectionCache = new Int2ObjectOpenHashMap<>(floorCount);
    }

    public void generateMazes(LevelOneBiomeSource biomeSource, PoissonDiskSampler sampler, ChunkRegion region, BlockPos regionPos,
                              int width, int height, int thicknessX, int thicknessZ, long seedModifier, Random random) {
        if (generated) {
            throw new IllegalStateException("Maze region: " + regionPos + " was generated twice");
        }

        // Generate structures with a strict spacing and separation


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

        // Cache previously generated structures
        boolean[] roomCache = new boolean[width * height];
        List<Vec2i> cachedPositions = new ArrayList<>(2);
        List<Room.Instance> cachedRooms = new ArrayList<>(2);
        if (connectionCache.containsKey(floor)) {
            Object2ObjectArrayMap<Vec2i, Room.Instance> cache = connectionCache.get(floor);
            cachedPositions.addAll(cache.keySet());
            cachedRooms.addAll(cache.values());
            cache.forEach((cell, room) -> {
                for (int row = 0; row < room.height(); row++) {
                    for (int column = 0; column < room.width(); column++) {
                        roomCache[(column + cell.y()) * width + row + cell.x()] = true;
                    }
                }
            });
        }

        // Run poisson disk sampler to find a position for each room
        List<Room.Instance> roomInstances = new ArrayList<>();
        List<Vec2i> roomPositions = sampler.generate(roomInstances, roomCache, biomeGrid, validRoomPos, random);
        roomPositions.addAll(cachedPositions);
        roomInstances.addAll(cachedRooms);
        ObjectLinkedOpenHashSet<Vector2D> nodes = new ObjectLinkedOpenHashSet<>();
        SetMultimap<Vec2i, Door.Instance> doors = LinkedHashMultimap.create();
        LevelOneMaze maze = new LevelOneMaze(width, height, roomCache, 0.2f, random);

        // Mark room origin cells for generation and add their doors' positions as nodes
        if (roomInstances.size() == roomPositions.size()) {
            for (int i = 0; i < roomInstances.size(); i++) {
                Room.Instance room = roomInstances.get(i);
                Vec2i roomPos = roomPositions.get(i);
                room.place(maze, roomPos.x(), roomPos.y(), floor)
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