package net.limit.cubliminal.item;

import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.block.custom.AlmondWaterBlock;
import net.limit.cubliminal.init.CubliminalEffects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

public class AlmondWaterBlockItem extends BlockItem {

    public AlmondWaterBlockItem(Block block, Settings settings) {
        super(block, settings);
        Validate.isInstanceOf(AlmondWaterBlock.class, block);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        super.finishUsing(stack, world, user);
        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!world.isClient) {
                ((PEAccessor) serverPlayerEntity).getSanityManager().resetTimer();
                user.removeStatusEffect(Registries.STATUS_EFFECT.getEntry(CubliminalEffects.PARANOIA));
            }
        }
        /*
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (user instanceof PlayerEntity playerEntity && !((PlayerEntity) user).getAbilities().creativeMode) {
            ItemStack itemStack = new ItemStack(Items.GLASS_BOTTLE);
            if (!playerEntity.getInventory().insertStack(itemStack)) {
                playerEntity.dropItem(itemStack, false);
            }
        }
         */
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }
}
