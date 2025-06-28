package net.limit.cubliminal.world.room;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

public record RoomType<R extends Room>(MapCodec<R> codec) {
    public static final Registry<RoomType<?>> REGISTRY = new SimpleRegistry<>(
            RegistryKey.ofRegistry(Cubliminal.id("room_type")), Lifecycle.stable());

    public static final RoomType<SingleRoom> SINGLE_PIECE = register("single_piece", new RoomType<>(SingleRoom.CODEC));
    public static final RoomType<CompositeRoom> COMPOUND_SET = register("compound_set", new RoomType<>(CompositeRoom.CODEC));
    public static final RoomType<ConnectingRoom> CONNECTION = register("connection", new RoomType<>(ConnectingRoom.CODEC));

    public static <R extends Room> RoomType<R> register(String id, RoomType<R> roomType) {
        return Registry.register(REGISTRY, Cubliminal.id(id), roomType);
    }

    public static void init() {
    }
}
