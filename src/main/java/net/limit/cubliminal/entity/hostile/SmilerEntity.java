package net.limit.cubliminal.entity.hostile;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SmilerEntity extends HostileEntity {

    public SmilerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes(){
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH,50)
                .add(EntityAttributes.MAX_ABSORPTION, 25)
                .add(EntityAttributes.ATTACK_DAMAGE, 10)
                .add(EntityAttributes.FOLLOW_RANGE, 25f)
                .add(EntityAttributes.MOVEMENT_SPEED,0.6f);
    }

    @Override
    protected void initGoals() {
        List<Block> targetBlocks = new ArrayList<>(List.of(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.CAMPFIRE, Blocks.LANTERN, Blocks.GLOWSTONE, Blocks.SEA_LANTERN, Blocks.JACK_O_LANTERN, Blocks.REDSTONE_TORCH));
        GoalSelector goal = this.goalSelector;
        goal.add(2, new MeleeAttackGoal(this, 0.6f, true));
        for (Block targetBlock : targetBlocks) {
            goal.add(4, new DestroyLightSourceGoal(targetBlock, this, 0.7f, 45, 10));
        }
        goal.add(6, new WanderAroundFarGoal(this,0.3f));
        goal.add(8, new WanderAroundGoal(this,0.6f));
        goal.add(8, new BreakDoorGoal(this, difficulty -> true));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, VillagerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        boolean bl = this.isAffectedByDaylight();
        if (bl){
            this.setHealth(0f);
        } else {
            this.heal(2f);
        }
    }

    static class DestroyLightSourceGoal extends StepAndDestroyBlockGoal {
        private final int innerRange;
        public DestroyLightSourceGoal(Block targetBlock, PathAwareEntity mob, double speed, int maxYDifference, int i) {
            super(targetBlock, mob, speed, maxYDifference);
            this.innerRange = i;
        }

        @Override
        public void tick() {
            super.tick();
            if (isInInnerRange()){
                this.mob.getWorld().breakBlock(this.targetPos, false, this.mob);
            }
        }

        private boolean isInInnerRange() {
            return this.mob.getBlockPos().getSquaredDistance(this.targetPos) <= (this.innerRange * this.innerRange);
        }
    }
}
