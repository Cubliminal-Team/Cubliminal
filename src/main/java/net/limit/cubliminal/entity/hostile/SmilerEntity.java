package net.limit.cubliminal.entity.hostile;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.util.DebugLogger;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class SmilerEntity extends HostileEntity {
    private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER;
    private final BreakDoorGoal breakDoorsGoal;
    private boolean canBreakDoors;

    public SmilerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
    }

    public static DefaultAttributeContainer.Builder createAttributes(){
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH,50)
                .add(EntityAttributes.MAX_ABSORPTION, 25)
                .add(EntityAttributes.ATTACK_DAMAGE, 10)
                .add(EntityAttributes.FOLLOW_RANGE, 25f)
                .add(EntityAttributes.MOVEMENT_SPEED,0.6f);
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    @Override
    protected void initGoals() {
        List<Block> targetBlocks = new ArrayList<>(List.of(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.CAMPFIRE, Blocks.LANTERN, Blocks.GLOWSTONE, Blocks.SEA_LANTERN, Blocks.JACK_O_LANTERN, Blocks.REDSTONE_TORCH));
        int elementSize = targetBlocks.size();
        int startNumber = 3;
        int totalSize = startNumber + elementSize;

        GoalSelector target = this.targetSelector;
        target.add(1, new RevengeGoal(this));
        target.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        target.add(3, new ActiveTargetGoal<>(this, VillagerEntity.class, true));

        GoalSelector goal = this.goalSelector;
        goal.add(2, new MeleeAttackGoal(this, 0.6f, true));
        for (int i = 0; i < elementSize; i++){
            Block block = targetBlocks.get(i);
            goal.add(startNumber + i, new DestroyLightSourceGoal(block, this, 0.7f, 45, 10));
        }
        goal.add(totalSize + 2, new WanderAroundFarGoal(this,0.3f));
        goal.add(totalSize + 3, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        goal.add(totalSize + 4, new WanderAroundGoal(this,0.6f));
        goal.add(totalSize + 6, new BreakDoorGoal(this, difficulty -> true));
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        setCanBreakDoors(true);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public void setCanBreakDoors(boolean canBreakDoors) {
        if (NavigationConditions.hasMobNavigation(this)) {
            if (this.canBreakDoors != canBreakDoors) {
                this.canBreakDoors = canBreakDoors;
                ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(canBreakDoors);
                if (canBreakDoors) {
                    this.goalSelector.add(1, this.breakDoorsGoal);
                } else {
                    this.goalSelector.remove(this.breakDoorsGoal);
                }
            }
        } else if (this.canBreakDoors) {
            this.goalSelector.remove(this.breakDoorsGoal);
            this.canBreakDoors = false;
        }

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

    static {
        DOOR_BREAK_DIFFICULTY_CHECKER = (difficulty) -> {
            return difficulty == Difficulty.HARD;
        };
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
