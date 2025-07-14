package net.limit.cubliminal.mixin;

import net.limit.cubliminal.block.fluids.CustomFluidBlock;
import net.limit.cubliminal.fluid.BackroomsFlowableFluid;
import net.limit.cubliminal.init.CubliminalFluidTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow protected boolean jumping;
    private FluidState fluidState;

    @Unique
    private boolean isSubmergedAtFeet(TagKey<Fluid> fluid) {
        Entity entity = (Entity)(Object) this;
        FluidState fluidState = entity.getWorld().getFluidState(entity.getBlockPos());
        if (fluidState.isIn(fluid)){
            this.fluidState = fluidState;
            return true;
        }
        return false;
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3d movementInput, CallbackInfo ci){
        if (isSubmergedAtFeet(CubliminalFluidTags.CUSTOM_FLUIDS)){
            // Checks to see if the fluid uses Project Backrooms custom implementations
            if (this.fluidState.getFluid() instanceof BackroomsFlowableFluid backroomsFluid){
                Entity entity = (Entity)(Object) this;
                // Gets the fluid block.
                CustomFluidBlock fluidBlock = backroomsFluid.getFluidBlock();
                // Gets the fluid settings from the fluid block.
                CustomFluidBlock.Settings settings = fluidBlock.getFluidSettings();
                // Checks to see if the fluid doesn't plan to use the default physics.
                if (!settings.usesDefaultPhysics()){
                    // The gravity.
                    double gravity = 0.08;

                    entity.updateVelocity(settings.getSpeed(), movementInput); // Speed
                    entity.move(MovementType.SELF, entity.getVelocity());

                    Vec3d drag = entity.getVelocity().multiply(settings.getDrag().x, settings.getDrag().y, settings.getDrag().z);
                    if (!entity.hasNoGravity()) {
                        drag = drag.add(0, -gravity / 4.0, 0);
                    }

                    if (jumping) {
                        drag = drag.add(0.0, 0.04, 0.0); // swim up slowly
                    }

                    entity.setVelocity(drag);
                    entity.fallDistance = 0.0F;
                    ci.cancel();
                }
            }

        }
    }
}