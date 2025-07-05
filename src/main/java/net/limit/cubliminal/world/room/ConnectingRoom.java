package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.util.Vec3b;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.limit.cubliminal.world.maze.RoomCellState;
import net.limit.cubliminal.world.maze.SpecialMaze;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public record ConnectingRoom(List<Component> components, byte width, byte height, byte padding, List<Door>[] doors) implements Room {
    public static MapCodec<ConnectingRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Component.CODEC.listOf().fieldOf("components").forGetter(ConnectingRoom::components),
            Codec.BYTE.fieldOf("width").forGetter(ConnectingRoom::getWidth),
            Codec.BYTE.fieldOf("height").forGetter(ConnectingRoom::getHeight),
            Codec.BYTE.fieldOf("padding").forGetter(ConnectingRoom::padding),
            Codec.pair(Codec.intRange(0, Integer.MAX_VALUE).fieldOf("floor").codec(), Codec.STRING
                    .fieldOf("doors").codec()).listOf().fieldOf("door_data").forGetter(ConnectingRoom::pack)
    ).apply(instance, ConnectingRoom::new));

    public ConnectingRoom(List<Component> components, byte width, byte height, byte padding, List<Pair<Integer, String>> doorData) {
        this(components, width, height, padding, ConnectingRoom.unpack(doorData, width, height));
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Room width: " + width + " and height: " + height + " must be set above 0");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Door>[] unpack(List<Pair<Integer, String>> doorData, byte width, byte height) {
        List<List<Door>> listSquared = new ArrayList<>(doorData.size());
        for (Pair<Integer, String> floor : doorData) {
            listSquared.add(floor.getFirst(), Room.unpackDoors(floor.getSecond(), width, height));
        }
        return listSquared.toArray(List[]::new);
    }

    private List<Pair<Integer, String>> pack() {
        List<Pair<Integer, String>> list = new ArrayList<>(doors.length);
        for (int i = 0; i < doors.length; i++) {
            list.add(Pair.of(i, Room.packDoors(doors[i])));
        }
        return list;
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
    public List<Door> place(SpecialMaze maze, int x, int y, int floor, Vec2b roomDimensions, byte packedManipulation) {
        Manipulation manipulation = Manipulation.unpack(packedManipulation);
        BiFunction<Vec2b, Vec2b, Vec2b> transformation = Room.cornerTransformation(roomDimensions, manipulation);
        this.components.forEach(component -> {
            // Note that the real meaning of a component's 'y' is its floor. They are all scrambled, thus this check is needed
            if (component.pos().y() == floor) {
                Vec2b transformed = transformation.apply(component.pos().toVec2(), new Vec2b(component.height(), component.width()));
                maze.withState(x + transformed.x(), y + transformed.y(), RoomCellState.of(component::get, packedManipulation));
            }
        });
        PosTransformation translation = Room.posTransformation(roomDimensions, manipulation);
        RotTransformation rotation = Room.rotTransformation(manipulation);
        List<Door> floorDoors = doors[floor];
        List<Door> transformed = new ArrayList<>(floorDoors.size());
        floorDoors.forEach(door -> transformed.add(door.transform(translation, rotation)));
        return transformed;
    }

    public record Component(Vec3b pos, byte width, byte height, WeightedHolderSet<String> structures) {
        public static Codec<Component> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3b.CODEC.fieldOf("pos").forGetter(Component::pos),
                Codec.BYTE.fieldOf("width").forGetter(Component::width),
                Codec.BYTE.fieldOf("height").forGetter(Component::height),
                WeightedHolderSet.createHashCodec(Codec.STRING).fieldOf("structures").forGetter(Component::structures)
        ).apply(instance, Component::new));

        public Component(Vec3b pos, byte width, byte height, WeightedHolderSet<String> structures) {
            this.pos = pos;
            if (width < 1 || height < 1) {
                throw new IllegalArgumentException("Room width: " + width + " and height: " + height + " must be set above 0");
            }
            this.width = width;
            this.height = height;
            if (structures.getValues().isEmpty()) {
                throw new IllegalArgumentException("No structures were found in the holder set");
            }
            this.structures = structures;
        }

        public String get(Random random) {
            return this.structures.random(random);
        }

    }
}
