package net.limit.cubliminal.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.limit.cubliminal.init.CubliminalParticleTypes;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CubliminalParticleManager {
    public static void init() {
        registerSplashFactory(CubliminalParticleTypes.SPLASH, WaterSplashParticle.SplashFactory::new);
        registerFactory(CubliminalParticleTypes.CONTAMINATED_WATER_SPLASH, ContaminatedWaterSplash.Factory::new);
        registerFactory(CubliminalParticleTypes.ALMOND_WATER_BUBBLE, Bubble.AlmondWaterBubbleFactory::new);
        registerFactory(CubliminalParticleTypes.CONTAMINATED_WATER_BUBBLE, Bubble.ContaminatedWaterBubbleFactory::new);

        registerPipeLeakFactory(CubliminalParticleTypes.DRIPPING_ALMOND_WATER, PipeLeakParticle::createDrippingAlmondWater);
        registerPipeLeakFactory(CubliminalParticleTypes.FALLING_ALMOND_WATER, PipeLeakParticle::createFallingAlmondWater);
        registerPipeLeakFactory(CubliminalParticleTypes.LANDING_ALMOND_WATER, PipeLeakParticle::createLandingAlmondWater);

        registerPipeLeakFactory(CubliminalParticleTypes.DRIPPING_BLACK_SLUDGE, PipeLeakParticle::createDrippingBlackSludge);
        registerPipeLeakFactory(CubliminalParticleTypes.FALLING_BLACK_SLUDGE, PipeLeakParticle::createFallingBlackSludge);
        registerPipeLeakFactory(CubliminalParticleTypes.LANDING_BLACK_SLUDGE, PipeLeakParticle::createLandingBlackSludge);

        registerPipeLeakFactory(CubliminalParticleTypes.DRIPPING_CONTAMINATED_WATER, PipeLeakParticle::createDrippingContaminatedWater);
        registerPipeLeakFactory(CubliminalParticleTypes.FALLING_CONTAMINATED_WATER, PipeLeakParticle::createFallingContaminatedWater);
        registerPipeLeakFactory(CubliminalParticleTypes.LANDING_CONTAMINATED_WATER, PipeLeakParticle::createLandingContaminatedWater);

        registerFactory(CubliminalParticleTypes.MOLD_SPORE, MoldSporeParticle.Factory::new);
    }

    public static <T extends ParticleEffect> void registerFactory(
            ParticleType<T> type,
            Function<SpriteProvider, ParticleFactory<T>> factoryProvider
    ) {
        ParticleFactoryRegistry.getInstance().register(type, factoryProvider::apply);
    }

    public static <T extends ParticleEffect> void registerSplashFactory(ParticleType<T> type, ParticleFactoryRegistry.PendingParticleFactory<T> factory){
        ParticleFactoryRegistry.getInstance().register(type, factory);
    }

    public static <T extends ParticleEffect> void registerPipeLeakFactory(ParticleType<T> type, ParticleFactory.BlockLeakParticleFactory<T> factory) {
        ParticleFactoryRegistry.getInstance().register(type, spriteProvider ->
                (parameters, world, x, y, z, vx, vy, vz) -> {
                    SpriteBillboardParticle particle = factory.createParticle(parameters, world, x, y, z, vx, vy, vz);
                    if (particle != null){
                        particle.setSprite(spriteProvider);
                    }
                    return particle;
                });
    }

}
