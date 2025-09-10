package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.effect.ParanoiaEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class CubliminalEffects implements Initer {

    public static RegistryEntry<StatusEffect> PARANOIA = register("paranoia", new ParanoiaEffect(StatusEffectCategory.HARMFUL, 24828));

    public static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Cubliminal.id(id), statusEffect);
    }

}
