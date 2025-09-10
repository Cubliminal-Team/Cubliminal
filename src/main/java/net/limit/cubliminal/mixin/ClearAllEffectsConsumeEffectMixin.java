package net.limit.cubliminal.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.limit.cubliminal.init.CubliminalEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.consume.ClearAllEffectsConsumeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClearAllEffectsConsumeEffect.class)
public class ClearAllEffectsConsumeEffectMixin {

    @WrapOperation(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z"))
    private boolean onClearStatusEffects(LivingEntity instance, Operation<Boolean> original) {
        if (instance.hasStatusEffect(CubliminalEffects.PARANOIA)) {
            StatusEffectInstance effect = instance.getStatusEffect(CubliminalEffects.PARANOIA);
            boolean bl = instance.clearStatusEffects();
            instance.addStatusEffect(effect);
            return bl;
        }

        return original.call(instance);
    }
}
