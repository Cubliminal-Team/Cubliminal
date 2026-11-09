package net.limit.cubliminal.block.custom.template;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;

import java.util.Optional;

public interface VariableBlastResistance {
    Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos,
                                       BlockState blockState, FluidState fluidState);
}
