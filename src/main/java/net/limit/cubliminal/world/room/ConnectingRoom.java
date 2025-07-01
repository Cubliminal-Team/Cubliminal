package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class ConnectingRoom extends CompositeRoom implements Room {
    public static MapCodec<ConnectingRoom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Component.CODEC.listOf().fieldOf("components").forGetter(CompositeRoom::components),
            Codec.BYTE.fieldOf("width").forGetter(CompositeRoom::width),
            Codec.BYTE.fieldOf("height").forGetter(CompositeRoom::height),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spacing").forGetter(ConnectingRoom::getSpacing),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("separation").forGetter(ConnectingRoom::getSeparation),
            Codec.STRING.fieldOf("doors").forGetter(CompositeRoom::doorData),
            Codec.pair(Codec.STRING.fieldOf("key").codec(), Codec.STRING.fieldOf("door").codec())
                    .fieldOf("extra").forGetter(ConnectingRoom::getExtraAsPair)
    ).apply(instance, ConnectingRoom::new));

    private final int spacing;
    private final int separation;
    private final String extraKey;
    private final Door extraDoor;

    public ConnectingRoom(List<Component> components, byte width, byte height, int spacing,
                          int separation, String doorData, Pair<String, String> extra) {
        super(components, width, height, doorData);
        this.spacing = spacing;
        this.separation = separation;
        this.extraKey = extra.getFirst();
        this.extraDoor = this.unpackDoors(extra.getSecond()).getFirst();
        this.doors().remove(extraDoor);
    }

    public int getSpacing() {
        return spacing;
    }

    public int getSeparation() {
        return separation;
    }

    public Pair<String, String> getExtraAsPair() {
        return Pair.of(extraKey, Room.packDoors(List.of(extraDoor)));
    }

    @Override
    public RoomType<?> type() {
        return RoomType.CONNECTION;
    }
}
