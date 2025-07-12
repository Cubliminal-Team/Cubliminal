package net.limit.cubliminal.world.connection;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.world.room.ConnectingRoom;

public record Connection(ConnectingRoom room, ConnectionPlacement placement) {
    public static final MapCodec<Connection> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ConnectingRoom.CODEC.fieldOf("room").forGetter(Connection::room),
            ConnectionPlacement.TYPE_CODEC.fieldOf("placement").forGetter(Connection::placement)
    ).apply(instance, Connection::new));
}
