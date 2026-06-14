package net.limit.cubliminal.entity.hostile;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class SkinStealerEntity extends HostileEntity implements Angerable {

    private static final Identifier ATTACKING_SPEED_MODIFIER_ID = Identifier.ofVanilla("attacking");
    private static final EntityAttributeModifier ATTACKING_SPEED_BOOST = new EntityAttributeModifier(
            SkinStealerEntity.ATTACKING_SPEED_MODIFIER_ID, 0.15f, EntityAttributeModifier.Operation.ADD_VALUE
    );
    private static final TrackedData<Boolean> ANGRY = DataTracker.registerData(SkinStealerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(70, 80);
    private static final TrackedData<Boolean> IN_DISGUISED = DataTracker.registerData(SkinStealerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<UUID>> DISGUISED_AS = DataTracker.registerData(SkinStealerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final UniformIntProvider DISGUISED_TIME_RANGE = TimeHelper.betweenSeconds(40, 60);

    public static final EntityDimensions PLAYER_DIMENSIONS = EntityDimensions.changing(0.6F, 1.8F)
            .withEyeHeight(1.62F)
            .withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, PlayerEntity.VEHICLE_ATTACHMENT_POS));

    private int angerTime;
    @Nullable
    private UUID angryAt;

    private int disguisedTime;
    private UUID disguisedAs;

    public SkinStealerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 60)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.15)
                .add(EntityAttributes.ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SkinStealerEntity.FleeAndRecoverGoal(this, 15, 25, 40, 1f));
        this.goalSelector.add(2, new SkinStealerEntity.AttackGoal(this, 1.0, false));
        this.goalSelector.add(3, new SkinStealerEntity.FollowVictimGoal(this, 1.0, 4f));
        this.goalSelector.add(4, new SkinStealerEntity.RevealSelfGoal(this, 28));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0, 0.0f));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, false, false, null));
        this.targetSelector.add(2, new RevengeGoal(this));
        this.targetSelector.add(4, new UniversalAngerGoal<>(this, false));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (target == null) {
            this.dataTracker.set(ANGRY, false);
            entityAttributeInstance.removeModifier(ATTACKING_SPEED_MODIFIER_ID);
        } else {
            this.dataTracker.set(ANGRY, true);
            if (!entityAttributeInstance.hasModifier(ATTACKING_SPEED_MODIFIER_ID)) {
                entityAttributeInstance.addTemporaryModifier(ATTACKING_SPEED_BOOST);
            }
        }
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other) {
        if (other instanceof PlayerEntity player) {
            this.setDisguise(player);
        }
        return super.onKilledOther(world, other);
    }

    @Override
    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isInDisguised() ? PLAYER_DIMENSIONS : super.getBaseDimensions(pose);
    }

    public void setDisguise(PlayerEntity player) {
        this.setDisguisedTime(DISGUISED_TIME_RANGE.get(this.random));
        this.setInDisguised(true);
        this.setDisguisedAs(player.getUuid());
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (IN_DISGUISED.equals(data)) {
            this.calculateDimensions();
        }
    }

    public void revealSelf() {
        this.setInDisguised(false);
        this.setDisguisedAs(null);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ANGRY, false);
        builder.add(IN_DISGUISED, false);
        builder.add(DISGUISED_AS, Optional.empty());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.writeAngerToNbt(nbt);
        nbt.putBoolean("InDisguised", this.isInDisguised());
        nbt.putInt("DisguisedTime", this.getDisguisedTime());

        this.getDisguisedAs().ifPresent(value -> nbt.putUuid("DisguisedAs", value));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.readAngerFromNbt(this.getWorld(), nbt);
        this.setInDisguised(nbt.getBoolean("InDisguised"));
        this.setDisguisedTime(nbt.getInt("DisguisedTime"));
        this.setDisguisedAs(nbt.containsUuid("DisguisedAs") ? nbt.getUuid("DisguisedAs") : null);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);

        if (this.age % 40 == 0 && this.timeUntilRegen <= 0) {
            this.heal(1);
        }
    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient) {
            this.tickAngerLogic((ServerWorld)this.getWorld(), true);

            if (this.disguisedTime > 0) {
                this.disguisedTime -= 1;
            }
        }
        super.tickMovement();
    }

    public boolean isAngry() {
        return this.dataTracker.get(ANGRY);
    }

    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    public boolean isInDisguised() {
        return this.dataTracker.get(IN_DISGUISED);
    }

    public void setInDisguised(boolean value) {
        this.dataTracker.set(IN_DISGUISED, value);
    }

    public Optional<UUID> getDisguisedAs() {
        return this.dataTracker.get(DISGUISED_AS);
    }

    public void setDisguisedAs(UUID disguisedAs) {
        this.dataTracker.set(DISGUISED_AS, disguisedAs == null ? Optional.empty() : Optional.of(disguisedAs));
    }

    public int getDisguisedTime() {
        return disguisedTime;
    }

    public void setDisguisedTime(int disguisedTime) {
        this.disguisedTime = disguisedTime;
    }

    private static class FollowVictimGoal extends Goal {
        private final SkinStealerEntity skinStealer;
        private final double speed;
        private final EntityNavigation navigation;
        private final float minDistance;

        private PlayerEntity target;
        private float oldWaterPathFindingPenalty;
        private int updateCountdownTicks;

        public FollowVictimGoal(SkinStealerEntity skinStealer, double speed, float minDistance) {
            this.skinStealer = skinStealer;
            this.speed = speed;
            this.navigation = skinStealer.getNavigation();
            this.minDistance = minDistance;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            UUID targetUUID = this.skinStealer.getAngryAt();
            if (targetUUID == null) return false;
            this.target = this.skinStealer.getWorld().getPlayerByUuid(targetUUID);
            return this.skinStealer.isInDisguised() && this.skinStealer.isAngry() && this.target != null;
        }

        @Override
        public boolean shouldContinue() {
            return this.target != null && this.skinStealer.isInDisguised() && !this.navigation.isIdle() && this.skinStealer.squaredDistanceTo(this.target) > this.minDistance * this.minDistance;
        }

        @Override
        public void start() {
            this.updateCountdownTicks = 0;
            this.oldWaterPathFindingPenalty = this.skinStealer.getPathfindingPenalty(PathNodeType.WATER);
            this.skinStealer.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
        }

        @Override
        public void stop() {
            this.target = null;
            this.navigation.stop();
            this.skinStealer.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathFindingPenalty);;
        }

        @Override
        public void tick() {
            if (this.target != null && !this.skinStealer.isLeashed()) {
                this.skinStealer.getLookControl().lookAt(this.target, 10.0F, this.skinStealer.getMaxLookPitchChange());
                if (--this.updateCountdownTicks <= 0) {
                    this.updateCountdownTicks = this.getTickCount(10);
                    double xDist = this.skinStealer.getX() - this.target.getX();
                    double yDist = this.skinStealer.getY() - this.target.getY();
                    double zDist = this.skinStealer.getZ() - this.target.getZ();
                    double dist = xDist * xDist + yDist * yDist + zDist * zDist;
                    if (dist > this.minDistance * this.minDistance) {
                        this.navigation.startMovingTo(this.target, this.speed);
                    } else {
                        this.navigation.stop();
                    }
                }
            }
        }
    }

    private static class AttackGoal extends MeleeAttackGoal {
        private final SkinStealerEntity skinStealer;

        public AttackGoal(SkinStealerEntity mob, double speed, boolean pauseWhenMobIdle) {
            super(mob, speed, pauseWhenMobIdle);
            this.skinStealer = mob;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !this.skinStealer.isInDisguised();
        }
    }

    private static class RevealSelfGoal extends Goal {
        private final SkinStealerEntity skinStealer;
        private final float maxDistance;
        private PlayerEntity target;

        private RevealSelfGoal(SkinStealerEntity skinStealer, float maxDistance) {
            this.skinStealer = skinStealer;
            this.maxDistance = maxDistance;
        }

        @Override
        public boolean canStart() {
            UUID targetUUID = this.skinStealer.getAngryAt();
            if (targetUUID == null) return false;
            this.target = this.skinStealer.getWorld().getPlayerByUuid(targetUUID);
            return this.skinStealer.isInDisguised() && this.skinStealer.isAngry() && this.target != null;
        }

        @Override
        public boolean shouldContinue() {
            return this.skinStealer.isInDisguised() && this.target != null;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.skinStealer.distanceTo(this.target) >= this.maxDistance || this.skinStealer.getDisguisedTime() <= 0) {
                this.skinStealer.revealSelf();
            }
        }
    }

    private static class FleeAndRecoverGoal extends FleeEntityGoal<PlayerEntity> {
        private final SkinStealerEntity skinStealer;
        private final float maxHealthToTrigger;
        private final float maxHealthToHeal;

        private FleeAndRecoverGoal(SkinStealerEntity skinStealer, float maxHealthToTrigger, float maxHealthToHeal, float fleeDistance, double fleeSpeed) {
            super(skinStealer, PlayerEntity.class, fleeDistance, fleeSpeed, fleeSpeed);
            this.skinStealer = skinStealer;
            this.maxHealthToTrigger = maxHealthToTrigger;
            this.maxHealthToHeal = maxHealthToHeal;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && this.skinStealer.getHealth() <= this.maxHealthToTrigger;
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() || this.skinStealer.getHealth() <= this.maxHealthToHeal;
        }

        @Override
        public void start() {
            super.start();
            this.skinStealer.setTarget(null);
        }

        @Override
        public void tick() {
            super.tick();

            if (skinStealer.age % 10 == 0) {
                this.skinStealer.heal(2);
            }
        }
    }
}
