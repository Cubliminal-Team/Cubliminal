package net.limit.cubliminal.world.room;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import net.limit.cubliminal.util.MazeUtil;
import net.limit.cubliminal.world.maze.SpecialMaze;
import net.limit.cubliminal.util.Vec2b;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.*;
import java.util.function.BiFunction;

/**
 * The basic interface common to room-like objects.
 */
public interface Room {

    Codec<Room> CODEC = RoomType.REGISTRY.getCodec().dispatch("type", Room::type, RoomType::codec);

    Room DEFAULT = new SingleRoom("default", (byte) 1, (byte) 1, "");

    byte getWidth();

    byte getHeight();

    RoomType<?> type();

    static String packDoors(List<Door> doors) {
        if (doors == null || doors.isEmpty()) return "";
        Byte2ObjectArrayMap<List<Door>> sortedWalls = new Byte2ObjectArrayMap<>();
        doors.forEach(door -> sortedWalls.computeIfAbsent(door.facing(), f -> new ArrayList<>()).add(door));
        StringBuilder doorData = new StringBuilder();
        for (int dir = 0; dir < 4; dir++) {
            List<Door> wall = sortedWalls.getOrDefault((byte) dir, Collections.emptyList());
            if (!wall.isEmpty()) {
                boolean yAxis = dir == 0 || dir == 1;
                List<Integer> orderedWall = wall.stream()
                        .map(yAxis ? door -> (int) door.relativePos().y() : door -> (int) door.relativePos().x())
                        .sorted().toList();
                int maxPos = orderedWall.getLast();
                StringBuilder wallData = new StringBuilder(String.valueOf("ewns".charAt(dir)));
                for (int i = 0; i <= maxPos; i++) wallData.append("_").append(orderedWall.contains(i) ? "1" : "0");
                if (!doorData.isEmpty()) doorData.append(":");
                doorData.append(wallData);
            }
        }
        return doorData.toString();
    }

    /**
     * Format: {@code e_bl_bl_bl:w_bl_bl_bl:s_bl_bl:n_bl_bl}
     * <p>Rightmost wall data can be skipped if considered irrelevant but any offset from relative 0, 0 should be specified.
     * It is read from left to right, and leftmost data represent doors closest to the west-north relative coordinate.
     * Redundant data will be ignored e.g. non-existent walls.
     **/
    static List<Door> unpackDoors(String doorData, byte width, byte height) {
        String[] rawData = doorData.split(":");
        List<Door> doors = new ArrayList<>();
        for (String wall : rawData) {
            if (!wall.isEmpty()) {
                char direction = wall.charAt(0);
                if ("ewns".indexOf(direction) != -1) {
                    String[] wallData = wall.split("_");
                    byte dir = MazeUtil.ordinal(MazeUtil.getDirection(direction));
                    for (int n = 1; n < wallData.length; n++) {
                        if (wallData[n].equals("1") && isDoorValid(dir, n, width, height)) {
                            Door door = switch (dir) {
                                case 0 -> new Door(new Vec2b((byte) (height - 1), (byte) (n - 1)), dir);
                                case 1 -> new Door(new Vec2b((byte) 0, (byte) (n - 1)), dir);
                                case 2 -> new Door(new Vec2b((byte) (n - 1), (byte) 0), dir);
                                default -> new Door(new Vec2b((byte) (n - 1), (byte) (width - 1)), dir);
                            };
                            doors.add(door);
                        }
                    }
                }
            }
        }
        return List.copyOf(doors);
    }

    static boolean isDoorValid(byte dir, int index, byte width, byte height) {
        return switch (dir) {
            case 0, 1 -> index <= width;
            default -> index <= height;
        };
    }

    List<Door> place(SpecialMaze maze, int x, int y, Vec2b roomDimensions, byte packedManipulation);

