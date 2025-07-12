package net.limit.cubliminal.world.connection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ConnectionRegistry implements SimpleResourceReloadListener<Object2ObjectOpenHashMap<RegistryKey<Biome>, Pair<Identifier, Connection>>> {

    private static final Object2ObjectOpenHashMap<Identifier, Connection> BY_ID = new Object2ObjectOpenHashMap<>();
    private static final Multimap<RegistryKey<Biome>, Connection> BY_BIOME = HashMultimap.create();

    @Override
    public CompletableFuture<Object2ObjectOpenHashMap<RegistryKey<Biome>, Pair<Identifier, Connection>>> load(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Object2ObjectOpenHashMap<RegistryKey<Biome>, Pair<Identifier, Connection>> data = new Object2ObjectOpenHashMap<>();
            for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources("worldgen/connection", id -> id.getPath().endsWith(".json")).entrySet()) {
                Identifier resourceId = entry.getKey();
                try (Reader reader = entry.getValue().getReader()) {
                    RegistryKey<Biome> biome = RegistryKey.of(RegistryKeys.BIOME, biomeId(resourceId));
                    data.computeIfAbsent(biome, key -> {
                        Identifier id = Identifier.of(resourceId.getNamespace(), FilenameUtils.getBaseName(resourceId.getPath()));
                        DataResult<Connection> connection = Connection.CODEC.codec().parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader));
                        return Pair.of(id, connection.getOrThrow());
                    });
                } catch (IOException e) {
                    Cubliminal.LOGGER.error("Couldn't parse json file in: {}", resourceId);
                }
            }

            return data;
        });
    }

    @Override
    public CompletableFuture<Void> apply(Object2ObjectOpenHashMap<RegistryKey<Biome>, Pair<Identifier, Connection>> data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            BY_ID.clear();
            BY_BIOME.clear();
            data.forEach((biome, pair) -> {
                if (BY_ID.containsKey(pair.getFirst())) Cubliminal.LOGGER.error("Found duplicate connection: {}", pair.getFirst());
                BY_ID.put(pair.getFirst(), pair.getSecond());
                BY_BIOME.put(biome, pair.getSecond());
            });
        });
    }

    private static Identifier biomeId(Identifier resourceId) {
        String[] subStrings = resourceId.getPath().split("/");
        int index = -1;
        for (int i = 0; i < subStrings.length; i++) {
            if (subStrings[i].equals("connection")) {
                index = i;
                break;
            }
        }
        if (index == -1 || index + 1 >= subStrings.length) {
            throw new IllegalArgumentException("Invalid path format, missing 'connection' folder: " + resourceId);
        }
        return Identifier.of(resourceId.getNamespace(), subStrings[index + 1]);
    }

    public static boolean containsId(Identifier id) {
        return BY_ID.containsKey(id);
    }

    public static boolean containsBiome(RegistryKey<Biome> biome) {
        return BY_BIOME.containsKey(biome);
    }

    public static Connection getMapping(Identifier id) {
        return BY_ID.get(id);
    }

    public static Connection forBiome(RegistryKey<Biome> biome, Random random) {
        Collection<Connection> mappings = BY_BIOME.get(biome);
        return mappings.stream().toList().get(random.nextInt(mappings.size()));
    }

    public static Collection<Connection> getMappings(RegistryKey<Biome> biome) {
        return BY_BIOME.get(biome);
    }

    public static ObjectSet<Identifier> byId() {
        return BY_ID.keySet();
    }

    @Override
    public Identifier getFabricId() {
        return Cubliminal.id("floor_connection_loader");
    }
}
