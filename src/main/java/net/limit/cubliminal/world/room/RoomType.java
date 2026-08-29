package net.limit.cubliminal.world.room;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.InvalidIdentifierException;

public record RoomType<R extends Room>(MapCodec<R> codec) {
    public static final Registry<RoomType<?>> REGISTRY = new SimpleRegistry<>(
            RegistryKey.ofRegistry(Cubliminal.id("room_type")), Lifecycle.stable());

    public static final RoomType<SimpleRoom> SIMPLE = register("simple", new RoomType<>(SimpleRoom.CODEC));
    public static final RoomType<CompositeRoom> COMPOSITE = register("composite", new RoomType<>(CompositeRoom.CODEC));
    public static final RoomType<ConnectingRoom> CONNECTION = register("connection", new RoomType<>(ConnectingRoom.CODEC));

    public static <R extends Room> RoomType<R> register(String id, RoomType<R> roomType) {
        return Registry.register(REGISTRY, Cubliminal.id(id), roomType);
    }

    public static void init() {
    }

    public enum Type {
        SIMPLE("Simple"),
        COMPOSITE("Composite"),
        CONNECTING("Connecting");

        private final String asString;
        Type(String asString) {
            this.asString = asString;
        }

        @Override
        public String toString() {
            return this.asString;
        }

        public static Type fromString(String name) {
            for (Type type : Type.values()) {
                if (type.asString.equals(name)) {
                    return type;
                }
            }

            throw new InvalidIdentifierException("Room type: " + name + " doesn't exist");
        }
    }
}
