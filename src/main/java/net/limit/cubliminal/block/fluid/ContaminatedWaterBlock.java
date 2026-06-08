package net.limit.cubliminal.block.fluid;

import net.limit.cubliminal.block.custom.template.BlockVariantHolder;
import net.limit.cubliminal.init.CubliminalFluids;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;

public class ContaminatedWaterBlock extends CustomFluidBlock implements BlockVariantHolder {
    public ContaminatedWaterBlock(FlowableFluid fluid, AbstractBlock.Settings settings, CustomFluidBlock.Settings fluidSettings) {
        super(fluid, settings, fluidSettings);
    }

    @Override
    protected StatusEffectInstance[] applyEffectsToEntities() {
        return new StatusEffectInstance[]{
                new StatusEffectInstance(StatusEffects.NAUSEA, 600, 1),
                new StatusEffectInstance(StatusEffects.POISON, 300, 1)
        };
    }

    @Override
    public void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos, Random random) {
        /*
        if (random.nextFloat() > 0.02) {
            region.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
        } else {
            region.scheduleFluidTick(pos, CubliminalFluids.CONTAMINATED_WATER, 0);
        }
         */
        region.scheduleFluidTick(pos, CubliminalFluids.CONTAMINATED_WATER, 0);
    }
}
