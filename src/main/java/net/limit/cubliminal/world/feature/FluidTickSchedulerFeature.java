package net.limit.cubliminal.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class FluidTickSchedulerFeature extends Feature<FluidTickSchedulerFeatureConfig> {

    public FluidTickSchedulerFeature(Codec<FluidTickSchedulerFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<FluidTickSchedulerFeatureConfig> context) {
        FluidTickSchedulerFeatureConfig config = context.getConfig();
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        BlockState state = config.fluidState().getBlockState();

        if (state.canPlaceAt(world, origin)) {
            world.setBlockState(origin, state, Block.NOTIFY_LISTENERS);
            world.scheduleFluidTick(origin, config.fluidState().getFluid(), 0);
            return true;
        }

        return false;
    }
}
