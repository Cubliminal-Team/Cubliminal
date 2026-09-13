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

public record CompositeRoom(List<Component> components, byte sizeX, byte sizeZ, List<Door> doors) implements Room {
    public static final MapCodec<CompositeRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Component.CODEC.listOf().fieldOf("components").forGetter(CompositeRoom::components),
            Codec.BYTE.fieldOf("size_x").forGetter(CompositeRoom::getSizeX),
            Codec.BYTE.fieldOf("size_z").forGetter(CompositeRoom::getSizeZ),
            Codec.STRING.fieldOf("doors").forGetter(CompositeRoom::doorData)
    ).apply(instance, CompositeRoom::new));

    public CompositeRoom(List<Component> components, byte sizeX, byte sizeZ, String doorData) {
        this(List.copyOf(components), sizeX, sizeZ, Room.unpackDoors(doorData, sizeX, sizeZ));
        if (sizeX < 1 || sizeZ < 1) {
            throw new IllegalArgumentException("Room size X: " + sizeX + " and size Z: " + sizeZ + " must be set above 0");
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
        return RoomType.COMPOSITE;
    }

    @Override
    public List<Door> place(SpecialMaze maze, int x, int z, Vec2b roomDimensions, byte packedManipulation) {
        Manipulation manipulation = Manipulation.unpack(packedManipulation);
        BiFunction<Vec2b, Vec2b, Vec2b> transformation = Room.cornerTransformation(roomDimensions, manipulation);
        this.components.forEach(component -> {
            Vec2b transformed = transformation.apply(component.pos(), new Vec2b(component.sizeX(), component.sizeZ()));
            maze.withState(x + transformed.x(), z + transformed.y(), RoomCellState.of(component::get, packedManipulation));
        });
        PosTransformation translation = Room.posTransformation(roomDimensions, manipulation);
        RotTransformation rotation = Room.rotTransformation(manipulation);
        List<Door> transformed = new ArrayList<>(this.doors.size());
        this.doors.forEach(door -> transformed.add(door.transform(translation, rotation)));
        return transformed;
    }

    public record Component(Vec2b pos, byte sizeX, byte sizeZ, WeightedHolderSet<String> templates) {
        public static Codec<Component> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec2b.CODEC.fieldOf("pos").forGetter(Component::pos),
                Codec.BYTE.fieldOf("size_x").forGetter(Component::sizeX),
                Codec.BYTE.fieldOf("size_z").forGetter(Component::sizeZ),
                WeightedHolderSet.createHashCodec(Codec.STRING).fieldOf("templates").forGetter(Component::templates)
        ).apply(instance, Component::new));

        public Component(Vec2b pos, byte sizeX, byte sizeZ, WeightedHolderSet<String> templates) {
            if (pos.x() < 0 || pos.y() < 0) {
                throw new IllegalArgumentException("Room's relative position X: " + pos.x() + " and Z: " + pos.y() + " must be 0 or positive");
            }
            this.pos = pos;
            if (sizeX < 1 || sizeZ < 1) {
                throw new IllegalArgumentException("Room size X: " + sizeX + " and size Z: " + sizeZ + " must be set above 0");
            }
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
            if (templates.getValues().isEmpty()) {
                throw new IllegalArgumentException("No templates were found in the holder set");
            }
            this.templates = templates;
        }

        public String get(Random random) {
            return this.templates.random(random);
        }

    }
}