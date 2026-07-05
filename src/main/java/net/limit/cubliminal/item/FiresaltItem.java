package net.limit.cubliminal.item;

import net.limit.cubliminal.entity.projectiles.FiresaltEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FiresaltItem extends Item implements ProjectileItem {
    public FiresaltItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (!CampfireBlock.canBeLit(blockState) && !CandleBlock.canBeLit(blockState) && !CandleCakeBlock.canBeLit(blockState)) {

            BlockPos sideBlockPos = blockPos.offset(context.getSide());
            if (AbstractFireBlock.canPlaceAt(world, sideBlockPos, context.getHorizontalPlayerFacing())) {
                world.playSound(playerEntity, sideBlockPos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState fireBlockState = AbstractFireBlock.getState(world, sideBlockPos);
                world.setBlockState(sideBlockPos, fireBlockState, Block.NOTIFY_ALL_AND_REDRAW);
                world.emitGameEvent(playerEntity, GameEvent.BLOCK_PLACE, blockPos);

                ItemStack itemStack = context.getStack();

                if (playerEntity instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, sideBlockPos, itemStack);
                }

                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }

        } else {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            world.setBlockState(blockPos, blockState.with(Properties.LIT, true), Block.NOTIFY_ALL_AND_REDRAW);
            world.emitGameEvent(playerEntity, GameEvent.BLOCK_CHANGE, blockPos);

            return ActionResult.SUCCESS;
        }
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        entity.setOnFireFor(2.5f);
        user.getWorld().playSound(user, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, user.getWorld().getRandom().nextFloat() * 0.4F + 0.8F);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(
                null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (world instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity(FiresaltEntity::new, serverWorld, itemStack, user, 0.0F, 1.5F, 1.0F);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);

        return ActionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!entity.isFireImmune()) {
            entity.setFireTicks(20);
        }
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        return new FiresaltEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
    }
}