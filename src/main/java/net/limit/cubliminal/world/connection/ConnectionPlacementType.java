package net.limit.cubliminal.world.connection;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

public record ConnectionPlacementType<C extends ConnectionPlacement>(MapCodec<C> codec) {
    public static final Registry<ConnectionPlacementType<?>> REGISTRY = new SimpleRegistry<>(
            RegistryKey.ofRegistry(Cubliminal.id("connection_placement_type")), Lifecycle.stable());

    public static final ConnectionPlacementType<RandomSpreadConnectionPlacement> RANDOM_SPREAD = register("random_spread", new ConnectionPlacementType<>(RandomSpreadConnectionPlacement.CODEC));

    public static <C extends ConnectionPlacement> ConnectionPlacementType<C> register(String id, ConnectionPlacementType<C> roomType) {
        return Registry.register(REGISTRY, Cubliminal.id(id), roomType);
    }

    public static void init() {
    }
}
