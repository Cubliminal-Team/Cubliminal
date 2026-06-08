package net.limit.cubliminal.item;

import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.block.custom.CanBlock;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.limit.cubliminal.init.CubliminalEffects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

public class AlmondWaterItem extends BlockItem {

    public AlmondWaterItem(Block block, Settings settings) {
        super(block, settings);
        Validate.isInstanceOf(CanBlock.class, block);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        super.finishUsing(stack, world, user);
        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!world.isClient()) {
                ((PEAccessor) serverPlayerEntity).getSanityManager().resetTimer();
                user.removeStatusEffect(CubliminalEffects.PARANOIA);
            }
        }

        ItemStack itemStack = new ItemStack(CubliminalBlocks.CAN);
        if (stack.isEmpty()) {
            return itemStack;
        }
        if (user instanceof PlayerEntity playerEntity && !playerEntity.isCreative() && !playerEntity.getInventory().insertStack(itemStack)) {
            playerEntity.dropItem(itemStack, false);
        }

        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }
}
