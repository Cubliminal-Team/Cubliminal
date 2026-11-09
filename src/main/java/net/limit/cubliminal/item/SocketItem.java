package net.limit.cubliminal.item;

import net.limit.cubliminal.block.custom.BlockWithSocket;
import net.limit.cubliminal.block.entity.SocketBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class SocketItem extends BlockItem {

    public SocketItem(Block block, Settings settings) {
        super(block, settings);
        Validate.isInstanceOf(BlockWithSocket.class, block);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState oldState = world.getBlockState(pos);
        ActionResult result = super.useOnBlock(context);
        if (result.isAccepted() && world.getBlockEntity(pos) instanceof SocketBlockEntity socketEntity) {
            socketEntity.setSupport(oldState);
        }

        return result;
    }

    @Nullable
    @Override
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        return new SocketPlacementContext(context);
    }

    static class SocketPlacementContext extends ItemPlacementContext {
        public SocketPlacementContext(ItemPlacementContext context) {
            super(context);
            this.canReplaceExisting = true;
        }

        @Override
        public BlockPos getBlockPos() {
            return this.getHitResult().getBlockPos();
        }
    }
}
