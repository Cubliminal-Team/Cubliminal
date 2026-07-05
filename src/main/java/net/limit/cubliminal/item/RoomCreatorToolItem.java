package net.limit.cubliminal.item;

import net.limit.cubliminal.client.screen.roomcreator.RoomCreatorDataManager;
import net.limit.cubliminal.init.CubliminalItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RoomCreatorToolItem extends Item {

    public RoomCreatorToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && player.isCreativeLevelTwoOp()) {
            World world = context.getWorld();

            if (!world.getBlockState(context.getBlockPos()).isAir()) {
                if (world.isClient()) {
                    RoomCreatorDataManager.INSTANCE.setCorner2(context.getBlockPos(), player);
                }

                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }

    public static void afterBreakingBlock(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        if (player.getMainHandStack().isOf(CubliminalItems.ROOM_CREATOR_TOOL) && player.isCreativeLevelTwoOp()) {
            world.setBlockState(pos, state, Block.FORCE_STATE);
            if (world.isClient()) {
                RoomCreatorDataManager.INSTANCE.setCorner1(pos, player);
            }
        }
    }
}
