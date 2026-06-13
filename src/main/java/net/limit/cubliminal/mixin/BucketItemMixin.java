package net.limit.cubliminal.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.limit.cubliminal.block.custom.pipe.AbstractPipeBlock;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @Shadow
    @Final
    private Fluid fluid;

    @Shadow
    protected abstract void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos);

    @Shadow
    public abstract void onEmptied(@Nullable PlayerEntity player, World world, ItemStack stack, BlockPos pos);

    @Shadow
    public static ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        throw new UnsupportedOperationException();
    }

    @Inject(method = "use", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 1), cancellable = true)
    private void onUseBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir,
                                 @Local ItemStack itemStack, @Local(ordinal = 0) BlockPos pos,
                                 @Local(ordinal = 1) BlockPos face, @Local BlockState state) {
        if (state.getBlock() instanceof AbstractPipeBlock && state.get(AbstractPipeBlock.LEAKING)) {
            if (world.setBlockState(pos, state.with(AbstractPipeBlock.FLUID_CONTAINER, AbstractPipeBlock
                    .toFluidContainer(this.fluid)), Block.NOTIFY_ALL_AND_REDRAW)) {
                BlockPos blockPos3 = this.fluid == Fluids.WATER ? pos : face;
                this.playEmptyingSound(user, world, pos);
                this.onEmptied(user, world, itemStack, blockPos3);
                if (user instanceof ServerPlayerEntity serverPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger(serverPlayerEntity, blockPos3, itemStack);
                }

                user.incrementStat(Stats.USED.getOrCreateStat((BucketItem) (Object) this));
                ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, getEmptiedStack(itemStack, user));

                cir.setReturnValue(ActionResult.SUCCESS.withNewHandStack(itemStack2));
                cir.cancel();
            } else {
                cir.setReturnValue(ActionResult.FAIL);
                cir.cancel();
            }
        }
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 0), cancellable = true)
    private void onUseEmptyBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir,
                                  @Local ItemStack itemStack, @Local(ordinal = 0) BlockPos pos) {
        BlockState targetState = world.getBlockState(pos);
        if (targetState.getBlock() instanceof AbstractPipeBlock && targetState.get(AbstractPipeBlock.LEAKING)) {
            Fluid pipeFluid = AbstractPipeBlock.toFluid(targetState);
            world.setBlockState(pos, targetState.with(AbstractPipeBlock.FLUID_CONTAINER,
                    AbstractPipeBlock.toFluidContainer(Fluids.EMPTY)), Block.NOTIFY_ALL_AND_REDRAW);
            ItemStack newStack = new ItemStack(pipeFluid.getBucketItem());
            if (!newStack.isEmpty()) {
                user.incrementStat(Stats.USED.getOrCreateStat((BucketItem) (Object) this));
                pipeFluid.getBucketFillSound().ifPresent(sound -> user.playSound(sound, 1.0f, 1.0f));
                world.emitGameEvent(user, GameEvent.FLUID_PICKUP, pos);
                ItemStack itemStack3 = ItemUsage.exchangeStack(itemStack, user, newStack);
                if (!world.isClient()) {
                    Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity) user, newStack);
                }

                cir.setReturnValue(ActionResult.SUCCESS.withNewHandStack(itemStack3));
                cir.cancel();
            }
        }
    }
}
