package net.limit.cubliminal.item;

import net.limit.cubliminal.block.custom.CanBlock;
import net.limit.cubliminal.fluid.AlmondWaterFluid;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.Validate;

public class CanItem extends BlockItem {

    public CanItem(Block block, Settings settings) {
        super(block, settings);
        Validate.isInstanceOf(CanBlock.class, block);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (!world.canPlayerModifyAt(user, blockPos)) {
                    return ActionResult.PASS;
                }

                if (world.getFluidState(blockPos).getFluid() instanceof AlmondWaterFluid) {
                    world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    world.emitGameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
                    return ActionResult.SUCCESS.withNewHandStack(this.fill(itemStack, user, new ItemStack(CubliminalBlocks.ALMOND_WATER_CAN)));
                }
            }

        }
        return ActionResult.PASS;
    }

    protected ItemStack fill(ItemStack stack, PlayerEntity player, ItemStack outputStack) {
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return ItemUsage.exchangeStack(stack, player, outputStack);
    }
}
