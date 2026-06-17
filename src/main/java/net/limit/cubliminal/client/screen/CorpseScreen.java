package net.limit.cubliminal.client.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class CorpseScreen extends ScreenHandler {
    private final Inventory inventory;

    public CorpseScreen(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);

        // Sets the player inventory.
        this.inventory = inventory;

        // Corpse inventory (6 rows = 54 slots)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                // Adds the slots.
                this.addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18) {

                    @Override
                    public boolean canInsert(ItemStack stack) {
                        // Receives player entity
                        PlayerEntity player = playerInventory.player;
                        // Lets the player insert items into slots depending on if they are in creative mode or not.
                        // True = Player can insert items in slots.
                        // False = Players cannot insert items in slots.
                        return player.isCreative();
                    }
                });
            }
        }

        // Player inventory
        int startY = 140;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, startY + 58));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);

        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack().copy();

        if (index < 54) {
            // from corpse → player
            if (!this.insertItem(slot.getStack(), 54, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // player → corpse (BLOCKED)
            return ItemStack.EMPTY;
        }

        slot.markDirty();
        return original;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
