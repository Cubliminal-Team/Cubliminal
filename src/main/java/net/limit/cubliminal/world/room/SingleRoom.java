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

public record SingleRoom(String id, byte width, byte height, List<Door> doors) implements Room {
    public static final MapCodec<SingleRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(SingleRoom::id),
            Codec.BYTE.fieldOf("width").forGetter(SingleRoom::getWidth),
            Codec.BYTE.fieldOf("height").forGetter(SingleRoom::getHeight),
            Codec.STRING.fieldOf("doors").forGetter(SingleRoom::doorData)
    ).apply(instance, SingleRoom::new));

    public SingleRoom(String id, byte width, byte height, String doorData) {
        this(id, width, height, Room.unpackDoors(doorData, width, height));
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("SingleRoom width: " + width + " and height: " + height + " must be set above 0");
        }
    }

    @Override
    public byte getWidth() {
        return this.width;
    }

    @Override
    public byte getHeight() {
        return this.height;
    }

    public String doorData() {
        return Room.packDoors(doors);
    }

    @Override
    public RoomType<?> type() {
        return RoomType.SINGLE_PIECE;
    }

    @Override
    public List<Door> place(SpecialMaze maze, int x, int y, Vec2b roomDimensions, byte packedManipulation) {
        maze.withState(x, y, RoomCellState.of(random -> this.id, packedManipulation));
        Manipulation manipulation = Manipulation.unpack(packedManipulation);
        PosTransformation translation = Room.posTransformation(roomDimensions, manipulation);
        RotTransformation rotation = Room.rotTransformation(manipulation);
        List<Door> transformed = new ArrayList<>(this.doors.size());
        this.doors.forEach(door -> transformed.add(door.transform(translation, rotation)));
        return transformed;
    }
}
