package net.limit.cubliminal.block.fluid;

import net.limit.cubliminal.init.CubliminalEffects;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AlmondWaterFluidBlock extends CustomFluidBlock {

    public AlmondWaterFluidBlock(FlowableFluid fluid, AbstractBlock.Settings settings, Settings fluidSettings) {
        super(fluid, settings, fluidSettings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        // Checks to see if world isn't a client and checks to see if the entity is a living entity
        if (!world.isClient() && entity instanceof LivingEntity living) {
            // Removes the Paranoia effect from the entity.
            living.removeStatusEffect(CubliminalEffects.PARANOIA);
        }
    }
}
