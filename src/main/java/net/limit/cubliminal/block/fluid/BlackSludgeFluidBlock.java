package net.limit.cubliminal.block.fluid;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FlowableFluid;

public class BlackSludgeFluidBlock extends CustomFluidBlock {

    public BlackSludgeFluidBlock(FlowableFluid fluid, AbstractBlock.Settings settings, CustomFluidBlock.Settings fluidSettings) {
        super(fluid, settings, fluidSettings);
    }

    @Override
    protected StatusEffectInstance[] applyEffectsToEntities() {
        super.applyEffectsToEntities();
        // This applies the slowness effect to entities.
        return new StatusEffectInstance[]{
                new StatusEffectInstance(StatusEffects.SLOWNESS, 1800, 1, true, false, true)
        };
    }
}
