package net.limit.cubliminal.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.particle.CubliminalParticleTypes;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CubliminalParticleManager implements ResourceReloader, Initer {
    @Override
    public void init() {
        registerSplashFactory(CubliminalParticleTypes.SPLASH, WaterSplashParticle.SplashFactory::new);
        registerFactory(CubliminalParticleTypes.CONTAMINATED_WATER_SPLASH, ContaminatedWaterSplash.Factory::new);
        registerFactory(CubliminalParticleTypes.ALMOND_WATER_BUBBLE, Bubble.AlmondWaterBubbleFactory::new);
        registerFactory(CubliminalParticleTypes.CONTAMINATED_WATER_BUBBLE, Bubble.ContaminatedWaterBubbleFactory::new);

        registerPipeLeakFactory(CubliminalParticleTypes.DRIPPING_ALMOND_WATER, CubliminalPipeLeakParticle::createDrippingAlmondWater);
        registerPipeLeakFactory(CubliminalParticleTypes.FALLING_ALMOND_WATER, CubliminalPipeLeakParticle::createFallingAlmondWater);
        registerPipeLeakFactory(CubliminalParticleTypes.LANDING_ALMOND_WATER, CubliminalPipeLeakParticle::createLandingAlmondWater);

        registerPipeLeakFactory(CubliminalParticleTypes.DRIPPING_BLACK_SLUDGE, CubliminalPipeLeakParticle::createDrippingBlackSludge);
        registerPipeLeakFactory(CubliminalParticleTypes.FALLING_BLACK_SLUDGE, CubliminalPipeLeakParticle::createFallingBlackSludge);
        registerPipeLeakFactory(CubliminalParticleTypes.LANDING_BLACK_SLUDGE, CubliminalPipeLeakParticle::createLandingBlackSludge);

        registerPipeLeakFactory(CubliminalParticleTypes.DRIPPING_CONTAMINATED_WATER, CubliminalPipeLeakParticle::createDrippingContaminatedWater);
        registerPipeLeakFactory(CubliminalParticleTypes.FALLING_CONTAMINATED_WATER, CubliminalPipeLeakParticle::createFallingContaminatedWater);
        registerPipeLeakFactory(CubliminalParticleTypes.LANDING_CONTAMINATED_WATER, CubliminalPipeLeakParticle::createLandingContaminatedWater);
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

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        return null;
    }
}
