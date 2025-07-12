package net.limit.cubliminal.block.fluids;

import net.limit.cubliminal.init.CubliminalEffects;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;

public class BlackSludgeFluidBlock extends CustomFluidBlock {
    public BlackSludgeFluidBlock(FlowableFluid fluid, AbstractBlock.Settings settings, CustomFluidBlock.Settings fluidSettings) {
        super(fluid, settings, fluidSettings);
    }

    @Override
    protected StatusEffectInstance[] applyEffectsToEntities() {
        super.applyEffectsToEntities();

        return new StatusEffectInstance[]{
                new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(CubliminalEffects.PARANOIA), 1800)
        };
    }
}
