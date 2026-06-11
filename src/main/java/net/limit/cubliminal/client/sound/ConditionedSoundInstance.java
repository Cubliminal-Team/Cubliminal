package net.limit.cubliminal.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Minecraft sound instances don't perform attenuation by default as you move away, which really breaks immersiveness
 * in some cases. We made our own sound instance class to fix this.
 * <p>
 *     Each tick it checks whether it should play according to a {@code BooleanSupplier}, incredibly useful for cases
 *     where the sound source is suddenly removed. Linear attenuation is then calculated from the distance between
 *     the source and the client, updating the volume in real time.
 * </p>
 */

@Environment(EnvType.CLIENT)
public class ConditionedSoundInstance extends MovingSoundInstance {

    private final SoundEvent soundEvent;
    private Vec3d pos;
    private final Supplier<Vec3d> posSupplier;
    private final BooleanSupplier shouldPlay;

    public ConditionedSoundInstance(SoundEvent soundEvent, SoundCategory soundCategory, AttenuationType attenuationType, Supplier<Vec3d> posSupplier, BooleanSupplier shouldPlay) {
        super(soundEvent, soundCategory, SoundInstance.createRandom());
        this.soundEvent = soundEvent;
        this.attenuationType = attenuationType;
        this.posSupplier = posSupplier;
        this.pos = posSupplier.get();
        this.shouldPlay = shouldPlay;
    }

    @Override
    public void tick() {
        if (this.shouldPlay.getAsBoolean()) {
            this.pos = this.posSupplier.get();
            if (this.attenuationType == AttenuationType.LINEAR) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    Vec3d playerPos = player.getPos();
                    double dx = this.pos.getX() - playerPos.getX();
                    double dy = this.pos.getY() - playerPos.getY();
                    double dz = this.pos.getZ() - playerPos.getZ();
                    float attenuationDist = this.sound.getAttenuation();
                    double sqDist = dx * dx + dy * dy + dz * dz;
                    if (sqDist >= attenuationDist * attenuationDist) {
                        this.volume = 0.0f;
                    } else {
                        double distance = Math.sqrt(sqDist);
                        double newVol = 1.0 - distance / attenuationDist;
                        this.volume = (float) getAdjustedVolume(newVol, this.category);
                    }
                }
            }
        } else {
            this.setDone();
        }
    }

    private static double getAdjustedVolume(double volume, SoundCategory category) {
        return MathHelper.clamp(volume * getSoundVolume(category), 0.0f, 1.0f);
    }

    private static float getSoundVolume(@Nullable SoundCategory category) {
        return category != null && category != SoundCategory.MASTER ? MinecraftClient.getInstance().options.getSoundVolume(category) : 1.0f;
    }

    public SoundEvent getSoundEvent() {
        return soundEvent;
    }

    public float getUnmodifiedVolume() {
        return volume;
    }

    @Override
    public double getX() {
        return pos.getX();
    }

    @Override
    public double getY() {
        return pos.getY();
    }

    @Override
    public double getZ() {
        return pos.getZ();
    }
}