    static PosTransformation posTransformation(Vec2b roomDimensions, Manipulation manipulation) {
        PosTransformation rotation = switch (manipulation.getRotation()) {
            case CLOCKWISE_90 -> pos -> new Vec2b((byte) (roomDimensions.x() - 1 - pos.y()), pos.x());
            case COUNTERCLOCKWISE_90 -> pos -> new Vec2b(pos.y(), (byte) (roomDimensions.y() - 1 - pos.x()));
            case CLOCKWISE_180 -> pos -> new Vec2b((byte) (roomDimensions.x() - 1 - pos.x()), (byte) (roomDimensions.y() - 1 - pos.y()));
            case NONE -> pos -> pos;
        };

        PosTransformation mirror = switch (manipulation.getMirror()) {
            case LEFT_RIGHT -> pos -> new Vec2b(pos.x(), (byte) (roomDimensions.y() - 1 - pos.y()));
            case FRONT_BACK -> pos -> new Vec2b((byte) (roomDimensions.x() - 1 - pos.x()), pos.y());
            case NONE -> pos -> pos;
        };

        return pos -> mirror.translate(rotation.translate(pos));
    }

    static RotTransformation rotTransformation(Manipulation manipulation) {
        RotTransformation rotation = switch (manipulation.getRotation()) {
            case CLOCKWISE_90 -> Direction::rotateYClockwise;
            case COUNTERCLOCKWISE_90 -> Direction::rotateYCounterclockwise;
            case CLOCKWISE_180 -> Direction::getOpposite;
            case NONE -> dir -> dir;
        };

        RotTransformation mirror = dir -> manipulation
                .getMirror()
                .getRotation(dir) == BlockRotation.CLOCKWISE_180 ? dir.getOpposite() : dir;

        return dir -> mirror.rotate(rotation.rotate(dir));
    }

    static BiFunction<Vec2b, Vec2b, Vec2b> cornerTransformation(Vec2b roomDimensions, Manipulation manipulation) {
        boolean swap = false;
        BiFunction<Vec2b, Vec2b, Vec2b> rotation = switch (manipulation.getRotation()) {
            case CLOCKWISE_90 -> {
                swap = true;
                yield (pos, hw) -> new Vec2b((byte) (roomDimensions.x() - pos.y() - hw.y()), pos.x());
            }
            case COUNTERCLOCKWISE_90 -> {
                swap = true;
                yield (pos, hw) -> new Vec2b(pos.y(), (byte) (roomDimensions.y() - pos.x() - hw.x()));
            }
            case CLOCKWISE_180 -> (pos, hw) -> new Vec2b((byte) (roomDimensions.x() - pos.x() - hw.x()), (byte) (roomDimensions.y() - pos.y() - hw.y()));
            case NONE -> (pos, hw) -> pos;
        };

        BiFunction<Vec2b, Vec2b, Vec2b> mirror = switch (manipulation.getMirror()) {
            case LEFT_RIGHT -> (pos, hw) -> new Vec2b(pos.x(), (byte) (roomDimensions.y() - pos.y() - hw.y()));
            case FRONT_BACK -> (pos, hw) -> new Vec2b((byte) (roomDimensions.x() - pos.x() - hw.x()), pos.y());
            case NONE -> (pos, hw) -> pos;
        };

        boolean finalSwap = swap;
        return (pos, hw) -> {
            Vec2b rotatedPos = rotation.apply(pos, hw);
            Vec2b updated = finalSwap ? hw.swap() : hw;
            return mirror.apply(rotatedPos, updated);
        };
    }

    default Instance newInstance(Random random) {
        return new Instance(this, this.getWidth(), this.getHeight(), (byte) random.nextInt(8));
    }

    default Instance newInstance(Manipulation manipulation) {
        return new Instance(this, this.getWidth(), this.getHeight(), manipulation.pack());
    }

    record Instance(Room parent, byte width, byte height, byte packedManipulation) {

        public Instance(Room parent, byte width, byte height, byte packedManipulation) {
            this.parent = parent;
            this.width = switch (Manipulation.unpack(packedManipulation).getRotation()) {
                case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> {
                    this.height = width;
                    yield height;
                }
                default -> {
                    this.height = height;
                    yield width;
                }
            };
            this.packedManipulation = packedManipulation;
        }

        // We invert width and height from the usual order because manipulations are done with a vector like this
        public List<Door> place(SpecialMaze maze, int x, int y) {
            return this.parent.place(maze, x, y, new Vec2b(height, width), packedManipulation);
        }

        public boolean shouldGenerate(int x, int z, int mazeWidth, int mazeHeight) {
            return x > 0 && x + height < mazeWidth && z > 0 && z + width < mazeHeight;
        }
    }
}