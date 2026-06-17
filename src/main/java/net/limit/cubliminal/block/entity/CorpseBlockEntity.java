package net.limit.cubliminal.block.entity;

import net.limit.cubliminal.client.screen.CorpseScreen;
import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class CorpseBlockEntity extends LootableContainerBlockEntity {
    // The inventory size.
    public static final int INVENTORY_SIZE = 54;
    // The item stack list.
    private DefaultedList<ItemStack> inventory;
    // The UUID of the player skin. The default is that of Steve.
    private UUID uuid = UUID.fromString("42424242-4242-4242-4242-424242424242");
    // The player name.
    private String playerName = null;

    protected CorpseBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        // Sets the list as empty.
        this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    public CorpseBlockEntity(BlockPos pos, BlockState state) {
        this(CubliminalBlockEntities.CORPSE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        // Checks to see if playerName is null or emptied
        if (playerName == null || playerName.isEmpty()) {
            // Sets the container text to the default.
            return Text.translatable("container.corpse.default");
        }
        // Set the container text to that of the player.
        return Text.translatable("container.corpse.player", this.playerName);
    }

    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CorpseScreen(syncId, playerInventory, this);
    }

    @Override
    public int size() {
        return INVENTORY_SIZE;
    }

    /**
     * Sets the player name for the corpse.
     * @param playerName The player name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Gets the player name the corpse belongs to.
     * @return Player's name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the UUID skin of the corpse model.
     * @param uuid UUID of skin
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Receives the UUID of the player's skin.
     * @return UUID of skin
     */
    public UUID getUuid() {
        return uuid;
    }

    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        // Sets the inventory to empty.
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        // Reads the inventory data.
        if (!this.readLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory, registries);
        }
        // Does nbt contain data on player name
        if (nbt.contains("playerName")){
            // Receives the player name.
            this.playerName = nbt.getString("playerName");
        }
        // Receives the UUID of player skin from nbt.
        this.uuid = nbt.getUuid("uuid");
    }

    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        // Saves the inventory data.
        if (!this.writeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, registries);
        }
        // Checks to see if player name is not null.
        if (this.playerName != null) {
            // Saves player name to nbt.
            nbt.putString("playerName", this.playerName);
        }
        // Save UUID to nbt.
        nbt.putUuid("uuid", this.uuid);
    }
}
