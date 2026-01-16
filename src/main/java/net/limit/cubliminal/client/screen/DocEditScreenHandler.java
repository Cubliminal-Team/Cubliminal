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
import net.minecraft.world.World;

public class DocEditScreenHandler extends ScreenHandler {

    private final BlockPos blockEntityPos;
    private final Inventory inventory;

    public DocEditScreenHandler(int syncId, Inventory inventory, BlockPos pos) {
        super(CubliminalScreenHandlers.DOC_EDIT_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.addSlot(new Slot(inventory, 0, 0, 0) {
            @Override
            public void markDirty() {
                super.markDirty();
                DocEditScreenHandler.this.onContentChanged(this.inventory);
            }
        });
        this.blockEntityPos = pos;
    }

    public static DocEditScreenHandler make(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        if (playerInventory.player.getWorld().getBlockEntity(pos) instanceof WrittenDocumentBlockEntity entity) {
            return new DocEditScreenHandler(syncId, entity.getInventory(), pos);
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

    public BlockPos getBlockEntityPos() {
        return this.blockEntityPos;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        World world = player.getWorld();
        if (!world.isClient() && world.getBlockEntity(blockEntityPos) instanceof WrittenDocumentBlockEntity entity) {
            entity.setEditing(false);
        }

        super.onClosed(player);
    }
}
