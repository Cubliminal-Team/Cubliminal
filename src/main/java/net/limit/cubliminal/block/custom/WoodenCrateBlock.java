package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.block.custom.template.BlackoutListener;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class WoodenCrateBlock extends Block implements BlackoutListener {

    public WoodenCrateBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        for (Direction direction : Direction.values()) {
            if (world.getLightLevel(pos.offset(direction)) > 11) {
                return;
            }
        }
        world.breakBlock(pos, false);
        world.setBlockState(pos, CubliminalBlocks.CRATE_AIR.getDefaultState());
    }

    @Override
    public void blackoutUpdate(BlockState state, ServerWorld world, BlockPos pos, boolean lightsOff, Random random) {
        if (lightsOff && random.nextFloat() < 0.5) {
            world.scheduleBlockTick(pos, state.getBlock(), 1);
        }
    }
}
