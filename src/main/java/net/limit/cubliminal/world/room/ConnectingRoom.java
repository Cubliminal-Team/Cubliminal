package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.world.maze.SpecialMaze;
import net.limit.cubliminal.world.room.CompositeRoom.Component;

import java.util.ArrayList;
import java.util.List;

public record ConnectingRoom(FloorData[] floorData, byte width, byte height, byte padding) implements Room {
    public static final MapCodec<ConnectingRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.pair(Codec.intRange(0, Integer.MAX_VALUE).fieldOf("floor").codec(),
                    Codec.pair(
                            Component.CODEC.listOf().optionalFieldOf("components", List.of()).codec(),
                            Codec.STRING.optionalFieldOf("doors", "").codec()
            )).listOf().fieldOf("floors").forGetter(ConnectingRoom::pack),
            Codec.BYTE.fieldOf("width").forGetter(ConnectingRoom::getWidth),
            Codec.BYTE.fieldOf("height").forGetter(ConnectingRoom::getHeight),
            Codec.BYTE.optionalFieldOf("padding", (byte) 0).forGetter(ConnectingRoom::padding)
    ).apply(instance, ConnectingRoom::new));

    public ConnectingRoom(List<Pair<Integer, Pair<List<Component>, String>>> packedData, byte width, byte height, byte padding) {
        this(ConnectingRoom.unpack(packedData, width, height), width, height, padding);
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Room width: " + width + " and height: " + height + " must be set above 0");
        }
    }

    private static FloorData[] unpack(List<Pair<Integer, Pair<List<Component>, String>>> packedData, byte width, byte height) {
        Int2ObjectOpenHashMap<FloorData> storage = new Int2ObjectOpenHashMap<>();
        packedData.forEach(pair -> storage.computeIfAbsent(pair.getFirst(), key -> {
            List<Door> doors = Room.unpackDoors(pair.getSecond().getSecond(), width, height);
            return new FloorData(List.copyOf(pair.getSecond().getFirst()), doors);
        }));
        int max = storage.keySet().intStream().max().orElse(-1);
        FloorData[] prepared = new FloorData[max + 1];
        storage.forEach((key, value) -> prepared[key] = value);
        return prepared;
    }

    private List<Pair<Integer, Pair<List<Component>, String>>> pack() {
        List<Pair<Integer, Pair<List<Component>, String>>> list = new ArrayList<>();
        for (int i = 0; i < this.getFloors(); i++) {
            FloorData data = floorData[i];
            if (data != null) {
                list.add(Pair.of(i, Pair.of(data.components(), Room.packDoors(data.doors()))));
            }
        }
        return list;
    }

    public int getFloors() {
        return this.floorData.length;
    }

    public boolean hasFloor(int floor) {
        return floorData[floor] != null;
    }

    public Instance getInstance(int floor, byte manipulation) {
        FloorData data = floorData[floor];
        return new Instance(new CompositeRoom(data.components(), width, height, data.doors()), width, height, manipulation);
    }

    @Override
    public byte getWidth() {
        return this.width;
    }

    @Override
    public byte getHeight() {
        return this.height;
    }

    @Override
    public RoomType<?> type() {
        return RoomType.CONNECTION;
    }

    @Override
    public List<Door> place(SpecialMaze maze, int x, int y, Vec2b roomDimensions, byte packedManipulation) {
        throw new IllegalCallerException("Connecting rooms are just holders and therefore should never be directly placed");
    }

    public record FloorData(List<Component> components, List<Door> doors) {
    }
}
