package net.limit.cubliminal.event.backrooms;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.init.CubliminalSounds;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class BlackoutManager extends PersistentState {

    // Average state tick intervals
    private static final int MIN_OFF = 2400;
    private static final int MAX_OFF = 7200;
    private static final int MIN_LIT = 4800;
    private static final int MAX_LIT = 18000;

    private static final Codec<RegistryKey<Biome>> BIOME_CODEC = RegistryKey.createCodec(RegistryKeys.BIOME);

    public static final AttachmentType<List<Integer>> ATTACHMENT = AttachmentRegistry.create(
            Cubliminal.id("blackout_data"),
            builder -> builder
                    .initializer(() -> List.of())
                    .persistent(Codec.INT.listOf())
    );

    private final Object2IntArrayMap<Entry> blackoutTimers = new Object2IntArrayMap<>();
    private final ServerWorld world;
    private int currentTime;
    private boolean tickChunk = true;

    public static PersistentState.Type<BlackoutManager> getPersistentStateType(ServerWorld world) {
        return new Type<>(
                () -> create(world, 0, new Object2IntArrayMap<>()),
                (nbt, registries) -> fromNbt(world, nbt),
                null
        );
    }

    private BlackoutManager(ServerWorld world, int currentTime, Object2IntArrayMap<Entry> savedEntries) {
        this.world = world;
        this.currentTime = currentTime;
        this.blackoutTimers.putAll(savedEntries);
    }

    public static BlackoutManager create(ServerWorld world, int currentTime, Object2IntArrayMap<Entry> savedEntries) {
        BlackoutManager blackoutManager = new BlackoutManager(world, currentTime, savedEntries);
        for (Entry entry : BlackoutParams.getValues()) {
            blackoutManager.blackoutTimers.computeIfAbsent(entry, e -> {
                Random random = world.getRandom();
                int minLit = entry.minLit();
                int bound = entry.maxLit() - minLit;
                int randomInt = (random.nextInt(bound) + random.nextInt(bound) + random.nextInt(bound)) / 3;
                return 3600 + random.nextInt(minLit) + minLit + randomInt;
            });
        }
        blackoutManager.markDirty();
        return blackoutManager;
    }

    public boolean shouldTickChunk() {
        return this.tickChunk;
    }

    public void onChunkTick() {
        this.tickChunk = false;
    }

    public void playSound(ChunkPos chunkPos, int y, boolean lightsOff) {
        SoundEvent soundEvent = lightsOff ? CubliminalSounds.POWER_DOWN.value() : CubliminalSounds.POWER_ON.value();
        CubliminalSounds.blockPlaySound(this.world, chunkPos.getCenterAtY(y), soundEvent);
    }

    public void tick() {
        ++this.currentTime;
        this.tickChunk = true;

        for (Entry entry : this.blackoutTimers.keySet()) {
            int ticks = this.blackoutTimers.getInt(entry);
            if (ticks <= 0) {
                ++ticks;
                if (ticks == 0) {
                    Random random = this.world.getRandom();
                    int bound = entry.maxLit() - entry.minLit();
                    ticks = entry.minLit() + (random.nextInt(bound) + random.nextInt(bound) + random.nextInt(bound)) / 3;
                }
            } else {
                --ticks;
                if (ticks == 0) {
                    Random random = this.world.getRandom();
                    int bound = entry.maxOff() - entry.minOff();
                    ticks = -(entry.minOff() + (random.nextInt(bound) + random.nextInt(bound) + random.nextInt(bound)) / 3);
                }
            }
            this.blackoutTimers.put(entry, ticks);
        }

        if (this.currentTime % 200 == 0) {
            this.markDirty();
        }
    }

    public Optional<Entry> forBiome(RegistryKey<Biome> biomeKey) {
        for (Entry entry : this.blackoutTimers.keySet()) {
            if (entry.affected().contains(biomeKey)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public boolean areLightsOff(Entry entry) {
        return this.blackoutTimers.getInt(entry) <= 0;
    }

    public void toggleState(Entry entry, boolean lightsOff) {
        if (this.areLightsOff(entry) != lightsOff) {
            Random random = this.world.getRandom();
            int ticks;
            if (lightsOff) {
                int bound = entry.maxOff() - entry.minOff();
                ticks = entry.minOff() + (random.nextInt(bound) + random.nextInt(bound) + random.nextInt(bound)) / 3;
            } else {
                int bound = entry.maxLit() - entry.minLit();
                ticks = entry.minLit() + (random.nextInt(bound) + random.nextInt(bound) + random.nextInt(bound)) / 3;
            }
            this.toggleState(entry, ticks, lightsOff);
            this.markDirty();
        }
    }

    public void toggleState(Entry entry, int ticks, boolean lightsOff) {
        this.blackoutTimers.put(entry, lightsOff ? -ticks : ticks);
    }

    public boolean lightsOffInChunk(ChunkPos chunkPos, int sectionY) {
        RegistryKey<Biome> biome = this.world.getGeneratorStoredBiome(
                BiomeCoords.fromChunk(chunkPos.x), BiomeCoords.fromBlock(sectionY), BiomeCoords.fromChunk(chunkPos.z)
        ).getKey().orElseThrow();
        Optional<Entry> optional = this.forBiome(biome);
        return optional.isPresent() && this.areLightsOff(optional.get());
    }

    public boolean lightsOffInSection(Chunk chunk, int sectionIndex) {
        return chunk.getAttachedOrElse(ATTACHMENT, List.of()).contains(sectionIndex);
    }

    public boolean lightsOffIn(BlockPos pos) {
        Chunk chunk = this.world.getChunk(pos);
        int sectionIndex = chunk.getSectionIndex(pos.getY());
        return chunk.getAttachedOrElse(ATTACHMENT, List.of()).contains(sectionIndex);
    }

    public static BlackoutManager fromNbt(ServerWorld world, NbtCompound nbt) {
        Object2IntArrayMap<Entry> savedEntries = new Object2IntArrayMap<>();
        NbtList nbtList = nbt.getList("Timers", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            Pair<Entry, Integer> entry = readCompound(nbtCompound);
            savedEntries.put(entry.getFirst(), entry.getSecond());
        }

        int currentTime = nbt.getInt("Tick");
        BlackoutManager blackoutManager = create(world, currentTime, savedEntries);
        return blackoutManager;
    }

    public static Pair<Entry, Integer> readCompound(NbtCompound nbt) {
        int timerTicks = nbt.getInt("TimerTicks");
        int id = nbt.getInt("Id");
        return Pair.of(BlackoutParams.getOrThrow(id), timerTicks);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("Tick", this.currentTime);
        NbtList nbtList = new NbtList();

        for (Object2IntMap.Entry<Entry> entry : this.blackoutTimers.object2IntEntrySet()) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putInt("TimerTicks", entry.getIntValue());
            nbtCompound.putInt("Id", entry.getKey().id());
            nbtList.add(nbtCompound);
        }

        nbt.put("Timers", nbtList);
        return nbt;
    }

    public record Entry(List<RegistryKey<Biome>> affected, int id, int minLit, int maxLit, int minOff, int maxOff) {
        public static Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BIOME_CODEC.listOf().fieldOf("affected").forGetter(Entry::affected),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("id").forGetter(Entry::id),
                Codec.intRange(1200, Integer.MAX_VALUE).fieldOf("min_lit").forGetter(Entry::minLit),
                Codec.intRange(1200, Integer.MAX_VALUE).fieldOf("max_lit").forGetter(Entry::maxLit),
                Codec.intRange(1200, Integer.MAX_VALUE).fieldOf("min_off").forGetter(Entry::minOff),
                Codec.intRange(1200, Integer.MAX_VALUE).fieldOf("max_off").forGetter(Entry::maxOff)
        ).apply(instance, Entry::new));

        public Entry(List<RegistryKey<Biome>> affected, int id, int minLit, int maxLit, int minOff, int maxOff) {
            this.affected = affected;
            this.id = id;
            if (maxLit - minLit <= 0) {
                throw new IllegalArgumentException("Minimum lit ticks should be always smaller than maximum lit ticks");
            }
            this.minLit = minLit;
            this.maxLit = maxLit;
            if (maxOff - minOff <= 0) {
                throw new IllegalArgumentException("Minimum off ticks should be always smaller than maximum off ticks");
            }
            this.minOff = minOff;
            this.maxOff = maxOff;
        }

    }

}
