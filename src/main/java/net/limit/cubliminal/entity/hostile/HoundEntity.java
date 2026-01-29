package net.limit.cubliminal.entity.hostile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HoundEntity extends HostileEntity {
    public HoundEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes(){
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40)
                .add(EntityAttributes.MOVEMENT_SPEED, 1.0f)
                .add(EntityAttributes.ATTACK_DAMAGE, 8)
                .add(EntityAttributes.FOLLOW_RANGE, 50f);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        GoalSelector target = this.targetSelector;
        GoalSelector goal = this.goalSelector;

        target.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        target.add(2, new ActiveTargetGoal<>(this, VillagerEntity.class, true));

        goal.add(0, new getIntimidated(this, PlayerEntity.class, 1.0f, 0f, 1.0f));
        goal.add(2, new MeleeAttackGoal(this, 1.0f, true));
        goal.add(4, new WanderAroundFarGoal(this, 1.0f));
        goal.add(5, new WanderAroundGoal(this, 0.5f));
    }

    boolean isPlayerStaring(PlayerEntity entity){
        return this.isEntityLookingAtMe(entity, 0.025, true, false, null, this::getEyeY);
    }

    static class getIntimidated extends FleeEntityGoal {
        private final HoundEntity hound;

        public getIntimidated(HoundEntity mob, Class fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(mob, fleeFromType, distance, slowSpeed, fastSpeed);
            this.hound = mob;
        }

        @Override
        public boolean canStart() {
            @Nullable LivingEntity target = this.hound.getTarget();

            if (target instanceof PlayerEntity) {
                if (hound.isPlayerStaring((PlayerEntity) target)) {
                    return super.canStart();
                }
            }
            return false;
        }
    }
}
