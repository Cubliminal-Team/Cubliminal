package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.limit.cubliminal.Cubliminal;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RoomRegistry implements SimpleResourceReloadListener<Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomPreset>> {

    private static final Object2ObjectOpenHashMap<RegistryKey<Biome>, RoomPreset> ROOMS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, RoomPreset> DEFERRED = new Object2ObjectOpenHashMap<>();

    @Override
    public CompletableFuture<Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomPreset>> load(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomPreset> roomPresets = new Object2ObjectOpenHashMap<>();

            for (var presetResource : resourceManager.findResources("worldgen/room/biome", RoomRegistry::isPathValid).entrySet()) {
                RegistryKey<Biome> biome = RegistryKey.of(
                        RegistryKeys.BIOME,
                        Identifier.of(presetResource.getKey().getNamespace(), FilenameUtils.getBaseName(presetResource.getKey().getPath()))
                );
                decodePreset(roomPresets, Either.left(biome), presetResource, resourceManager);
            }

            for (var presetResource : resourceManager.findResources("worldgen/room/deferred", RoomRegistry::isPathValid).entrySet()) {
                String mappingKey = FilenameUtils.getBaseName(presetResource.getKey().getPath());
                decodePreset(roomPresets, Either.right(mappingKey), presetResource, resourceManager);
            }

            return roomPresets;
        });
    }

    @Override
    public CompletableFuture<Void> apply(Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomPreset> rooms, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            ROOMS.clear();
            rooms.forEach((either, roomPreset) -> {
                either.ifLeft(biome -> ROOMS.put(biome, roomPreset));
                either.ifRight(key -> DEFERRED.put(key, roomPreset));
            });
        }, executor);
    }

    private static boolean isPathValid(Identifier path) {
        return path.getPath().endsWith(".json");
    }

    private static void decodePreset(Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomPreset> roomPresets,
                                     Either<RegistryKey<Biome>, String> key, Map.Entry<Identifier, Resource> presetResource,
                                     ResourceManager resourceManager) {
        try (Reader reader = presetResource.getValue().getReader()) {
            roomPresets.computeIfAbsent(key, k -> {
                var rawPreset = RoomPreset.CODEC.parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader)).getOrThrow();
                return RoomPreset.parse(resourceManager, rawPreset.getFirst(), rawPreset.getSecond());
            });
        } catch (IOException e) {
            Cubliminal.LOGGER.error("Couldn't parse room preset json file in: {}", presetResource.getKey());
        }
    }

    public static boolean contains(RegistryKey<Biome> biome) {
        return ROOMS.containsKey(biome);
    }

    public static boolean contains(String key) {
        return DEFERRED.containsKey(key);
    }

    public static RoomPreset getPreset(RegistryKey<Biome> biome) {
        return ROOMS.get(biome);
    }

    public static RoomPreset getPreset(String key) {
        return DEFERRED.get(key);
    }

    public static float getSpacing(RegistryKey<Biome> biome) {
        return ROOMS.get(biome).spacing();
    }

    public static float getSpacing(String key) {
        return DEFERRED.get(key).spacing();
    }

    public static Room forBiome(RegistryKey<Biome> biome, Random random) {
        return ROOMS.get(biome).holder().random(random);
    }

    public static Room forKey(String key, Random random) {
        return DEFERRED.get(key).holder().random(random);
    }

    @Override
    public Identifier getFabricId() {
        return Cubliminal.id("room_preset_loader");
    }
}