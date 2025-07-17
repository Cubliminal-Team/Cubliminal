package net.limit.cubliminal.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.client.particle.util.ParticleColorManagement;
import net.limit.cubliminal.particle.CubliminalParticleTypes;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class CubliminalPipeLeakParticle extends BlockLeakParticle {
    public static float[] chosenCWColors;
    protected CubliminalPipeLeakParticle(ClientWorld world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z, fluid);
        this.gravityStrength = 0.06f;
    }

    public static SpriteBillboardParticle createDrippingAlmondWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        BlockLeakParticle particle = new Dripping(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.FALLING_ALMOND_WATER);
        particle.setColor(ParticleColorManagement.ALMOND_WATER[0], ParticleColorManagement.ALMOND_WATER[1], ParticleColorManagement.ALMOND_WATER[2]);
        return particle;
    }

    public static SpriteBillboardParticle createFallingAlmondWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        BlockLeakParticle particle = new ContinuousFalling(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.LANDING_ALMOND_WATER);
        particle.setColor(ParticleColorManagement.ALMOND_WATER[0], ParticleColorManagement.ALMOND_WATER[1], ParticleColorManagement.ALMOND_WATER[2]);
        return particle;
    }

    public static SpriteBillboardParticle createLandingAlmondWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        SpriteBillboardParticle particle = new WaterSplashParticle(world, x, y, z, velocityX, velocityY, velocityZ);
        particle.setColor(ParticleColorManagement.ALMOND_WATER[0], ParticleColorManagement.ALMOND_WATER[1], ParticleColorManagement.ALMOND_WATER[2]);
        return particle;
    }

    public static SpriteBillboardParticle createDrippingBlackSludge(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        Dripping dripping = new Dripping(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.FALLING_BLACK_SLUDGE);
        dripping.gravityStrength *= 0.01f;
        dripping.maxAge = 100;
        dripping.setColor(ParticleColorManagement.BLACK_SLUDGE[0], ParticleColorManagement.BLACK_SLUDGE[1], ParticleColorManagement.BLACK_SLUDGE[2]);
        return dripping;
    }

    public static SpriteBillboardParticle createFallingBlackSludge(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        BlockLeakParticle blockLeakParticle = new FallingHoney(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.LANDING_BLACK_SLUDGE);
        blockLeakParticle.gravityStrength *= 0.01f;
        blockLeakParticle.setColor(ParticleColorManagement.BLACK_SLUDGE[0], ParticleColorManagement.BLACK_SLUDGE[1], ParticleColorManagement.BLACK_SLUDGE[2]);
        return blockLeakParticle;
    }

    public static SpriteBillboardParticle createLandingBlackSludge(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        BlockLeakParticle blockLeakParticle = new Landing(world, x, y, z, Fluids.EMPTY);
        blockLeakParticle.maxAge = (int)(128.0 / (Math.random() * 0.8 + 0.2));
        blockLeakParticle.setColor(ParticleColorManagement.BLACK_SLUDGE[0], ParticleColorManagement.BLACK_SLUDGE[1], ParticleColorManagement.BLACK_SLUDGE[2]);
        return blockLeakParticle;
    }

    public static SpriteBillboardParticle createDrippingContaminatedWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        BlockLeakParticle particle = new Dripping(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.FALLING_CONTAMINATED_WATER);
        chosenCWColors = ParticleColorManagement.chooseRandomContaminatedWaterColors();
        particle.setColor(chosenCWColors[0], chosenCWColors[1], chosenCWColors[2]);
        return particle;
    }

    public static SpriteBillboardParticle createFallingContaminatedWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        BlockLeakParticle particle = new ContinuousFalling(world, x, y, z, Fluids.EMPTY, CubliminalParticleTypes.LANDING_CONTAMINATED_WATER);
        particle.setColor(chosenCWColors[0], chosenCWColors[1], chosenCWColors[2]);
        return particle;
    }

    public static SpriteBillboardParticle createLandingContaminatedWater(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ){
        SpriteBillboardParticle particle = new WaterSplashParticle(world, x, y, z, velocityX, velocityY, velocityZ);
        particle.setColor(chosenCWColors[0], chosenCWColors[1], chosenCWColors[2]);
        return particle;
    }
}
