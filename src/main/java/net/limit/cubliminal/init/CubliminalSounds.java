package net.limit.cubliminal.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.sound.ConditionedSoundInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


public class CubliminalSounds {
	private static SoundEvent register(String id) {
		return Registry.register(Registries.SOUND_EVENT, Cubliminal.id(id), SoundEvent.of(Cubliminal.id(id)));
	}

	private static RegistryEntry.Reference<SoundEvent> registerReference(String id) {
		return Registry.registerReference(Registries.SOUND_EVENT, Cubliminal.id(id), SoundEvent.of(Cubliminal.id(id)));
	}
    public static final RegistryEntry.Reference<SoundEvent> AMBIENT_LEVEL_O = registerReference("ambient.lvl0");
    public static final RegistryEntry.Reference<SoundEvent> AMBIENT_PILLARS_LEVEL_0 = registerReference("ambient.lvl0.pillars");
    public static final RegistryEntry.Reference<SoundEvent> AMBIENT_REDROOMS = registerReference("ambient.redrooms");
	public static final RegistryEntry.Reference<SoundEvent> HEARTBEAT = registerReference("heartbeat");
	public static final RegistryEntry.Reference<SoundEvent> NOCLIPPING = registerReference("noclipping");
	public static final RegistryEntry.Reference<SoundEvent> WALL_CLIPPING = registerReference("wall_clipping");
	public static final RegistryEntry.Reference<SoundEvent> OPEN_SINK = registerReference("block.sink_open");
	public static final RegistryEntry.Reference<SoundEvent> SINK_AMBIENT = registerReference("block.sink.ambient");
	public static final RegistryEntry.Reference<SoundEvent> FLUX_CAPACITOR = registerReference("block.flux_capacitor");


	@Environment(EnvType.CLIENT)
	public static void playSound(ConditionedSoundInstance soundInstance) {
		playSoundAt(soundInstance, soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	public static void playSoundAtBlock(ConditionedSoundInstance soundInstance) {
		playSoundAt(soundInstance, soundInstance.getX() + 0.5, soundInstance.getY() + 0.5, soundInstance.getZ() + 0.5);
	}

	@Environment(EnvType.CLIENT)
	public static void stopSound(SoundInstance soundInstance) {
		MinecraftClient.getInstance().getSoundManager().stop(soundInstance);
	}

	public static void clientPlaySoundSingle(ServerPlayerEntity target, RegistryEntry<SoundEvent> sound, SoundCategory category, double x, double y, double z, float volume, float pitch, long seed) {
		PlaySoundS2CPacket playSoundS2CPacket = new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch, seed);
		target.networkHandler.sendPacket(playSoundS2CPacket);
	}

	public static void clientPlaySoundCollection(Collection<ServerPlayerEntity> targets, RegistryEntry<SoundEvent> sound, SoundCategory category, double x, double y, double z, float volume, float pitch, long seed) {
		PlaySoundS2CPacket playSoundS2CPacket = new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch, seed);
		for (ServerPlayerEntity serverPlayerEntity : targets) {
			serverPlayerEntity.networkHandler.sendPacket(playSoundS2CPacket);
		}
	}

	public static void clientStopSoundCollection(Collection<ServerPlayerEntity> targets, @Nullable SoundCategory category, @Nullable Identifier sound) {
		StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(sound, category);
		for (ServerPlayerEntity serverPlayerEntity : targets) {
			serverPlayerEntity.networkHandler.sendPacket(stopSoundS2CPacket);
		}
	}

	public static void blockPlaySound(World world, BlockPos pos, SoundEvent sound) {
		world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
	}

    public static void init() {
    }
}
