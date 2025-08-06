package net.limit.cubliminal.block.custom.template;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface BlackoutListener {
    void blackoutUpdate(BlockState state, ServerWorld world, BlockPos pos, boolean lightsOff, Random random);
}
