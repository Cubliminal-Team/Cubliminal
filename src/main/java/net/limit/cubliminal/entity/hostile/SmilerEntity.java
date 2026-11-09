package net.limit.cubliminal.entity.hostile;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class SmilerEntity extends HostileEntity {

    private BreakDoorGoal breakDoorsGoal;
    private boolean canBreakDoors;

    private static final Set<Block> LIGHT_BLOCKS = new ReferenceOpenHashSet<>();

    public SmilerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH,50)
                .add(EntityAttributes.MOVEMENT_SPEED,0.3)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 10)
                .add(EntityAttributes.FOLLOW_RANGE, 25);
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 15.0f));
        this.goalSelector.add(4, new DestroyLightSourceGoal(this, 0.3, 3, 2));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        /*
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        targetSelector.add(3, new ActiveTargetGoal<>(this, VillagerEntity.class, true));

        GoalSelector goalSelector = this.goalSelector;
        goalSelector.add(0, new MeleeAttackGoal(this, 0.6, true));
        goalSelector.add(1, new DestroyLightSourceGoal(this, 0.7f, 45, 10));
        goalSelector.add(2, new WanderAroundFarGoal(this,0.3f));
        goalSelector.add(4, new WanderAroundGoal(this,0.6f));
        this.breakDoorsGoal = new BreakDoorGoal(this, difficulty -> difficulty == Difficulty.HARD);
        goalSelector.add(5, this.breakDoorsGoal);

         */
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        //this.setCanBreakDoors(true);
        for (Block block : Registries.BLOCK){
            for (BlockState state : block.getStateManager().getStates()) {
                int targetedLuminance = 2;
                if (state.getLuminance() >= targetedLuminance){
                    LIGHT_BLOCKS.add(block);
                    break;
                }
            }
        }

        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public void setCanBreakDoors(boolean canBreakDoors) {
        if (NavigationConditions.hasMobNavigation(this)) {
            if (this.canBreakDoors != canBreakDoors) {
                this.canBreakDoors = canBreakDoors;
                ((MobNavigation) this.getNavigation()).setCanPathThroughDoors(canBreakDoors);
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

    // Checks to see if the Smiler is in daylight and kills it.
    // This may be removed or modified in the future when improving the entity.
    @Override
    public void tick() {
        super.tick();
        if (this.isAffectedByDaylight()) {
            this.setHealth(0f);
        } else {
            this.heal(2f);
        }
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    static class DestroyLightSourceGoal extends StepAndDestroyBlockGoal {
        private final int innerRange;

        /**
         * Smiler goal to destroy any light source
         * @param mob The smiler mob
         * @param speed The speed at which the smiler entity goes to destroy the light source.
         * @param maxYDifference Distance from block from where smiler entity heads towards the target block.
         * @param innerRange The range where the smiler entity is able to destroy the block.
         */
        public DestroyLightSourceGoal(PathAwareEntity mob, double speed, int maxYDifference, int innerRange) {
            super(Blocks.TORCH, mob, speed, maxYDifference);
            this.innerRange = innerRange;
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            BlockState state = world.getBlockState(pos);
            if (!LIGHT_BLOCKS.contains(state.getBlock())) {
                return false;
            }

            if (state.contains(Properties.LIT)) {
                return state.get(Properties.LIT);
            }

            return true;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.isInInnerRange()) {
                this.mob.getWorld().breakBlock(this.targetPos, false, this.mob);
            }
        }

        private boolean isInInnerRange() {
            return this.mob.getBlockPos().getSquaredDistance(this.targetPos) <= (this.innerRange * this.innerRange);
        }
    }
}
