package net.limit.cubliminal.world.connection;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.limit.cubliminal.world.room.Room;

public record Connection(Room room, ConnectionPlacement placement) {
    public static final Codec<Pair<String, ConnectionPlacement>> CODEC = Codec.pair(
            Codec.STRING.fieldOf("room").codec(),
            ConnectionPlacement.TYPE_CODEC.fieldOf("placement").codec()
    );
}
