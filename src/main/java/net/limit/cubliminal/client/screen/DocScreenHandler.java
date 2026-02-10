package net.limit.cubliminal.client.screen;

import net.limit.cubliminal.block.entity.WrittenDocumentBlockEntity;
import net.limit.cubliminal.init.CubliminalScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class DocScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    public DocScreenHandler(int syncId, Inventory inventory) {
        super(CubliminalScreenHandlers.DOC_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.addSlot(new Slot(inventory, 0, 0, 0) {
            @Override
            public void markDirty() {
                super.markDirty();
                DocScreenHandler.this.onContentChanged(this.inventory);
            }
        });
    }

    public static DocScreenHandler make(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getWorld().getBlockEntity(pos) instanceof WrittenDocumentBlockEntity entity) {
            return new DocScreenHandler(syncId, entity.getInventory());
        }

        throw new IllegalArgumentException("Couldn't find a document Block Entity at pos: " + pos.toShortString());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public ItemStack getDocItem() {
        return this.inventory.getStack(0);
    }
}
