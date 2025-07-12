package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RoomRegistry implements SimpleResourceReloadListener<Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomRegistry.RoomPreset>> {

    private static final Object2ObjectOpenHashMap<RegistryKey<Biome>, RoomPreset> ROOMS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, RoomPreset> DEFERRED = new Object2ObjectOpenHashMap<>();

    @Override
    public CompletableFuture<Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomPreset>> load(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Object2ObjectOpenHashMap<Either<RegistryKey<Biome>, String>, RoomPreset> roomPresets = new Object2ObjectOpenHashMap<>();

            for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources("worldgen/room/biome", id -> id.getPath().endsWith(".json")).entrySet()) {
                try (Reader reader = entry.getValue().getReader()) {
                    RegistryKey<Biome> biome = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(
                            entry.getKey().getNamespace(), FilenameUtils.getBaseName(entry.getKey().getPath())));
                    roomPresets.computeIfAbsent(Either.left(biome), key -> {
                        DataResult<RoomPreset> rooms = RoomPreset.CODEC.parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader));
                        return rooms.getOrThrow();
                    });
                } catch (IOException e) {
                    Cubliminal.LOGGER.error("Couldn't parse room preset json file in: {}", entry.getKey());
                }
            }

            for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources("worldgen/room/deferred", id -> id.getPath().endsWith(".json")).entrySet()) {
                try (Reader reader = entry.getValue().getReader()) {
                    String mappingKey = FilenameUtils.getBaseName(entry.getKey().getPath());
                    roomPresets.computeIfAbsent(Either.right(mappingKey), key -> {
                        DataResult<RoomPreset> rooms = RoomPreset.CODEC.parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader));
                        return rooms.getOrThrow();
                    });
                } catch (IOException e) {
                    Cubliminal.LOGGER.error("Couldn't parse deferred room preset json file in: {}", entry.getKey());
                }
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

    public record RoomPreset(float spacing, WeightedHolderSet<Room> holder) {

        public static final Codec<WeightedHolderSet<Room>> SET_CODEC = WeightedHolderSet.createCodec(Room.CODEC).fieldOf("rooms").codec();

        public static final Codec<RoomPreset> CODEC = Codec
                .pair(Codec.floatRange(0.0f, Float.MAX_VALUE).optionalFieldOf("spacing", 1.0f).codec(), SET_CODEC)
                .xmap(pair -> new RoomPreset(pair.getFirst(), pair.getSecond()), preset -> Pair.of(preset.spacing(), preset.holder()));
    }
}