package net.limit.cubliminal.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.apache.http.annotation.Obsolete;

public class CubliminalParticleTypes {
    public static SimpleParticleType CONTAMINATED_WATER_SPLASH;

    public static SimpleParticleType ALMOND_WATER_BUBBLE;
    public static SimpleParticleType CONTAMINATED_WATER_BUBBLE;

    public static SimpleParticleType DRIPPING_ALMOND_WATER;
    public static SimpleParticleType FALLING_ALMOND_WATER;
    public static SimpleParticleType LANDING_ALMOND_WATER;

    public static SimpleParticleType DRIPPING_BLACK_SLUDGE;
    public static SimpleParticleType FALLING_BLACK_SLUDGE;
    public static SimpleParticleType LANDING_BLACK_SLUDGE;

    public static SimpleParticleType DRIPPING_CONTAMINATED_WATER;
    public static SimpleParticleType FALLING_CONTAMINATED_WATER;
    public static SimpleParticleType LANDING_CONTAMINATED_WATER;
    @Obsolete
    public static SimpleParticleType SPLASH;

    private static SimpleParticleType register(String name){
        return Registry.register(Registries.PARTICLE_TYPE, Cubliminal.id(name), FabricParticleTypes.simple());
    }

    public static void init() {
        CONTAMINATED_WATER_SPLASH = register("contaminated_water_splash");
        ALMOND_WATER_BUBBLE = register("almond_water_bubble");
        CONTAMINATED_WATER_BUBBLE = register("contaminated_water_bubble");

        DRIPPING_ALMOND_WATER = register("dripping_almond_water");
        FALLING_ALMOND_WATER = register("falling_almond_water");
        LANDING_ALMOND_WATER = register("landing_almond_water");

        DRIPPING_BLACK_SLUDGE = register("dripping_black_sludge");
        FALLING_BLACK_SLUDGE = register("falling_black_sludge");
        LANDING_BLACK_SLUDGE = register("landing_black_sludge");

        DRIPPING_CONTAMINATED_WATER = register("dripping_contaminated_water");
        FALLING_CONTAMINATED_WATER = register("falling_contaminated_water");
        LANDING_CONTAMINATED_WATER = register("landing_contaminated_water");

        SPLASH = register("splash");
    }
}
