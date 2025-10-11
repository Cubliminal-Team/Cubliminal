package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.block.custom.template.BlackoutListener;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class CrateAirBlock extends AirBlock implements BlackoutListener {

    public CrateAirBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void blackoutUpdate(BlockState state, ServerWorld world, BlockPos pos, boolean lightsOff, Random random) {
        if (!lightsOff && random.nextFloat() < 0.2) {
            world.setBlockState(pos, CubliminalBlocks.WOODEN_CRATE.getDefaultState());
        }
    }
}
