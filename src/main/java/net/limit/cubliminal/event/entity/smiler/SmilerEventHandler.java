package net.limit.cubliminal.event.entity.smiler;

import net.limit.cubliminal.util.WeightedHolderSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SmilerEventHandler extends PersistentState {

    public List<SmilerTask> randomTasks = List.of(
            new InvadeTask(), new WatchTask(), new FollowTask(), new StareTask()
    );

    public final ServerWorld world;
    public long timeWithPlayer;
    public long timeWithoutActions;

    public SmilerTask smilerTask;
    public UUID smilerUUID;

    public PlayerEntity chosenPlayer;
    public UUID playerUUID;

    public static PersistentState.Type<SmilerEventHandler> getPersistentStateType(ServerWorld world) {
        return new Type<>(
                () -> new SmilerEventHandler(world),
                (nbt, registries) -> fromNbt(world, nbt),
                null
        );
    }

    public SmilerEventHandler(ServerWorld world) {
        this(world, 0, 0, null, null, null);
    }

    public SmilerEventHandler(ServerWorld world, long timeWithPlayer, long timeWithoutActions,
                              SmilerTask smilerTask, UUID smilerUUID, UUID playerUUID) {
        this.world = world;
        this.timeWithPlayer = timeWithPlayer;
        this.timeWithoutActions = timeWithoutActions;
        this.smilerTask = smilerTask;
        this.smilerUUID = smilerUUID;
        this.playerUUID = playerUUID;
        this.tryRecoverPlayerByUUID();
    }

    private void tryRecoverPlayerByUUID() {
        if (this.playerUUID == null) {
            this.chosenPlayer = null;
        } else {
            PlayerEntity player = this.world.getPlayerByUuid(this.playerUUID);
            if (player == null) {
                this.chosenPlayer = null;
            } else {
                this.setChosenPlayer(player);
            }
        }
    }

    public void setChosenPlayer(PlayerEntity player) {
        this.chosenPlayer = player;
        this.playerUUID = player.getUuid();
        this.markDirty();
        System.out.println("Chose player: " + player.getNameForScoreboard());
    }

    public Optional<PlayerEntity> tryToChoosePlayer() {
        System.out.println("Trying to choose a player");
        List<ServerPlayerEntity> candidates = this.world.getPlayers(SmilerEventHandler::canStalkPlayer);
        if (!candidates.isEmpty()) {
            return Optional.of(candidates.get(this.world.getRandom().nextInt(candidates.size())));
        } else {
            return Optional.empty();
        }
    }

    public static boolean canStalkPlayer(PlayerEntity player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    public void setSmilerTask(SmilerTask task) {
        System.out.println("Switching to smiler task: " + task.toString());
        this.smilerTask = task;
        this.markDirty();
    }

    public void chooseRandomTask() {
        WeightedHolderSet<SmilerTask> tasks = new WeightedHolderSet<>(4);
        for (SmilerTask task : this.randomTasks) {
            if (this.timeWithPlayer >= task.getMinTicksToAllow()) {
                tasks.add(task.getChanceToBeChosen(), task);
            }
        }

        if (!tasks.isEmpty()) {
            this.setSmilerTask(tasks.random(this.world.getRandom()));
            this.smilerTask.begin();
        }
    }

    public void tickFromWorld() {
        if (this.chosenPlayer == null || this.smilerTask == null || !canStalkPlayer(this.chosenPlayer)) {
            ++this.timeWithoutActions;
        } else {
            this.timeWithoutActions = 0;
            ++this.timeWithPlayer;
        }

        if (this.world.getTime() % 600 == 0) {
            if (this.playerUUID == null || this.timeWithoutActions > 6000) {
                System.out.println("Too much time without actions");
                this.tryToChoosePlayer().ifPresent(player -> {
                    this.setChosenPlayer(player);
                    this.timeWithPlayer = 0;
                    this.smilerTask = null;
                });
            } else if (this.chosenPlayer == null) {
                System.out.println("Trying to recover player with UUID: " + this.playerUUID);
                PlayerEntity player = this.world.getPlayerByUuid(this.playerUUID);
                if (player != null) {
                    this.setChosenPlayer(player);
                }
            }

            if (this.chosenPlayer != null && this.smilerTask == null) {
                this.chooseRandomTask();
            }
        }

        if (this.world.getTime() % 200 == 0) {
            this.markDirty();
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putLong("TimeWithPlayer", this.timeWithPlayer);
        nbt.putLong("TimeWithoutActions", this.timeWithoutActions);
        if (this.smilerTask != null) nbt.putString("SmilerTask", this.smilerTask.toString());
        if (this.smilerUUID != null) nbt.putUuid("SmilerUUID", this.smilerUUID);
        if (this.playerUUID != null) nbt.putUuid("PlayerUUID", this.playerUUID);
        return nbt;
    }

    public static SmilerEventHandler fromNbt(ServerWorld world, NbtCompound nbt) {
        return new SmilerEventHandler(
                world,
                nbt.getLong("TimeWithPlayer"),
                nbt.getLong("TimeWithoutActions"),
                nbt.contains("SmilerTask") ? SmilerTask.fromNbt(nbt) : null,
                nbt.containsUuid("SmilerUUID") ? nbt.getUuid("SmilerUUID") : null,
                nbt.containsUuid("PlayerUUID") ? nbt.getUuid("PlayerUUID") : null
        );
    }
}
