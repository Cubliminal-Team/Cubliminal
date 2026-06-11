package net.limit.cubliminal.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.entity.FluxCapacitorBlockEntity;
import net.limit.cubliminal.init.CubliminalSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

/**
 * This class holds all the custom client side sound utility functions.
 */

@Environment(EnvType.CLIENT)
public class ClientSoundHelper {

    public static void playSound(ConditionedSoundInstance soundInstance) {
        playSoundAt(soundInstance, soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
    }

    /**
     * Plays a sound instance if the client is close enough.
     * @param soundInstance The desired sound instance.
     * @param x X pos.
     * @param y Y pos.
     * @param z Z pos.
     */
    public static void playSoundAt(ConditionedSoundInstance soundInstance, double x, double y, double z) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            double distance = soundInstance.getSoundEvent().getDistanceToTravel(soundInstance.getUnmodifiedVolume());
            double d = x - player.getX();
            double e = y - player.getY();
            double f = z - player.getZ();
            if (d * d + e * e + f * f < distance * distance) {
                MinecraftClient.getInstance().getSoundManager().play(soundInstance);
            }
        }
    }

    /**
     * Plays a sound instance centered on a block.
     * @param soundInstance The desired sound instance.
     */
    public static void playSoundAtBlock(ConditionedSoundInstance soundInstance) {
        playSoundAt(soundInstance, soundInstance.getX() + 0.5, soundInstance.getY() + 0.5, soundInstance.getZ() + 0.5);
    }

    /**
     * Stops a specific sound instance that is being played. You would need to save it as a variable after being played
     * to stop it later. If you're on a common side class, you can save it as an {@code Object} and then cast it.
     * @param soundInstance The sound instance to be stopped.
     */
    public static void stopSound(SoundInstance soundInstance) {
        MinecraftClient.getInstance().getSoundManager().stop(soundInstance);
    }

    public static void playFluxCapacitor(FluxCapacitorBlockEntity blockEntity) {
        if (blockEntity.soundInstance != null) stopSound((SoundInstance) blockEntity.soundInstance);
        blockEntity.soundInstance = new ConditionedSoundInstance(
                CubliminalSounds.FLUX_CAPACITOR.value(),
                SoundCategory.BLOCKS,
                SoundInstance.AttenuationType.LINEAR,
                () -> Vec3d.of(blockEntity.getPos()),
                () -> !blockEntity.isRemoved());
        playSoundAtBlock((ConditionedSoundInstance) blockEntity.soundInstance);
    }

    public static void stopFluxCapacitor(Object soundInstance) {
        stopSound((SoundInstance) soundInstance);
    }
}
