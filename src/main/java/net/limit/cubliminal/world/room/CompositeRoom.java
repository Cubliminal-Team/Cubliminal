package net.limit.cubliminal.world.room;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.limit.cubliminal.world.maze.RoomCellState;
import net.limit.cubliminal.world.maze.SpecialMaze;
import net.limit.cubliminal.util.Vec2b;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public record CompositeRoom(List<Component> components, byte width, byte height, List<Door> doors) implements Room {
    public static final MapCodec<CompositeRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Component.CODEC.listOf().fieldOf("components").forGetter(CompositeRoom::components),
            Codec.BYTE.fieldOf("width").forGetter(CompositeRoom::getWidth),
            Codec.BYTE.fieldOf("height").forGetter(CompositeRoom::getHeight),
            Codec.STRING.fieldOf("doors").forGetter(CompositeRoom::doorData)
    ).apply(instance, CompositeRoom::new));

    public CompositeRoom(List<Component> components, byte width, byte height, String doorData) {
        this(List.copyOf(components), width, height, Room.unpackDoors(doorData, width, height));
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Room width: " + width + " and height: " + height + " must be set above 0");
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
        return RoomType.COMPOUND_SET;
    }

    @Override
    public List<Door> place(SpecialMaze maze, int x, int y, Vec2b roomDimensions, byte packedManipulation) {
        Manipulation manipulation = Manipulation.unpack(packedManipulation);
        BiFunction<Vec2b, Vec2b, Vec2b> transformation = Room.cornerTransformation(roomDimensions, manipulation);
        this.components.forEach(component -> {
            Vec2b transformed = transformation.apply(component.pos(), new Vec2b(component.height(), component.width()));
            maze.withState(x + transformed.x(), y + transformed.y(), RoomCellState.of(component::get, packedManipulation));
        });
        PosTransformation translation = Room.posTransformation(roomDimensions, manipulation);
        RotTransformation rotation = Room.rotTransformation(manipulation);
        List<Door> transformed = new ArrayList<>(this.doors.size());
        this.doors.forEach(door -> transformed.add(door.transform(translation, rotation)));
        return transformed;
    }

    public record Component(Vec2b pos, byte width, byte height, WeightedHolderSet<String> structures) {
        public static Codec<Component> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec2b.CODEC.fieldOf("pos").forGetter(Component::pos),
                Codec.BYTE.fieldOf("width").forGetter(Component::width),
                Codec.BYTE.fieldOf("height").forGetter(Component::height),
                WeightedHolderSet.createHashCodec(Codec.STRING).fieldOf("structures").forGetter(Component::structures)
        ).apply(instance, Component::new));

        public Component(Vec2b pos, byte width, byte height, WeightedHolderSet<String> structures) {
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