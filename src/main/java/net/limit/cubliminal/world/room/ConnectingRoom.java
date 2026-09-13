package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.world.maze.SpecialMaze;
import net.limit.cubliminal.world.room.CompositeRoom.Component;
import net.minecraft.util.StringIdentifiable;

import java.util.ArrayList;
import java.util.List;

public record ConnectingRoom(FloorData[] floorData, byte sizeX, byte sizeZ, byte padding, PaddingType paddingType) implements Room {
    public static final MapCodec<ConnectingRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.pair(Codec.intRange(0, Integer.MAX_VALUE).fieldOf("floor").codec(), Codec.pair(
                    Component.CODEC.listOf().optionalFieldOf("components", List.of()).codec(),
                    Codec.STRING.optionalFieldOf("doors", "").codec()
            )).listOf().fieldOf("floors").forGetter(ConnectingRoom::pack),
            Codec.BYTE.fieldOf("size_x").forGetter(ConnectingRoom::getSizeX),
            Codec.BYTE.fieldOf("size_z").forGetter(ConnectingRoom::getSizeZ),
            Codec.BYTE.optionalFieldOf("padding", (byte) 0).forGetter(ConnectingRoom::padding),
            StringIdentifiable.BasicCodec.STRING.optionalFieldOf("padding_type", PaddingType.BELOW.toString()).xmap(PaddingType::fromString, PaddingType::toString).forGetter(ConnectingRoom::paddingType)
    ).apply(instance, ConnectingRoom::new));

    public ConnectingRoom(List<Pair<Integer, Pair<List<Component>, String>>> packedData, byte sizeX, byte sizeZ, byte padding, PaddingType paddingType) {
        this(ConnectingRoom.unpack(packedData, sizeX, sizeZ), sizeX, sizeZ, padding, paddingType);
        if (sizeX < 1 || sizeZ < 1) {
            throw new IllegalArgumentException("Room size X: " + sizeX + " and size Z: " + sizeZ + " must be set above 0");
        }
    }

    private static FloorData[] unpack(List<Pair<Integer, Pair<List<Component>, String>>> packedData, byte sizeX, byte sizeZ) {
        Int2ObjectOpenHashMap<FloorData> storage = new Int2ObjectOpenHashMap<>();
        packedData.forEach(pair -> storage.computeIfAbsent(pair.getFirst(), key -> {
            List<Door> doors = Room.unpackDoors(pair.getSecond().getSecond(), sizeX, sizeZ);
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
        return new Instance(new CompositeRoom(data.components(), sizeX, sizeZ, data.doors()), sizeX, sizeZ, manipulation);
    }

    @Override
    public byte getSizeX() {
        return this.sizeX;
    }

    @Override
    public byte getSizeZ() {
        return this.sizeZ;
    }

    @Override
    public RoomType<?> type() {
        return RoomType.CONNECTING;
    }

    @Override
    public List<Door> place(SpecialMaze maze, int x, int z, Vec2b roomDimensions, byte packedManipulation) {
        throw new UnsupportedOperationException("Connecting rooms are just holders and therefore should never be directly placed");
    }

    public record FloorData(List<Component> components, List<Door> doors) {
    }
}
