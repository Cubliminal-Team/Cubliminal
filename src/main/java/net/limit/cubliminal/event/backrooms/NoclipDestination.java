package net.limit.cubliminal.event.backrooms;

import com.mojang.datafixers.util.Pair;
import net.limit.cubliminal.block.custom.VentilationPipeBlock;
import net.limit.cubliminal.init.CubliminalBiomes;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.limit.cubliminal.init.CubliminalRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A record used to define how no-clip is performed across backrooms levels. They are stored in a {@code Map}
 * where you input the {@code RegistryKey} of the world where the player comes from and get a {@code NoclipDestination},
 * and provided by {@link #from(RegistryKey)}. You can also get the {@code NoclipDestination} that corresponds to a
 * specific destination world via {@link #fromDestination(RegistryKey)}.
 * <p>
 *     Most no-clip destination positions will be calculated using the following steps:
 *     <li>Search for a biome where players can no-clip to according to the {@code CAN_NOCLIP_TO} biome tag.</li>
 *     <li>If the last step is successful, search for a valid spawn point position in a 32 x world height x 32 block range
 *     from the previously found position. Otherwise, return a somewhat random position within world bounds.</li>
 *     <li>If the last step is successful, search for a valid no-clip position, usually a block below the ceiling of a
 *     structure.</li>
 *     <li>If any of the two previous steps fail, return the found biome's position. Otherwise, with all steps completed properly,
 *     a {@code Pair} of two positions will be returned.</li>
 * </p>
 *
 * @param destination The {@code RegistryKey} of the destination world that this {@code NoclipDestination} sends players to.
 * @param pos A function that takes a {@code ServerPlayerEntity} and returns a {@code Pair} of a first and a second element,
 *            respectively a valid spawn point position and the exact position where the player no-clips.
 */

public record NoclipDestination(RegistryKey<World> destination, Function<ServerPlayerEntity, Pair<BlockPos, Vec3d>> pos) {
    private static final Map<RegistryKey<World>, NoclipDestination> DESTINATIONS = new HashMap<>();

    private static final NoclipDestination DEFAULT = new NoclipDestination(CubliminalRegistrar.THE_LOBBY_KEY, player -> {
        ServerWorld world = player.getServer().getWorld(CubliminalRegistrar.THE_LOBBY_KEY);
        Pair<BlockPos, RegistryEntry<Biome>> pair = world.locateBiome(registryEntry -> registryEntry
                .isIn(CubliminalBiomes.CAN_NOCLIP_TO), new BlockPos(player.getBlockX(),
                world.getBottomY() + 2, player.getBlockZ()), 640, 16, 32);

        BlockPos finalPos = new BlockPos(MathHelper.floor(player.getX() / 8) + 4, 2, MathHelper.floor(player.getZ() / 8) + 4);
        if (pair != null) {
            Optional<BlockPos> spawnPos = BlockPos.findClosest(new BlockPos(pair.getFirst().getX(), world.getBottomY(),
                    pair.getFirst().getZ()), 32, world.getTopYInclusive() - 1, p -> {
                WorldChunk chunk = getChunk(world, p);
                if (world.isInHeightLimit(p.down().getY()) && chunk.getBlockState(p.down()).isIn(CubliminalBlocks.FLOOR_PALETTE)) {
                    return !chunk.getBlockState(p).isOpaqueFullCube() && chunk.getBlockState(p.up()).isAir();
                }
                return false;
            });

            if (spawnPos.isPresent()) {
                Optional<BlockPos> validPos = BlockPos.findClosest(spawnPos.get(), 32, world.getTopYInclusive() - 1, p -> {
                    WorldChunk chunk = getChunk(world, p);
                    if (world.isInHeightLimit(p.down().getY()) && chunk.getBlockState(p.up()).isOpaqueFullCube()) {
                        return !chunk.getBlockState(p).isOpaqueFullCube() && chunk.getBlockState(p.down()).isAir();
                    }
                    return false;
                });

                if (validPos.isPresent()) {
                    return Pair.of(spawnPos.get(), validPos.get().toCenterPos());
                }
            }

            finalPos = new BlockPos((pair.getFirst().getX() / 8) + 4, 2, (pair.getFirst().getZ() / 8) + 4);
        }

        return Pair.of(finalPos, finalPos.toCenterPos().add(0, 2.5, 0));
    });

    public static Pair<RegistryKey<World>, NoclipDestination> from(RegistryKey<World> world) {
        NoclipDestination des = DESTINATIONS.getOrDefault(world, DEFAULT);
        return Pair.of(des.destination(), des);
    }

    public static Pair<RegistryKey<World>, NoclipDestination> fromDestination(RegistryKey<World> destination) {
        return Pair.of(destination, DESTINATIONS.values().stream()
                .filter(d -> d.destination().equals(destination)).findFirst().orElse(DEFAULT));
    }

    public Pair<BlockPos, Vec3d> locate(ServerPlayerEntity player) {
        return this.pos().apply(player);
    }

    public static NoclipDestination create(RegistryKey<World> origin, RegistryKey<World> destination, Function<ServerPlayerEntity, Pair<BlockPos, Vec3d>> pos) {
        NoclipDestination noclipDestination = new NoclipDestination(destination, pos);
        DESTINATIONS.put(origin, noclipDestination);
        return noclipDestination;
    }

    public static BlockPos centerPos(BlockPos pos, int y) {
        return new BlockPos(MathHelper.floor(pos.getX() / 16.0) + 8, y, MathHelper.floor(pos.getZ() / 16.0) + 8);
    }

    public static WorldChunk getChunk(World world, BlockPos pos) {
        return world.getChunk(MathHelper.floor(pos.getX() / 16.0), MathHelper.floor(pos.getZ() / 16.0));
    }

    public static void init() {
        create(CubliminalRegistrar.THE_LOBBY_KEY, CubliminalRegistrar.HABITABLE_ZONE_KEY, player -> {
            ServerWorld world = player.getServer().getWorld(CubliminalRegistrar.HABITABLE_ZONE_KEY);
            Pair<BlockPos, RegistryEntry<Biome>> pair = world.locateBiome(registryEntry -> registryEntry
                    .isIn(CubliminalBiomes.CAN_NOCLIP_TO), new BlockPos(player.getBlockX(),
                    world.getBottomY() + 2, player.getBlockZ()), 1600, 16, 16);

            BlockPos finalPos = centerPos(player.getBlockPos(), 3);
            if (pair != null) {
                Optional<BlockPos> spawnPos = BlockPos.findClosest(new BlockPos(pair.getFirst().getX(), world.getBottomY(),
                        pair.getFirst().getZ()), 32, world.getTopYInclusive() - 1, p -> {
                    WorldChunk chunk = getChunk(world, p);
                    if (world.isInHeightLimit(p.down().getY()) && chunk.getBlockState(p.down()).isIn(CubliminalBlocks.FLOOR_PALETTE)) {
                        return !chunk.getBlockState(p).isOpaqueFullCube() && chunk.getBlockState(p.up()).isAir();
                    }
                    return false;
                });

                if (spawnPos.isPresent()) {
                    Optional<BlockPos> validPos = BlockPos.findClosest(spawnPos.get(), 32, world.getTopYInclusive() - 1, p -> {
                        WorldChunk chunk = getChunk(world, p);
                        if (world.isInHeightLimit(p.down().getY()) && chunk.getBlockState(p.up()).isOpaqueFullCube()) {
                            BlockState state = chunk.getBlockState(p);
                            return !(state.getBlock() instanceof VentilationPipeBlock)
                                    && !state.isOpaqueFullCube() && chunk.getBlockState(p.down()).isAir();
                        }
                        return false;
                    });

                    if (validPos.isPresent()) {
                        return Pair.of(spawnPos.get(), validPos.get().toCenterPos());
                    }
                }

                finalPos = centerPos(pair.getFirst(), 3);
            }

            return Pair.of(finalPos, finalPos.toCenterPos().add(0, 2.5, 0));
        });

        create(CubliminalRegistrar.HABITABLE_ZONE_KEY, RegistryKeys.toWorldKey(DimensionOptions.OVERWORLD),
                player -> Pair.of(player.getServerWorld().getSpawnPos(), player.getServerWorld().getSpawnPos().toCenterPos()));
    }
}
