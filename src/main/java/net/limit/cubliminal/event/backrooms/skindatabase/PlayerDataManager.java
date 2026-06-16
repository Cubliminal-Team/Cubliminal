package net.limit.cubliminal.event.backrooms.skindatabase;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record PlayerDataManager<T extends IPlayerData>(Supplier<List<T>> getterFunc, Runnable markDirtyFunc) {

    /**
     * Update player data entry
     *
     * @param uuid     player uuid
     * @param consumer updater
     * @return new the player data
     */
    public T updatePlayerData(UUID uuid, Consumer<T> consumer) {
        T playerData = this.getPlayerData(uuid).orElseThrow();
        consumer.accept(playerData);
        this.markDirty();

        return playerData;
    }

    /**
     * Overwrite player data.
     * {@link IPlayerData#getUuid()} define which player to overwrite
     *
     * @param playerData new player data
     */
    public void storePlayerData(T playerData) {
        if (this.hasPlayerData(playerData.getUuid())) {
            this.deletePlayerData(playerData.getUuid());
        }

        this.getData().add(playerData);
        this.markDirty();
    }

    /**
     * Check if the player has data in the database
     *
     * @param uuid player uuid
     * @return true if the player has data in the database, otherwise return false
     */
    public boolean hasPlayerData(UUID uuid) {
        return this.getData().stream().anyMatch(playerData -> playerData.getUuid().equals(uuid));
    }

    /**
     * Get the player data from the database
     *
     * @param uuid player uuid
     * @return Optional of the player data
     */
    public Optional<T> getPlayerData(UUID uuid) {
        return this.getData().stream().filter(playerData -> playerData.getUuid().equals(uuid)).findFirst();
    }

    /**
     * Get random entry from the database
     *
     * @param random random
     * @return player data entry
     */
    public T getRandomPlayer(Random random) {
        int entryCount = this.getEntryCount();
        if (entryCount <= 0) return null;
        if (entryCount == 1) return this.getData().getFirst();

        return this.getData().get(random.nextBetween(0, entryCount - 1));
    }

    /**
     * Delete every data from the specific uuid
     *
     * @param uuid player uuid
     */
    public void deletePlayerData(UUID uuid) {
        this.getData().removeIf(playerData -> playerData.getUuid().equals(uuid));
        this.markDirty();
    }

    /**
     * Get all data stored
     * <br/>
     * DO NOT EDIT ANY DATA FROM HERE IT WILL NOT UPDATE
     *
     * @return immutable list of the data
     */
    public ImmutableList<T> getAllData() {
        return ImmutableList.copyOf(this.getData());
    }

    public int getEntryCount() {
        return this.getData().size();
    }

    private void markDirty() {
        this.markDirtyFunc.run();
    }

    private List<T> getData() {
        return this.getterFunc.get();
    }
}
