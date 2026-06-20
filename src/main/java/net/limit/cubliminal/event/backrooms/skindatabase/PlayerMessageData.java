package net.limit.cubliminal.event.backrooms.skindatabase;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;

import java.util.*;

/**
 * Player messages data object
 * <br/>
 * Use {@link PlayerMessageData#createEmpty(UUID)} to create new instance
 */
public class PlayerMessageData implements IPlayerData {
    public static final Codec<PlayerMessageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(PlayerMessageData::getUuid),
            PlayerMessageProcessor.ProcessedMessage.CODEC.listOf().fieldOf("messages").forGetter(PlayerMessageData::getMessagesAsList)
    ).apply(instance, PlayerMessageData::new));

    private final UUID uuid;
    private final Set<PlayerMessageProcessor.ProcessedMessage> messages;

    private PlayerMessageData(UUID uuid, List<PlayerMessageProcessor.ProcessedMessage> messages) {
        this.uuid = uuid;
        this.messages = new HashSet<>(messages);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public Set<PlayerMessageProcessor.ProcessedMessage> getMessages() {
        return messages;
    }

    public void addMessage(PlayerMessageProcessor.ProcessedMessage message) {
        this.messages.add(message);
    }

    private List<PlayerMessageProcessor.ProcessedMessage> getMessagesAsList() {
        return messages.stream().toList();
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
