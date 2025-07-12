package net.limit.cubliminal.block.fluids;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.fluid.FlowableFluid;

@FunctionalInterface
public interface FluidBlockFactory {
    Block create(FlowableFluid fluid, AbstractBlock.Settings settings, CustomFluidBlock.Settings fluidSettings);
}
