package net.limit.cubliminal.event.backrooms;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerSkinDataManager {
    private static PlayerSkinDataManager INSTANCE;

    private Data data;

    public PlayerSkinDataManager() {
        INSTANCE = this;
    }

    /**
     * Update player data entry
     *
     * @param uuid player uuid
     * @param consumer updater
     * @return new the player data
     */
    public PlayerSkinData updatePlayerData(UUID uuid, Consumer<PlayerSkinData> consumer) {
        PlayerSkinData playerSkinData = this.getPlayerData(uuid).orElseThrow();
        consumer.accept(playerSkinData);
        this.data.markDirty();

        return playerSkinData;
    }

    /**
     * Overwrite player data.
     * {@link PlayerSkinData#uuid} define which player to overwrite
     *
     * @param playerSkinData new player data
     */
    public void storePlayerData(PlayerSkinData playerSkinData) {
        if (this.hasPlayerData(playerSkinData.getUuid())) {
            this.deletePlayerData(playerSkinData.getUuid());
        }

        this.data.getPlayerSkinData().add(playerSkinData);
        this.data.markDirty();
    }

    /**
     * Check if the player has data in the database
     *
     * @param uuid player uuid
     * @return true if the player has data in the database, otherwise return false
     */
    public boolean hasPlayerData(UUID uuid) {
        return this.data.getPlayerSkinData().stream().anyMatch(playerSkinData -> playerSkinData.uuid.equals(uuid));
    }

    /**
     * Get the player data from the database
     *
     * @param uuid player uuid
     * @return Optional of the player data
     */
    public Optional<PlayerSkinData> getPlayerData(UUID uuid) {
        return this.data.getPlayerSkinData().stream().filter(playerSkinData -> playerSkinData.uuid.equals(uuid)).findFirst();
    }

    /**
     * Get random entry from the database
     *
     * @param random random
     * @return player data entry
     */
    public PlayerSkinData getRandomPlayer(Random random) {
        int entryCount = this.getEntryCount();
        if (entryCount <= 0) return null;
        if (entryCount == 1) return this.data.getPlayerSkinData().getFirst();

        return this.data.getPlayerSkinData().get(random.nextBetween(0, entryCount - 1));
    }

    /**
     * Delete every data from the specific uuid
     * @param uuid player uuid
     */
    public void deletePlayerData(UUID uuid) {
        this.data.getPlayerSkinData().removeIf(playerSkinData -> playerSkinData.uuid.equals(uuid));
        this.data.markDirty();
    }

    /**
     * Get all data stored
     * <br/>
     * DO NOT EDIT ANY DATA FROM HERE IT WILL NOT UPDATE
     *
     * @return immutable list of the data
     */
    public ImmutableList<PlayerSkinData> getAllData() {
        return ImmutableList.copyOf(this.data.getPlayerSkinData());
    }

    public int getEntryCount() {
        return this.data.playerSkinData.size();
    }

    public PersistentState.Type<Data> getPersistentStateType() {
        return new PersistentState.Type<>(
                () -> this.data = new Data(),
                (nbtCompound, wrapperLookup) -> this.data = Data.fromNbt(nbtCompound, wrapperLookup),
                null
        );
    }

    /**
     * Create new instance of {@link PlayerSkinData}
     * @param uuid player uuid
     * @param displayName player display name
     * @return new player data instance
     */
    public static PlayerSkinData createPlayerData(UUID uuid, Text displayName) {
        return new PlayerSkinData(uuid, displayName);
    }

    /**
     * Create new instance of {@link PlayerSkinData} from the player data
     * @param player server player
     * @return new player data instance
     */
    public static PlayerSkinData createFromPlayer(ServerPlayerEntity player) {
        return createPlayerData(player.getUuid(), player.getName());
    }

    public static PlayerSkinDataManager getInstance() {
        return INSTANCE;
    }

    // The main data chunk
    public static class Data extends PersistentState {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PlayerSkinData.CODEC.listOf().fieldOf("players").forGetter(Data::getPlayerSkinData)
        ).apply(instance, Data::new));

        private final List<PlayerSkinData> playerSkinData;

        private Data(List<PlayerSkinData> playerSkinData) {
            this.playerSkinData = new ArrayList<>(playerSkinData);
        }

        public Data() {
            this(new ArrayList<>());
        }

        public List<PlayerSkinData> getPlayerSkinData() {
            return playerSkinData;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            return (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial().orElseThrow();
        }

        public static Data fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
            Cubliminal.LOGGER.info(String.valueOf(nbtCompound.getKeys().size()));
            Cubliminal.LOGGER.info(String.valueOf(nbtCompound.getKeys().toString()));
            return CODEC.decode(new Dynamic<>(NbtOps.INSTANCE, nbtCompound)).resultOrPartial().orElseThrow().getFirst();
        }
    }

    /**
     * Player skin data object
     * <br/>
     * Use {@link PlayerSkinDataManager#createFromPlayer(ServerPlayerEntity)} to create new instance
     */
    public static class PlayerSkinData {
        public static final Codec<PlayerSkinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Uuids.CODEC.fieldOf("uuid").forGetter(PlayerSkinData::getUuid),
                TextCodecs.CODEC.fieldOf("displayName").forGetter(PlayerSkinData::getDisplayName)
        ).apply(instance, PlayerSkinData::new));

        private final UUID uuid;
        private Text displayName;

        protected PlayerSkinData(UUID uuid, Text displayName) {
            this.uuid = uuid;
            this.displayName = displayName;
        }

        public UUID getUuid() {
            return uuid;
        }

        public Text getDisplayName() {
            return displayName;
        }

        public void setDisplayName(Text displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return "UUID=%s | DisplayName=%s".formatted(this.getUuid().toString(), this.getDisplayName().toString());
        }
    }
}
