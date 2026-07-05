package net.limit.cubliminal.entity.projectiles;

import net.limit.cubliminal.init.CubliminalEntities;
import net.limit.cubliminal.init.CubliminalItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

public class FiresaltEntity extends ThrownItemEntity {
    public FiresaltEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public FiresaltEntity(World world, double x, double y, double z, ItemStack stack) {
        super(CubliminalEntities.FIRESALT, x, y, z, world, stack);
    }

    public FiresaltEntity( World world, LivingEntity owner, ItemStack stack) {
        super(CubliminalEntities.FIRESALT, owner, world, stack);
    }


    @Override
    protected Item getDefaultItem() {
        return CubliminalItems.FIRESALT;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient()) {
            this.discard();
            this.getWorld()
                    .createExplosion(
                            this,
                            Explosion.createDamageSource(this.getWorld(), this),
                            new ExplosionBehavior() {
                                @Override
                                public float calculateDamage(Explosion explosion, Entity entity, float amount) {
                                    entity.setOnFireFor(5.0f);
                                    return super.calculateDamage(explosion, entity, amount);
                                }
                            },
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            5,
                            true,
                            World.ExplosionSourceType.MOB
                    );
        }
    }
}
