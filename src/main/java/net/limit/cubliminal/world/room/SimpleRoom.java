package net.limit.cubliminal.world.room;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.world.maze.RoomCellState;
import net.limit.cubliminal.world.maze.SpecialMaze;
import net.limit.cubliminal.util.Vec2b;
import net.ludocrypt.limlib.api.world.Manipulation;

import java.util.ArrayList;
import java.util.List;

public record SimpleRoom(String template, byte sizeX, byte sizeZ, List<Door> doors) implements Room {
    public static final MapCodec<SimpleRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("template").forGetter(SimpleRoom::template),
            Codec.BYTE.fieldOf("size_x").forGetter(SimpleRoom::getSizeX),
            Codec.BYTE.fieldOf("size_z").forGetter(SimpleRoom::getSizeZ),
            Codec.STRING.fieldOf("doors").forGetter(SimpleRoom::doorData)
    ).apply(instance, SimpleRoom::new));

    public SimpleRoom(String id, byte sizeX, byte sizeZ, String doorData) {
        this(id, sizeX, sizeZ, Room.unpackDoors(doorData, sizeX, sizeZ));
        if (sizeX < 1 || sizeZ < 1) {
            throw new IllegalArgumentException("SimpleRoom size X: " + sizeX + " and size Z: " + sizeZ + " must be set above 0");
        }
    }

    @Override
    public byte getSizeX() {
        return this.sizeX;
    }

    @Override
    public byte getSizeZ() {
        return this.sizeZ;
    }

    public String doorData() {
        return Room.packDoors(doors);
    }

    @Override
    public RoomType<?> type() {
        return RoomType.SIMPLE;
    }

    @Override
    public List<Door> place(SpecialMaze maze, int x, int z, Vec2b roomDimensions, byte packedManipulation) {
        maze.withState(x, z, RoomCellState.of(random -> this.template, packedManipulation));
        Manipulation manipulation = Manipulation.unpack(packedManipulation);
        PosTransformation translation = Room.posTransformation(roomDimensions, manipulation);
        RotTransformation rotation = Room.rotTransformation(manipulation);
        List<Door> transformed = new ArrayList<>(this.doors.size());
        this.doors.forEach(door -> transformed.add(door.transform(translation, rotation)));
        return transformed;
    }
}
