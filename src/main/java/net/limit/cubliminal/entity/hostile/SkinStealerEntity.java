package net.limit.cubliminal.entity.hostile;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.event.backrooms.skindatabase.*;
import net.limit.cubliminal.init.CubliminalMessageTypes;
import net.limit.cubliminal.init.CubliminalSounds;
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
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
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
    private static final UniformIntProvider DISGUISED_TIME_RANGE = TimeHelper.betweenSeconds(10, 20);

    // The dimensions for the disguised state
    public static final EntityDimensions PLAYER_DIMENSIONS = EntityDimensions.changing(0.6F, 1.8F)
            .withEyeHeight(1.62F)
            .withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, PlayerEntity.VEHICLE_ATTACHMENT_POS));

    private int angerTime;
    @Nullable
    private UUID angryAt;

    // Time left in disguise
    private int disguisedTime;

    public SkinStealerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        PlayerDataManager<PlayerSkinData> skinsData = PlayerInfoManager.getInstance().getSkins();

        // 1/3 chance to spawn in disguise
        if (skinsData.getEntryCount() > 0 && this.random.nextBetween(0, 2) == 0) {
            PlayerSkinData playerSkinData = skinsData.getRandomPlayer(this.getRandom());
            this.setDisguise(playerSkinData);
        }

        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 60)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.15)
                .add(EntityAttributes.ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SkinStealerEntity.FleeAndRecoverGoal(this, 15, 25, 40, 1f)); // Make the mob flee and recover health faster then normal
        this.goalSelector.add(2, new SkinStealerEntity.AttackGoal(this, 1.0, false)); // Make the mob attack if not disguised
        this.goalSelector.add(3, new SkinStealerEntity.MimicPlayerGoal(this, 80)); // Follow the victim when in disguise
        this.goalSelector.add(3, new SkinStealerEntity.FollowVictimGoal(this, 1.0, 4f)); // Follow the victim when in disguise
        this.goalSelector.add(4, new SkinStealerEntity.RevealSelfGoal(this, 28)); // Reveal its true form when he wants to attack
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0, 0.0f));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, false, false, null)); // Search for victim in the area
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
        // Take the skin of the last player killed
        if (other instanceof ServerPlayerEntity player) {
            PlayerDataManager<PlayerSkinData> skinsData = PlayerInfoManager.getInstance().getSkins();

            PlayerSkinData playerSkinData;

            // Update / store the player in the database
            if (skinsData.hasPlayerData(player.getUuid())) {
                playerSkinData = skinsData.updatePlayerData(player.getUuid(), data -> {
                    data.setDisplayName(player.getDisplayName());
                });
            } else {
                playerSkinData = PlayerSkinData.createFromPlayer(player);
                skinsData.storePlayerData(playerSkinData);
            }

            this.setDisguise(playerSkinData);
        }
        return super.onKilledOther(world, other);
    }

    @Override
    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        // Change the disguise mob dimensions
        return this.isInDisguised() ? PLAYER_DIMENSIONS : super.getBaseDimensions(pose);
    }

    /**
     * Sets the skin stealer disguise
     *
     * @param playerSkinData player skin data
     */
    public void setDisguise(PlayerSkinData playerSkinData) {
        this.setDisguisedTime(DISGUISED_TIME_RANGE.get(this.random));
        this.setInDisguised(true);
        this.setDisguisedAs(playerSkinData.getUuid());
        this.setCustomNameVisible(true);
        this.setCustomName(playerSkinData.getDisplayName());

        if (this.getWorld() instanceof ServerWorld world) {
            this.spawnTransitionParticles(world);
        }
    }

    /**
     * Remove the skin stealer disguise
     */
    public void revealSelf() {
        this.setInDisguised(false);
        this.setDisguisedAs(null);
        this.setCustomNameVisible(false);
        this.setCustomName(null);

        if (this.getWorld() instanceof ServerWorld world) {
            this.spawnTransitionParticles(world);
        }

        if (!this.getWorld().isClient()) {
            this.playSound(CubliminalSounds.SKIN_STEALER_ANGRY, 2.5f, 1f);
        }
    }

    private void spawnTransitionParticles(ServerWorld world) {
        world.spawnParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 400, 0.5, 1, 0.5, 0);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        // Update the mob dimensions when the disguise state change
        if (IN_DISGUISED.equals(data)) {
            this.calculateDimensions();
        }
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

        // Make the mob regen 1 hp every 2 seconds
        if (this.age % 40 == 0 && this.timeUntilRegen <= 0) {
            this.heal(1);
        }
    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient) {
            this.tickAngerLogic((ServerWorld)this.getWorld(), true);

            if (this.disguisedTime > 0 && this.isAngry()) {
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

    private void setInDisguised(boolean value) {
        this.dataTracker.set(IN_DISGUISED, value);
    }

    public Optional<UUID> getDisguisedAs() {
        return this.dataTracker.get(DISGUISED_AS);
    }

    private void setDisguisedAs(UUID disguisedAs) {
        this.dataTracker.set(DISGUISED_AS, disguisedAs == null ? Optional.empty() : Optional.of(disguisedAs));
    }

    public int getDisguisedTime() {
        return disguisedTime;
    }

    private void setDisguisedTime(int disguisedTime) {
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

        /**
         * Follow the victim
         *
         * @param skinStealer skin stealer
         * @param speed follow speed
         * @param minDistance the min distance between the victim and the skin stealer
         */
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

        /**
         * Same as {@link MeleeAttackGoal} but the skin stealer only attack when is in disguise
         *
         * @param mob skin stealer
         * @param speed skin stealer move speed
         * @param pauseWhenMobIdle pause when idle
         */
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
        private double maxHealth;
        private boolean wasNear;

        /**
         * This triggers when the skin stealer is in disguise and if one of the 3 is happening.
         * <br>
         * 1. the skin stealer is enough time in disguise.
         * <br>
         * 2. the player is far from the skin stealer.
         * <br>
         * 3. the skin stealer has taken damage
         *
         * @param skinStealer mob
         * @param maxDistance max distance between the mob and the player
         */
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
        public void stop() {
            this.wasNear = false;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.maxHealth = Math.max(maxHealth, this.skinStealer.getHealth());

            if (this.skinStealer.getDisguisedTime() <= 0 || this.skinStealer.getHealth() < this.maxHealth) {
                this.skinStealer.revealSelf();
            }

            if (this.skinStealer.squaredDistanceTo(this.target) >= this.maxDistance * this.maxDistance) {
                if (this.wasNear) {
                    this.skinStealer.revealSelf();
                }
            } else {
                this.wasNear = true;
            }
        }
    }

    private static class FleeAndRecoverGoal extends FleeEntityGoal<PlayerEntity> {
        private final SkinStealerEntity skinStealer;
        private final float maxHealthToTrigger;
        private final float maxHealthToHeal;

        /**
         *
         * @param skinStealer mob
         * @param maxHealthToTrigger The maximum health for the mob to trigger this action
         * @param maxHealthToHeal The maximum health to heal
         * @param fleeDistance Distance to flee
         * @param fleeSpeed mob move speed
         */
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

    private static class MimicPlayerGoal extends Goal {
        private final SkinStealerEntity skinStealer;
        private final int chance;

        private PlayerEntity target;
        private PlayerMessageData playerMessageData;
        private PlayerSkinData playerSkinData;
        private boolean responded = false;

        private MimicPlayerGoal(SkinStealerEntity skinStealer, int chance) {
            this.skinStealer = skinStealer;
            this.chance = chance;
        }

        @Override
        public boolean canStart() {
            UUID targetUUID = this.skinStealer.getAngryAt();
            if (targetUUID == null) return false;
            this.target = this.skinStealer.getWorld().getPlayerByUuid(targetUUID);

            Optional<UUID> disguiseUuid = this.skinStealer.getDisguisedAs();

            if (disguiseUuid.isPresent()) {
                PlayerInfoManager playerInfoManager = PlayerInfoManager.getInstance();

                this.playerSkinData = playerInfoManager.getSkins().getPlayerData(disguiseUuid.get()).orElse(null);
                this.playerMessageData = playerInfoManager.getMessages().getPlayerData(disguiseUuid.get()).orElse(null);
            }

            return this.skinStealer.isInDisguised() && this.skinStealer.isAngry() && this.target != null && this.playerMessageData != null && this.playerSkinData != null;
        }

        @Override
        public boolean shouldContinue() {
            return this.skinStealer.isInDisguised() && this.target != null && this.playerMessageData != null && this.playerSkinData != null;
        }

        @Override
        public void tick() {
            PlayerMessageProcessor.Intent playerIntent = PlayerInfoManager.getInstance().getPlayerIntent(this.target.getUuid());

            // reset the response
            responded = responded && playerIntent != null;

            if (playerIntent == PlayerMessageProcessor.Intent.GREETING && !this.responded) {
                this.mimicPlayer(PlayerMessageProcessor.Intent.GREETING);
                this.markResponse();
            } else if (this.skinStealer.squaredDistanceTo(this.target) > 7*7 && this.shouldTriggerRandomResponse()) {
                this.mimicPlayer(PlayerMessageProcessor.Intent.COME_HERE, PlayerMessageProcessor.Intent.HELP);
            } else if (playerIntent == PlayerMessageProcessor.Intent.HESITATION && !this.responded) {
               this.mimicPlayer(PlayerMessageProcessor.Intent.TRUST);
                this.markResponse();
            } else if (this.shouldTriggerRandomResponse()) {
                this.mimicPlayer(PlayerMessageProcessor.Intent.GREETING);
            }
        }

        private void mimicPlayer(PlayerMessageProcessor.Intent... intents) {
            Cubliminal.LOGGER.info("trying to mimic");
            PlayerMessageProcessor.ProcessedMessage processedMessage = this.playerMessageData.getRandomMessageFromIntents(this.skinStealer.getRandom(), intents);
            if (processedMessage == null) return;

            UUID mimicPlayerUuid = this.skinStealer.getDisguisedAs().get();

            ServerPlayerEntity mimicPlayer = skinStealer.getServer().getPlayerManager().getPlayer(mimicPlayerUuid);

            MessageType.Parameters parameters = MessageType.params(CubliminalMessageTypes.MIMIC, this.skinStealer.getRegistryManager(), this.playerSkinData.getDisplayName());
            if (mimicPlayer == null) {
                this.skinStealer.getServer().getPlayerManager().broadcast(parameters.applyChatDecoration(processedMessage.message()), false);
            } else {
                SignedMessage signedMessage = SignedMessage.ofUnsigned(mimicPlayerUuid, processedMessage.message().getString());
                this.skinStealer.getServer().getPlayerManager().broadcast(signedMessage, mimicPlayer, parameters);
            }
        }

        private boolean shouldTriggerRandomResponse() {
            return chance <= 0 || this.skinStealer.random.nextBetween(0, chance) == 0;
        }

        private void markResponse() {
            this.responded = true;
            PlayerInfoManager.getInstance().removePlayerIntent(this.target.getUuid());
        }
    }
}
