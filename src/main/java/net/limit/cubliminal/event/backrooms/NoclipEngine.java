package net.limit.cubliminal.event.backrooms;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.init.CubliminalRegistrar;
import net.limit.cubliminal.init.CubliminalSounds;
import net.limit.cubliminal.networking.s2c.NoClipSyncPayload;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

/**
 * Manages the whole no-clip mechanics and actions of a single player: there is a single {@code NoclipEngine} per player.
 * This mechanic is highly experimental and needs to be balanced.
 * <p>
 *     {@code ticksToNc} tick down inside the Backrooms
 * </p>
 */

public class NoclipEngine {

    private int ticksToNc = 0;
    private RegistryKey<World> ncDestination;

    public void update(ServerPlayerEntity player) {
        if (this.ticksToNc > 1) {
            if (player.isSprinting()) {
                --this.ticksToNc;
            }
        } else if (this.ticksToNc < 0 && this.ticksToNc > -80) {
            --this.ticksToNc;
        } else if (this.ticksToNc != 1) {
            if (this.ticksToNc < -79 && player.isAlive() && !player.isDisconnected()) {
                this.teleport(player);
            }

            this.ticksToNc = player.getRandom().nextInt(6000) + 12000;
        }

        ServerPlayNetworking.send(player, new NoClipSyncPayload(this.ticksToNc));
    }

    public void teleport(ServerPlayerEntity player) {
        if (this.ncDestination != null) {
            NoclipDestination destination = NoclipDestination.fromDestination(this.ncDestination).getSecond();
            Pair<BlockPos, Vec3d> positions = destination.locate(player);
            player.setSpawnPoint(this.ncDestination, positions.getFirst(), 0f, true, false);
            TeleportTarget teleportTarget = new TeleportTarget(
                    player.getServer().getWorld(this.ncDestination), positions.getSecond(),
                    Vec3d.ZERO, player.getYaw(), player.getPitch(), NoclipEngine::afterNoCLip);
            player.teleportTo(teleportTarget);
        }
    }

    public void noclip(PlayerEntity playerEntity) {
        if (playerEntity instanceof ServerPlayerEntity player) {
            RegistryKey<World> to = NoclipDestination.from(player.getWorld().getRegistryKey()).getFirst();
            this.noclipTo(player, to);
        }
    }

    public void noclipTo(PlayerEntity playerEntity, RegistryKey<World> to) {
        if (playerEntity instanceof ServerPlayerEntity player) {
            CubliminalSounds.clientPlaySoundSingle(
                    player, CubliminalSounds.NOCLIPPING, SoundCategory.MASTER,
                    player.getX(), player.getY(), player.getZ(),1f, 1f, 1);
            this.ticksToNc = -1;
            this.ncDestination = to;
        }
    }

    public static void afterNoCLip(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, true, false, true));
        }
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("TicksToNc", NbtElement.NUMBER_TYPE)) {
            this.ticksToNc = nbt.getInt("TicksToNc");
        }
        if (nbt.contains("NcDestination")) {
            this.ncDestination = World.CODEC
                    .parse(NbtOps.INSTANCE, nbt.get("NcDestination"))
                    .resultOrPartial(Cubliminal.LOGGER::error)
                    .orElse(CubliminalRegistrar.THE_LOBBY_KEY);
        }
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("TicksToNc", this.ticksToNc);
        if (this.ncDestination != null) {
            Identifier.CODEC
                    .encodeStart(NbtOps.INSTANCE, this.ncDestination.getValue())
                    .resultOrPartial(Cubliminal.LOGGER::error)
                    .ifPresent(encoded -> nbt.put("NcDestination", encoded));
        }
    }

    public boolean isClipping() {
        return this.ticksToNc < 0;
    }

    public boolean canClip() {
        return this.ticksToNc == 1;
    }

    public int getTicksToNc() {
        return this.ticksToNc;
    }

    public void setTicksToNc(int ticks) {
        this.ticksToNc = ticks;
    }
}
