package net.limit.cubliminal.event.backrooms.skindatabase;

import net.limit.cubliminal.event.backrooms.skindatabase.PlayerMessageProcessor.ProcessedMessage;
import net.limit.cubliminal.event.backrooms.skindatabase.PlayerMessageProcessor.Intent;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * Player messages data object
 * <br/>
 * Use {@link PlayerMessageData#createEmpty(UUID)} to create new instance
 */
public class PlayerMessageData implements IPlayerData {
    public static final Codec<PlayerMessageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(PlayerMessageData::getUuid),
            ProcessedMessage.CODEC.listOf().fieldOf("messages").forGetter(PlayerMessageData::getMessagesAsList)
    ).apply(instance, PlayerMessageData::new));

    private final UUID uuid;
    private final Set<ProcessedMessage> messages;

    private PlayerMessageData(UUID uuid, List<ProcessedMessage> messages) {
        this.uuid = uuid;
        this.messages = new HashSet<>(messages);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public ImmutableSet<ProcessedMessage> getMessages() {
        return ImmutableSet.copyOf(messages);
    }

    public void addMessage(ProcessedMessage message) {
        this.messages.add(message);
    }

    public ImmutableSet<ProcessedMessage> getAllMessageForIntents(Intent... intents) {
        return this.messages.stream().filter(processedMessage -> Arrays.stream(intents).anyMatch(intent -> intent == processedMessage.intent())).collect(ImmutableSet.toImmutableSet());
    }

    public ProcessedMessage getRandomMessageFromIntents(Random random, Intent... intents) {
        ImmutableSet<ProcessedMessage> set = this.getAllMessageForIntents(intents);
        if (set.isEmpty()) return null;

        return Util.getRandom(set.stream().toList(), random);
    }

    private List<ProcessedMessage> getMessagesAsList() {
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
