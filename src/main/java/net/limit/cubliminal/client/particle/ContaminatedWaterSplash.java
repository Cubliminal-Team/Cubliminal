package net.limit.cubliminal.client.particle;

import net.limit.cubliminal.client.particle.util.ParticleColorManagement;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class ContaminatedWaterSplash {
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            WaterSplashParticle particle = new WaterSplashParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            float[] color = ParticleColorManagement.chooseRandomContaminatedWaterColors();
            particle.setColor(color[0], color[1], color[2]);
            particle.setSprite(spriteProvider);
            return particle;
        }
    }
}
