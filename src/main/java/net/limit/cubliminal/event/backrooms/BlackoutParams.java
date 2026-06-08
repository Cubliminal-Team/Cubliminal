package net.limit.cubliminal.event.backrooms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BlackoutParams implements SimpleResourceReloadListener<Int2ObjectOpenHashMap<BlackoutManager.Entry>> {

    private static final Codec<Int2ObjectOpenHashMap<BlackoutManager.Entry>> CODEC = BlackoutManager.Entry.CODEC.listOf()
            .xmap(BlackoutParams::toMap, map -> map.values().stream().toList())
            .fieldOf("entries").codec();

    private static final Int2ObjectOpenHashMap<BlackoutManager.Entry> REGISTRIES = new Int2ObjectOpenHashMap<>();

    @Override
    public CompletableFuture<Int2ObjectOpenHashMap<BlackoutManager.Entry>> load(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Int2ObjectOpenHashMap<BlackoutManager.Entry> entries = new Int2ObjectOpenHashMap<>();
            try {
                Resource resource = resourceManager.getResourceOrThrow(Cubliminal.id("settings/blackout.json"));
                Reader reader = resource.getReader();
                entries = CODEC.parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader)).getOrThrow();
            } catch (IOException e) {
                Cubliminal.LOGGER.error("Couldn't parse blackout json file");
            }

            return entries;
        });
    }

    @Override
    public CompletableFuture<Void> apply(Int2ObjectOpenHashMap<BlackoutManager.Entry> entries, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            REGISTRIES.clear();
            REGISTRIES.putAll(entries);
        }, executor);
    }

    public static Int2ObjectOpenHashMap<BlackoutManager.Entry> toMap(List<BlackoutManager.Entry> list) {
        Int2ObjectOpenHashMap<BlackoutManager.Entry> entries = new Int2ObjectOpenHashMap<>(list.size());
        list.forEach(entry -> {
            if (!entries.containsKey(entry.id())) {
                entries.put(entry.id(), entry);
            } else {
                throw new IllegalArgumentException("Found duplicate of ID: " + entry.id());
            }
        });

        return entries;
    }

    public static boolean contains(int id) {
        return REGISTRIES.containsKey(id);
    }

    public static BlackoutManager.Entry getEntry(int id) {
        return REGISTRIES.get(id);
    }

    public static BlackoutManager.Entry getOrThrow(int id) {
        if (REGISTRIES.containsKey(id)) {
            return REGISTRIES.get(id);
        } else {
            throw new InvalidIdentifierException("Couldn't find a matching entry for id: " + id);
        }
    }

    public static ObjectCollection<BlackoutManager.Entry> getValues() {
        return REGISTRIES.values();
    }

    @Override
    public Identifier getFabricId() {
        return Cubliminal.id("blackout_parameter_loader");
    }

}