package net.limit.cubliminal.event.backrooms.skindatabase;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Player skin data object
 * <br/>
 * Use {@link PlayerSkinData#createFromPlayer(ServerPlayerEntity)} to create new instance
 */
public class PlayerSkinData implements IPlayerData {
    public static final Codec<PlayerSkinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(PlayerSkinData::getUuid),
            TextCodecs.CODEC.fieldOf("displayName").forGetter(PlayerSkinData::getDisplayName)
    ).apply(instance, PlayerSkinData::new));

    private final UUID uuid;
    private Text displayName;

    private PlayerSkinData(UUID uuid, Text displayName) {
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
     * @return new player skin data instance
     */
    public static PlayerSkinData createFromPlayer(ServerPlayerEntity player) {
        return createPlayerData(player.getUuid(), player.getName());
    }
}