package net.limit.cubliminal.fluid;

import net.limit.cubliminal.block.fluid.CustomFluidBlock;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.limit.cubliminal.init.CubliminalFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.world.WorldView;

public class ContaminatedWaterFluid extends BackroomsFlowableFluid {
    public ContaminatedWaterFluid() {
        super(SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS);
    }

    @Override
    public ItemStack getBottleItem() {
        return null;
        //return new ItemStack(BackroomItems.CONTAMINATED_WATER);
    }

    @Override
    public CustomFluidBlock getFluidBlock() {
        return (CustomFluidBlock) CubliminalBlocks.CONTAMINATED_WATER_BLOCK;
    }

    @Override
    public Fluid getFlowing() {
        return CubliminalFluids.FLOWING_CONTAMINATED_WATER;
    }

    @Override
    public Fluid getStill() {
        return CubliminalFluids.CONTAMINATED_WATER;
    }

    @Override
    protected boolean isInfinite(ServerWorld world) {
        return false;
    }

    @Override
    protected int getMaxFlowDistance(WorldView world) {
        return 4;
    }

    @Override
    public Item getBucketItem() {
        return null;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return CubliminalBlocks.CONTAMINATED_WATER_BLOCK.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    public static class Flowing extends ContaminatedWaterFluid {
        public Flowing() {
        }

        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends ContaminatedWaterFluid {
        public Still() {
        }

        public int getLevel(FluidState state) {
            return 8;
        }

        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
