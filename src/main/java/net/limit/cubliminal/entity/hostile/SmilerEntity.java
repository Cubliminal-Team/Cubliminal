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
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
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

    // Creates a set for a list of light blocks.
    // ReferenceOpenHashSet ensures that it only stores the list once when looking
    // through every block to see if it is a light.
    private static final Set<Block> LIGHT_BLOCKS = new ReferenceOpenHashSet<>();

    public SmilerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
    }

    public static DefaultAttributeContainer.Builder createAttributes(){
        // Gives the Smiller mob attributes such as the max health, movement speed, attack damage, etc.
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
        // A list of known light emitting blocks.
        List<Block> targetBlocks = new ArrayList<>(List.of(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.CAMPFIRE, Blocks.LANTERN, Blocks.GLOWSTONE, Blocks.SEA_LANTERN, Blocks.JACK_O_LANTERN, Blocks.REDSTONE_TORCH));
        // The variables used in the most dynamic way to set target goals.
        /// Gets the element size of how many light emitting blocks.
        int elementSize = targetBlocks.size();
        /// The start number from the priority of goals.
        int startNumber = 3;
        /// Gets the total size by adding the start number with the light block array.
        int totalSize = startNumber + elementSize;

        // Receives the target selector.
        GoalSelector target = this.targetSelector;
        target.add(1, new RevengeGoal(this));
        // Have Smilers attack players.
        target.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        // Have Smilers attack villagers.
        target.add(3, new ActiveTargetGoal<>(this, VillagerEntity.class, true));

        // Receives the goal selector.
        GoalSelector goal = this.goalSelector;
        // Give Smilers the ability to Melee attack the targeted entities.
        goal.add(2, new MeleeAttackGoal(this, 0.6f, true));
        // This makes the smiler destroy any light source.
        // Loops through the array of light emitting blocks.
        for (int i = 0; i < elementSize; i++){
            // Gets the current light emitting block.
            Block block = targetBlocks.get(i);
            // Adds a goal to destroy that light emitting block.
            goal.add(startNumber + i, new DestroyLightSourceGoal(block, this, 0.7f, 45, 10));
        }
        // Allows Smilers to wander around at a far distance and spread out better.
        goal.add(totalSize + 2, new WanderAroundFarGoal(this,0.3f));
        // Gives Smilers the ability to move through villages.
        goal.add(totalSize + 3, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        // Allows Smilers to wander around at shorter distances.
        goal.add(totalSize + 4, new WanderAroundGoal(this,0.6f));
        // Gives Smilers the ability to break doors so they are able to reach their target.
        goal.add(totalSize + 6, new BreakDoorGoal(this, difficulty -> true));
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        setCanBreakDoors(true);

        // Loops through every single block.
        for (Block block : Registries.BLOCK){
            // Loops through every state of the block.
            for (BlockState state : block.getStateManager().getStates()){
                // The luminance level the Smiler will target.
                int targetedLuminance = 1;
                // Checks to see if the luminance is greater or equal to the targeted.
                if (state.getLuminance() >= targetedLuminance){
                    // Adds the block to the set of light blocks.
                    LIGHT_BLOCKS.add(block);
                    // Breaks out of the state loop since it doesn't need to finish the loop.
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

    // Checks to see if the Smiler is in daylight and kills it.
    // This may be removed or modified in the future when improving the entity.
    @Override
    public void tick() {
        super.tick();
        boolean bl = this.isAffectedByDaylight();
        // Is affected by daylight.
        if (bl){
            // Kills it.
            this.setHealth(0f);
        } else {
            // Otherwise, it will get healed every tick.
            // May be modified as to improve game balancing.
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

        /**
         * Smiler goal to destroy any light source
         * @param targetBlock The target block to destroy. AKA a light source block.
         * @param mob The smiler mob
         * @param speed The speed at which the smiler entity goes to destroy the light source.
         * @param maxYDifference Distance from block from where smiler entity heads towards the target block.
         * @param i The range where the smiler entity is able to destroy the block.
         */
        public DestroyLightSourceGoal(Block targetBlock, PathAwareEntity mob, double speed, int maxYDifference, int i) {
            super(targetBlock, mob, speed, maxYDifference);
            this.innerRange = i;
        }

        @Override
        public void tick() {
            super.tick();
            // Checks to see when smiler entity is close enough to light block to destroy it.
            if (isInInnerRange()){
                // Breaks the light source block.
                this.mob.getWorld().breakBlock(this.targetPos, false, this.mob);
            }
        }

        /**
         * Checks to see if the Smiler is within range of the block.
         * @return Returns true when entity is in range of target block.
         */
        private boolean isInInnerRange() {
            return this.mob.getBlockPos().getSquaredDistance(this.targetPos) <= (this.innerRange * this.innerRange);
        }
    }
}
