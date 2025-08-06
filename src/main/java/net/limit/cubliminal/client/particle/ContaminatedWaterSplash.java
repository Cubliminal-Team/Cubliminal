package net.limit.cubliminal.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.client.util.ParticleColorManagement;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class ContaminatedWaterSplash {
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            WaterSplashParticle particle = (WaterSplashParticle) new WaterSplashParticle.SplashFactory(this.spriteProvider)
                    .createParticle(type, world, x, y, z, velocityX, velocityY, velocityZ);
            float[] color = ParticleColorManagement.chooseRandomContaminatedWaterColors();
            particle.setColor(color[0], color[1], color[2]);
            return particle;
        }
    }
}
