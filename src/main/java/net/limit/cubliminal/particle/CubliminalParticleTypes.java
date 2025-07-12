package net.limit.cubliminal.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.http.annotation.Obsolete;

public class CubliminalParticleTypes implements Initer {
    public static final SimpleParticleType CONTAMINATED_WATER_SPLASH = register("contaminated_water_splash", FabricParticleTypes.simple());

    public static final SimpleParticleType ALMOND_WATER_BUBBLE = register("almond_water_bubble", FabricParticleTypes.simple());
    public static final SimpleParticleType CONTAMINATED_WATER_BUBBLE = register("contaminated_water_bubble", FabricParticleTypes.simple());

    public static final SimpleParticleType DRIPPING_ALMOND_WATER = register("dripping_almond_water", FabricParticleTypes.simple());
    public static final SimpleParticleType FALLING_ALMOND_WATER = register("falling_almond_water", FabricParticleTypes.simple());
    public static final SimpleParticleType LANDING_ALMOND_WATER = register("landing_almond_water", FabricParticleTypes.simple());

    public static final SimpleParticleType DRIPPING_BLACK_SLUDGE = register("dripping_black_sludge", FabricParticleTypes.simple());
    public static final SimpleParticleType FALLING_BLACK_SLUDGE = register("falling_black_sludge", FabricParticleTypes.simple());
    public static final SimpleParticleType LANDING_BLACK_SLUDGE = register("landing_black_sludge", FabricParticleTypes.simple());

    public static final SimpleParticleType DRIPPING_CONTAMINATED_WATER = register("dripping_contaminated_water", FabricParticleTypes.simple());
    public static final SimpleParticleType FALLING_CONTAMINATED_WATER = register("falling_contaminated_water", FabricParticleTypes.simple());
    public static final SimpleParticleType LANDING_CONTAMINATED_WATER = register("landing_contaminated_water", FabricParticleTypes.simple());
    @Obsolete
    public static final SimpleParticleType SPLASH = register("splash", FabricParticleTypes.simple());

    private static SimpleParticleType register(String name, SimpleParticleType type){
        return Registry.register(Registries.PARTICLE_TYPE, Cubliminal.id(name), type);
    }

    @Override
    public void init() {
        Initer.super.init();
    }
}
