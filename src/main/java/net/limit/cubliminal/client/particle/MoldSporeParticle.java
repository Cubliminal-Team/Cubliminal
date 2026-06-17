package net.limit.cubliminal.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MoldSporeParticle extends SpriteBillboardParticle {

    public MoldSporeParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0, 0, 0);
        this.setSpriteForAge(spriteProvider);
        this.velocityY *= 0.8f;
        this.scale = this.scale * (this.random.nextFloat() * 0.4f + 0.6f);
        this.maxAge = (int) (16.0 / (Math.random() * 0.8 + 0.2));
        this.velocityMultiplier = 0.96f;
        this.gravityStrength = 0.0005f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    private float ageProportion(float age) {
        return MathHelper.clamp(age / this.maxAge, 0.0f, 1.0f);
    }

    private static float calculateBrightness(float f, float g, float h) {
        if (f >= 1.0f - g) {
            return (1.0f - f) / g;
        } else {
            return f <= h ? f / h : 1.0f;
        }
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge || !this.world.getBlockState(BlockPos.ofFloored(this.x, this.y, this.z)).isAir()) {
            this.markDead();
        } else {
            this.setAlpha(calculateBrightness(this.ageProportion(this.age), 0.3f, 0.5f));

            if (this.random.nextDouble() > 0.7) {
                this.velocityX += (-0.0015f + 0.003f * this.random.nextFloat());
                this.velocityY += (-0.0015f + 0.003f * this.random.nextFloat());
                this.velocityZ += (-0.0015f + 0.003f * this.random.nextFloat());
            }

            this.velocityY -= this.gravityStrength;

            this.move(this.velocityX, this.velocityY, this.velocityZ);

            this.velocityX *= this.velocityMultiplier;
            this.velocityY *= this.velocityMultiplier;
            this.velocityZ *= this.velocityMultiplier;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            MoldSporeParticle particle = new MoldSporeParticle(world, x, y, z, this.spriteProvider);
            particle.setMaxAge(MathHelper.nextBetween(world.random, 300, 500));
            particle.setColor(0.63f, 0.77f, 0.70f);
            particle.setAlpha(0.0f);
            return particle;
        }
    }
}
