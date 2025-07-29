package net.limit.cubliminal.mixin;

import net.limit.cubliminal.block.fluid.CustomFluidBlock;
import net.limit.cubliminal.fluid.BackroomsFlowableFluid;
import net.limit.cubliminal.init.CubliminalFluids;
import net.limit.cubliminal.particle.CubliminalParticleTypes;
import net.limit.cubliminal.util.DebugLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(Entity.class)
public abstract class FluidSplashMixin {
    @Shadow public abstract World getWorld();

    @ModifyArg(
            method = "onSwimmingStart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"
            ),
            index = 0
    )
    private ParticleEffect redirectSplashParticle(ParticleEffect original) {
        Entity self = (Entity) (Object) this;
        Fluid fluid = getTouchingFluid(self, f -> f instanceof BackroomsFlowableFluid);

        if (fluid instanceof BackroomsFlowableFluid flowableFluid){
            CustomFluidBlock.Settings settings = flowableFluid.getFluidBlock().getFluidSettings();
            CustomFluidBlock.FluidSplashParticleManager splashParticle = settings.getSplashParticles();
            if (original == ParticleTypes.SPLASH && splashParticle.getSplashParticle() != null){
                return splashParticle.getSplashParticle();
            } else if (original == ParticleTypes.BUBBLE && splashParticle.getBubbleParticle() != null){
                return splashParticle.getBubbleParticle();
            }
        }
        return original;
    }

    @Unique
    private Fluid getTouchingFluid(Entity entity, Predicate<Fluid> filter) {
        Box box = entity.getBoundingBox().contract(0.001); // Slightly smaller to avoid edge noise
        World world = entity.getWorld();

        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.floor(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.floor(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.floor(box.maxZ);

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    FluidState state = world.getFluidState(pos);
                    Fluid fluid = state.getFluid();
                    if (!state.isEmpty() && filter.test(fluid)) {
                        return fluid;
                    }
                }
            }
        }

        return null;
    }

}
