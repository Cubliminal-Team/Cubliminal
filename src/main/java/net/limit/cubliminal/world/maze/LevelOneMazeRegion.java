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
import net.limit.cubliminal.world.connection.ConnectionRegistry;
import net.limit.cubliminal.world.placement.MSTree;
import net.limit.cubliminal.world.placement.PoissonDiskSampler;
import net.limit.cubliminal.world.room.ConnectingRoom;
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

    private transient RegistryEntry<Biome>[] biomeGrid;
    private transient List<Vec2i> validRoomPos;
    private transient Int2ObjectOpenHashMap<Object2ObjectArrayMap<Vec2i, Room.Instance>> connectionCache;

    public LevelOneMazeRegion(int layerHeight, int floorCount) {
        super(layerHeight, new LevelOneMaze[floorCount]);
    }

    @SuppressWarnings("unchecked")
    public void generateMazes(LevelOneBiomeSource biomeSource, PoissonDiskSampler sampler, ChunkRegion region,
                              BlockPos regionPos, int width, int floors, int height, int thicknessX,
                              int thicknessZ, long seedModifier, Random random) {

        // Generate connections and cache biomes + valid room positions
        this.biomeGrid = new RegistryEntry[width * height];
        this.validRoomPos = new ArrayList<>(width * height);
        this.connectionCache = new Int2ObjectOpenHashMap<>(floors);

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                BlockPos inWorldPos = new BlockPos(x * thicknessX + regionPos.getX(), regionPos.getY(), z * thicknessZ + regionPos.getZ());
                RegistryEntry<Biome> biome = biomeSource.calcBiome(inWorldPos);

                // Cache biome
                biomeGrid[z * width + x] = biome;
                if (biome.isIn(CubliminalBiomes.DEEP_LEVEL_ONE)) {
                    validRoomPos.add(new Vec2i(x, z));
                }

                // Generate if it's not touching a border
                if (x > 0 && x < width - 1 && z > 0 && z < height - 1) {
                    int finalX = x;
                    int finalZ = z;
                    biome.getKey().ifPresent(key -> {
                        if (ConnectionRegistry.containsBiome(key)) {
                            ConnectionRegistry.getMappings(key).forEach(connection -> {
                                // Minecraft-like structure placement
                                if (connection.placement().shouldGenerate(Cubliminal.SERVER.getSeed(),
                                        Math.floorDiv(inWorldPos.getX(), thicknessX),
                                        Math.floorDiv(inWorldPos.getZ(), thicknessZ))) {
                                    this.placeConnection(finalX, finalZ, width, height, connection.room(), floors, random);
                                }
                            });
                        }
                    });
                }
            }
        }

        // Generate the actual mazes
        for (int floor = 0; floor < floors; floor++) {
            BlockPos mazePos = regionPos.add(0, floor * layerHeight, 0);
            Random mazeRandom = Random.create(LimlibHelper.blockSeed(mazePos) + seedModifier + region.getSeed());
            floorStorage[floor] = floor % 2 == 0
                    ? this.generateHalls(sampler, floor, mazePos, width, height, thicknessX, thicknessZ, mazeRandom)
                    : this.generateCorridors(sampler, floor, mazePos, width, height, thicknessX, thicknessZ, mazeRandom);
        }

        // Nullify unused variables
        biomeGrid = null;
        validRoomPos = null;
        connectionCache = null;
    }

    // Chooses a random floor where the room can be placed
    private void placeConnection(int x, int z, int width, int height, ConnectingRoom room, int floors, Random random) {
        int validFloors = 1 + floors - room.getFloors() - room.padding();
        if (validFloors > 0) {
            int startFloor = room.padding() + random.nextInt(validFloors);
            byte manipulation = (byte) random.nextInt(8);
            for (int dy = 0; dy < room.getFloors(); dy++) {
                if (room.hasFloor(dy)) {
                    Room.Instance instance = room.getInstance(dy, manipulation);
                    if (instance.shouldGenerate(x, z, width, height)) {
                        connectionCache
                                .computeIfAbsent(startFloor + dy, k -> new Object2ObjectArrayMap<>(2))
                                .putIfAbsent(new Vec2i(x, z), instance);
                    }
                }
            }
        }
    }

    private LevelOneMaze generateCorridors(PoissonDiskSampler sampler, int floor, BlockPos mazePos, int width,
                                           int height, int thicknessX, int thicknessZ, Random random) {

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
        List<Vec2i> roomPositions = sampler.generate(roomInstances, roomCache, "habitable_zone_corridors", random);
        roomPositions.addAll(cachedPositions);
        roomInstances.addAll(cachedRooms);
        SetMultimap<Vec2i, Door.Instance> doors = LinkedHashMultimap.create();
        LevelOneMaze maze = new LevelOneMaze(width, height, roomCache, 0.2f, true, random);

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
                            doors.put(vec, instance);
                        });
            }
        }

        // Add connections to other mazes in form of doors
        List<Vec2i> connections = this.addConnections(mazePos, width, height, thicknessX, thicknessZ, 4);
        for (int i = 0; i < connections.size(); ++i) {
            Vec2i entryPos = connections.get(i);
            if (maze.fits(entryPos)) {
                MazeComponent.Face face = MazeComponent.Face.values()[i];
                doors.put(entryPos, new Door.Instance(entryPos, face.mirror()));
            }
        }

        maze.setDoors(doors);
        maze.generateMaze();

        return maze;
    }

    private LevelOneMaze generateHalls(PoissonDiskSampler sampler, int floor, BlockPos mazePos, int width,
                                       int height, int thicknessX, int thicknessZ, Random random) {

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
        Collections.shuffle(validRoomPos, new java.util.Random(LimlibHelper.blockSeed(mazePos)));
        List<Room.Instance> roomInstances = new ArrayList<>();
        List<Vec2i> roomPositions = sampler.generate(roomInstances, roomCache, biomeGrid, validRoomPos, random);
        roomPositions.addAll(cachedPositions);
        roomInstances.addAll(cachedRooms);
        ObjectLinkedOpenHashSet<Vector2D> nodes = new ObjectLinkedOpenHashSet<>();
        SetMultimap<Vec2i, Door.Instance> doors = LinkedHashMultimap.create();
        LevelOneMaze maze = new LevelOneMaze(width, height, roomCache, 0.2f, false, random);

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

    private List<Vec2i> addConnections(BlockPos mazePos, int width, int height, int thicknessX, int thicknessZ, int a) {
        Random randomUp = Random.create(LimlibHelper.blockSeed(mazePos.add(height * thicknessZ - 1, 0, 0)));
        Random randomDown = Random.create(LimlibHelper.blockSeed(mazePos.add(-1, 0, 0)));
        Random randomLeft = Random.create(LimlibHelper.blockSeed(mazePos));
        Random randomRight = Random.create(LimlibHelper.blockSeed(mazePos.add(0, 0, width * thicknessX)));
        List<Vec2i> connections = new ArrayList<>();
        // East
        connections.add(new Vec2i(width - 1, randomUp.nextBetween(-a, height + a - 1)));
        // West
        connections.add(new Vec2i(0, randomDown.nextBetween(-a, height + a - 1)));
        // North
        connections.add(new Vec2i(randomLeft.nextBetween(-a, width + a - 1), 0));
        // South
        connections.add(new Vec2i(randomRight.nextBetween(-a, width + a - 1), height - 1));
        return connections;
    }
}