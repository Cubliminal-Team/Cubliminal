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

public class SingleRoom implements Room {
    public static final MapCodec<SingleRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(SingleRoom::id),
            Codec.BYTE.fieldOf("width").forGetter(SingleRoom::width),
            Codec.BYTE.fieldOf("height").forGetter(SingleRoom::height),
            Codec.STRING.fieldOf("doors").forGetter(SingleRoom::doorData)
    ).apply(instance, SingleRoom::new));

    private final String id;
    private final byte width, height;
    private final List<Door> doors;

    public SingleRoom(String id, byte width, byte height, String doorData) {
        this.id = id;
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("SingleRoom width: " + width + " and height: " + height + " must be set above 0");
        }
        this.width = width;
        this.height = height;
        this.doors = this.unpackDoors(doorData);
    }

    public String id() {
        return this.id;
    }

    @Override
    public byte width() {
        return this.width;
    }

    @Override
    public byte height() {
        return this.height;
    }

    public String doorData() {
        return Room.packDoors(doors);
    }

    @Override
    public List<Door> doors() {
        return this.doors;
    }

    @Override
    public RoomType<?> type() {
        return RoomType.SINGLE_PIECE;
    }

    @Override
    public List<Door> place(SpecialMaze maze, int x, int y, Vec2b roomDimensions, byte packedManipulation, boolean generate) {
        if (generate) maze.withState(x, y, RoomCellState.of(random -> this.id, packedManipulation));
        Manipulation manipulation = Manipulation.unpack(packedManipulation);
        PosTransformation translation = Room.posTransformation(roomDimensions, manipulation);
        RotTransformation rotation = Room.rotTransformation(manipulation);
        List<Door> transformed = new ArrayList<>(this.doors.size());
        this.doors.forEach(door -> transformed.add(door.transform(translation, rotation)));
        return transformed;
    }

    @Override
    public String toString() {
        return "Id: " + this.id + "; width: " + this.width + "; height: " + this.height + "; doors: " + this.doors.toString();
    }
}
