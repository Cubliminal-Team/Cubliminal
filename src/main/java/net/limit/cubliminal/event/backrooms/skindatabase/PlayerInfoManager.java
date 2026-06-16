package net.limit.cubliminal.event.backrooms.skindatabase;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfoManager {
    private static PlayerInfoManager INSTANCE;

    private final PlayerDataManager<PlayerSkinData> skins;
    private final PlayerDataManager<PlayerMessageData> messages;

    private Data data;

    public PlayerInfoManager() {
        INSTANCE = this;

        this.skins = new PlayerDataManager<>(() -> this.data.getPlayerSkinData(), () -> this.data.markDirty());
        this.messages = new PlayerDataManager<>(() -> this.data.getPlayerMessageData(), () -> this.data.markDirty());
    }

    /**
     * Get skins data
     * @return instance of skins data manager
     */
    public PlayerDataManager<PlayerSkinData> getSkins() {
        return skins;
    }

    /**
     * Get messages data
     * @return instance of messages data manager
     */
    public PlayerDataManager<PlayerMessageData> getMessages() {
        return messages;
    }

    public PersistentState.Type<Data> getPersistentStateType() {
        return new PersistentState.Type<>(
                () -> this.data = new Data(),
                (nbtCompound, wrapperLookup) -> this.data = Data.fromNbt(nbtCompound, wrapperLookup),
                null
        );
    }

    /**
     * Get the current instance of player info manager
     * @return instance
     */
    public static PlayerInfoManager getInstance() {
        return INSTANCE;
    }

    public static void processPlayerMessage(Text content, ServerPlayerEntity player) {

    }

    // The main data chunk
    public static class Data extends PersistentState {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PlayerSkinData.CODEC.listOf().fieldOf("skins").forGetter(Data::getPlayerSkinData),
                PlayerMessageData.CODEC.listOf().fieldOf("messages").forGetter(Data::getPlayerMessageData)
        ).apply(instance, Data::new));

        private final List<PlayerSkinData> playerSkinData;
        private final List<PlayerMessageData> playerMessageData;

        private Data(List<PlayerSkinData> playerSkinData, List<PlayerMessageData> playerMessageData) {
            this.playerSkinData = new ArrayList<>(playerSkinData);
            this.playerMessageData = new ArrayList<>(playerMessageData);
        }

        public Data() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        public List<PlayerSkinData> getPlayerSkinData() {
            return playerSkinData;
        }

        public List<PlayerMessageData> getPlayerMessageData() {
            return playerMessageData;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            return (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial().orElseThrow();
        }

        public static Data fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
            return CODEC.decode(new Dynamic<>(NbtOps.INSTANCE, nbtCompound)).resultOrPartial().orElseThrow().getFirst();
        }
    }
}
