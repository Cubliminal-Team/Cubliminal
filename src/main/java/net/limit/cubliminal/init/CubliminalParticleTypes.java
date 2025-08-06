package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.apache.http.annotation.Obsolete;

public class CubliminalParticleTypes {
    public static SimpleParticleType CONTAMINATED_WATER_SPLASH = register("contaminated_water_splash");

    public static SimpleParticleType ALMOND_WATER_BUBBLE = register("almond_water_bubble");
    public static SimpleParticleType CONTAMINATED_WATER_BUBBLE = register("contaminated_water_bubble");

    public static SimpleParticleType DRIPPING_ALMOND_WATER = register("dripping_almond_water");
    public static SimpleParticleType FALLING_ALMOND_WATER = register("falling_almond_water");
    public static SimpleParticleType LANDING_ALMOND_WATER = register("landing_almond_water");

    public static SimpleParticleType DRIPPING_BLACK_SLUDGE = register("dripping_black_sludge");
    public static SimpleParticleType FALLING_BLACK_SLUDGE = register("falling_black_sludge");
    public static SimpleParticleType LANDING_BLACK_SLUDGE = register("landing_black_sludge");

    public static SimpleParticleType DRIPPING_CONTAMINATED_WATER = register("dripping_contaminated_water");
    public static SimpleParticleType FALLING_CONTAMINATED_WATER = register("falling_contaminated_water");
    public static SimpleParticleType LANDING_CONTAMINATED_WATER = register("landing_contaminated_water");

    @Obsolete
    public static SimpleParticleType SPLASH = register("splash");

    private static SimpleParticleType register(String name){
        return Registry.register(Registries.PARTICLE_TYPE, Cubliminal.id(name), FabricParticleTypes.simple());
    }

}
