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

@Environment(EnvType.CLIENT)
public class ClientSoundHelper {

    public static void playSound(ConditionedSoundInstance soundInstance) {
        playSoundAt(soundInstance, soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
    }

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

    public static void playSoundAtBlock(ConditionedSoundInstance soundInstance) {
        playSoundAt(soundInstance, soundInstance.getX() + 0.5, soundInstance.getY() + 0.5, soundInstance.getZ() + 0.5);
    }

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
