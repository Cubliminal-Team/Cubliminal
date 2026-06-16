package net.limit.cubliminal.event.backrooms.skindatabase;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Uuids;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Player messages data object
 * <br/>
 * Use {@link PlayerMessageData#createEmpty(UUID)} to create new instance
 */
public class PlayerMessageData implements IPlayerData {
    public static final Codec<PlayerMessageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(PlayerMessageData::getUuid),
            TextCodecs.CODEC.listOf().fieldOf("messages").forGetter(PlayerMessageData::getMessages)
    ).apply(instance, PlayerMessageData::new));

    private final UUID uuid;
    private final List<Text> messages;

    private PlayerMessageData(UUID uuid, List<Text> messages) {
        this.uuid = uuid;
        this.messages = new ArrayList<>(messages);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public List<Text> getMessages() {
        return messages;
    }

    public void addMessage(Text message) {
        this.messages.add(message);
    }

    /**
     * Create empty instance of {@link PlayerSkinData}
     *
     * @param playerUuid player uuid
     * @return new player message data instance
     */
    public static PlayerMessageData createEmpty(UUID playerUuid) {
        return new PlayerMessageData(playerUuid, new ArrayList<>());
    }
}
