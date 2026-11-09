package net.limit.cubliminal.mixin;

import net.limit.cubliminal.block.custom.template.VariableBlastResistance;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ExplosionBehavior.class)
public class ExplosionBehaviorMixin {
    @Inject(method = "getBlastResistance", at = @At("HEAD"), cancellable = true)
    private void cubliminal$getBlastResistance(Explosion explosion, BlockView world, BlockPos pos,
                                               BlockState blockState, FluidState fluidState,
                                               CallbackInfoReturnable<Optional<Float>> cir) {
        if (blockState.getBlock() instanceof VariableBlastResistance variableBlastResistance) {
            cir.setReturnValue(variableBlastResistance.getBlastResistance(
                    explosion,
                    world,
                    pos,
                    blockState,
                    fluidState
            ));
            cir.cancel();
        }
    }
}
