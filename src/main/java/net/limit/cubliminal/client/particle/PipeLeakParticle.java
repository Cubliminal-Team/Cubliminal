package net.limit.cubliminal.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.client.util.ParticleColorManagement;
import net.limit.cubliminal.init.CubliminalParticleTypes;
import net.limit.cubliminal.mixin.client.ParticleAccessor;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class PipeLeakParticle extends BlockLeakParticle {

    public static float[] chosenCWColors;
    private static final SpriteProvider NULL_PROVIDER = new SpriteProvider() {
        @Override
        public Sprite getSprite(int age, int maxAge) {
            return null;
        }

        @Override
        public Sprite getSprite(Random random) {
            return null;
        }
    };

    protected PipeLeakParticle(ClientWorld world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z, fluid);
        ((ParticleAccessor) this).setGravityStrength(0.06f);
    }

    public static SpriteBillboardParticle createDrippingAlmondWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        BlockLeakParticle particle = new BlockLeakParticle.Dripping(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.FALLING_ALMOND_WATER);
        particle.setColor(ParticleColorManagement.ALMOND_WATER[0], ParticleColorManagement.ALMOND_WATER[1], ParticleColorManagement.ALMOND_WATER[2]);
        return particle;
    }

    public static SpriteBillboardParticle createFallingAlmondWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        BlockLeakParticle particle = new BlockLeakParticle.ContinuousFalling(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.LANDING_ALMOND_WATER);
        particle.setColor(ParticleColorManagement.ALMOND_WATER[0], ParticleColorManagement.ALMOND_WATER[1], ParticleColorManagement.ALMOND_WATER[2]);
        return particle;
    }

    public static SpriteBillboardParticle createLandingAlmondWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        SpriteBillboardParticle particle = (WaterSplashParticle) new WaterSplashParticle.SplashFactory(NULL_PROVIDER)
                .createParticle(type, world, x, y, z, velocityX, velocityY, velocityZ);
        particle.setColor(ParticleColorManagement.ALMOND_WATER[0], ParticleColorManagement.ALMOND_WATER[1], ParticleColorManagement.ALMOND_WATER[2]);
        return particle;
    }

    public static SpriteBillboardParticle createDrippingBlackSludge(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        BlockLeakParticle dripping = new BlockLeakParticle.Dripping(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.FALLING_BLACK_SLUDGE);
        ParticleAccessor accessor = ((ParticleAccessor) dripping);
        accessor.setGravityStrength(accessor.getGravityStrength() * 0.01f);
        accessor.setMaxAge(100);
        dripping.setColor(ParticleColorManagement.BLACK_SLUDGE[0], ParticleColorManagement.BLACK_SLUDGE[1], ParticleColorManagement.BLACK_SLUDGE[2]);
        return dripping;
    }

    public static SpriteBillboardParticle createFallingBlackSludge(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        BlockLeakParticle blockLeakParticle = new BlockLeakParticle.FallingHoney(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.LANDING_BLACK_SLUDGE);
        ParticleAccessor accessor = ((ParticleAccessor) blockLeakParticle);
        accessor.setGravityStrength(accessor.getGravityStrength() * 0.01f);
        blockLeakParticle.setColor(ParticleColorManagement.BLACK_SLUDGE[0], ParticleColorManagement.BLACK_SLUDGE[1], ParticleColorManagement.BLACK_SLUDGE[2]);
        return blockLeakParticle;
    }

    public static SpriteBillboardParticle createLandingBlackSludge(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        BlockLeakParticle blockLeakParticle = new BlockLeakParticle.Landing(world, x, y, z, Fluids.EMPTY);
        ((ParticleAccessor) blockLeakParticle).setMaxAge((int) (128.0 / (Math.random() * 0.8 + 0.2)));
        blockLeakParticle.setColor(ParticleColorManagement.BLACK_SLUDGE[0], ParticleColorManagement.BLACK_SLUDGE[1], ParticleColorManagement.BLACK_SLUDGE[2]);
        return blockLeakParticle;
    }

    public static SpriteBillboardParticle createDrippingContaminatedWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        BlockLeakParticle particle = new BlockLeakParticle.Dripping(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.FALLING_CONTAMINATED_WATER);
        chosenCWColors = ParticleColorManagement.chooseRandomContaminatedWaterColors();
        particle.setColor(chosenCWColors[0], chosenCWColors[1], chosenCWColors[2]);
        return particle;
    }

    public static SpriteBillboardParticle createFallingContaminatedWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        BlockLeakParticle particle = new BlockLeakParticle.ContinuousFalling(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.LANDING_CONTAMINATED_WATER);
        particle.setColor(chosenCWColors[0], chosenCWColors[1], chosenCWColors[2]);
        return particle;
    }

    public static SpriteBillboardParticle createLandingContaminatedWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        SpriteBillboardParticle particle = (WaterSplashParticle) new WaterSplashParticle.SplashFactory(NULL_PROVIDER)
                .createParticle(type, world, x, y, z, velocityX, velocityY, velocityZ);
        particle.setColor(chosenCWColors[0], chosenCWColors[1], chosenCWColors[2]);
        return particle;
    }
}
