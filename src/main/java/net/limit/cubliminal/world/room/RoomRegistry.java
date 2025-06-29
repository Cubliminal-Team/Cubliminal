package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RoomRegistry implements SimpleResourceReloadListener<Map<RegistryKey<Biome>, RoomRegistry.RoomPreset>> {

    private static final Map<RegistryKey<Biome>, RoomPreset> ROOMS = new HashMap<>();

    @Override
    public CompletableFuture<Map<RegistryKey<Biome>, RoomPreset>> load(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<RegistryKey<Biome>, RoomPreset> roomPresets = new HashMap<>();

            for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources("worldgen/room", id -> id.getPath().endsWith(".json")).entrySet()) {
                try (Reader reader = entry.getValue().getReader()) {
                    RegistryKey<Biome> biome = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(
                            entry.getKey().getNamespace(), FilenameUtils.getBaseName(entry.getKey().getPath())));
                    roomPresets.computeIfAbsent(biome, key -> {
                        DataResult<RoomPreset> rooms = RoomPreset.CODEC.parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader));
                        return rooms.getOrThrow();
                    });
                } catch (IOException e) {
                    Cubliminal.LOGGER.error("Couldn't parse json file in: {}", entry.getKey());
                }
            }

            return roomPresets;
        });
    }

    @Override
    public CompletableFuture<Void> apply(Map<RegistryKey<Biome>, RoomPreset> rooms, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            ROOMS.clear();
            ROOMS.putAll(rooms);
            //ROOMS.values().forEach(preset -> preset.holder().getValues().forEach(room -> Cubliminal.LOGGER.info("Room: {}", room.toString())));
        }, executor);
    }

    public static boolean contains(RegistryKey<Biome> biome) {
        return ROOMS.containsKey(biome);
    }

    public static RoomPreset getPreset(RegistryKey<Biome> biome) {
        return ROOMS.get(biome);
    }

    public static float getSpacing(RegistryKey<Biome> biome) {
        return ROOMS.get(biome).spacing();
    }

    public static Room forBiome(RegistryKey<Biome> biome, Random random) {
        return ROOMS.get(biome).holder().random(random);
    }

    @Override
    public Identifier getFabricId() {
        return Cubliminal.id("room_preset_loader");
    }

    public record RoomPreset(float spacing, WeightedHolderSet<Room> holder) {

        public static final Codec<WeightedHolderSet<Room>> SET_CODEC = WeightedHolderSet.createCodec(Room.CODEC).fieldOf("rooms").codec();

        public static final Codec<RoomPreset> CODEC = Codec
                .pair(Codec.floatRange(0.0f, Float.MAX_VALUE).optionalFieldOf("spacing", 1.0f).codec(), SET_CODEC)
                .xmap(pair -> new RoomPreset(pair.getFirst(), pair.getSecond()), preset -> Pair.of(preset.spacing(), preset.holder()));
    }
}