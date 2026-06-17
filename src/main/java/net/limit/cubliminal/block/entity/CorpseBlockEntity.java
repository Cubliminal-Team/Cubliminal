package net.limit.cubliminal.block.entity;

import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class CorpseBlockEntity extends LootableContainerBlockEntity {
    public static final int INVENTORY_SIZE = 54;
    private DefaultedList<ItemStack> inventory;
    private UUID uuid = UUID.fromString("42424242-4242-4242-4242-424242424242");
    private String playerName = null;

    protected CorpseBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    public CorpseBlockEntity(BlockPos pos, BlockState state) {
        this(CubliminalBlockEntities.CORPSE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        if (playerName == null || playerName.isEmpty()) {
            return Text.translatable("container.corpse.default");
        }
        return Text.translatable("container.corpse.player", this.playerName);
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, this);
    }

    @Override
    public int size() {
        return INVENTORY_SIZE;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory, registries);
        }
        if (nbt.contains("playerName")){
            this.playerName = nbt.getString("playerName");
        }
        this.uuid = nbt.getUuid("uuid");
    }

    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (!this.writeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, registries);
        }
        if (this.playerName != null) {
            nbt.putString("playerName", this.playerName);
        }
        nbt.putUuid("uuid", this.uuid);
    }
}
