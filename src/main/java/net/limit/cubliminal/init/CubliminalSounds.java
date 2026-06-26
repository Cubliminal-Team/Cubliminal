package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
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

/**
 * This class holds sound event related registry utilities and all the custom server side sound event registries.
 */
public class CubliminalSounds implements Initer {
	private static SoundEvent register(String id) {
		return Registry.register(Registries.SOUND_EVENT, Cubliminal.id(id), SoundEvent.of(Cubliminal.id(id)));
	}

	private static RegistryEntry.Reference<SoundEvent> registerReference(String id) {
		return Registry.registerReference(Registries.SOUND_EVENT, Cubliminal.id(id), SoundEvent.of(Cubliminal.id(id)));
	}
    public static final RegistryEntry.Reference<SoundEvent> AMBIENT_LEVEL_O = registerReference("ambient.lvl0");
    public static final RegistryEntry.Reference<SoundEvent> AMBIENT_PILLARS_LEVEL_0 = registerReference("ambient.lvl0.pillars");
    public static final RegistryEntry.Reference<SoundEvent> AMBIENT_REDROOMS = registerReference("ambient.redrooms");
	public static final RegistryEntry.Reference<SoundEvent> POWER_DOWN = registerReference("ambient.power_down");
	public static final RegistryEntry.Reference<SoundEvent> POWER_ON = registerReference("ambient.power_on");
	public static final RegistryEntry.Reference<SoundEvent> HEARTBEAT = registerReference("heartbeat");
	public static final RegistryEntry.Reference<SoundEvent> NOCLIPPING = registerReference("noclipping");
	public static final RegistryEntry.Reference<SoundEvent> WALL_CLIPPING = registerReference("wall_clipping");
	public static final RegistryEntry.Reference<SoundEvent> OPEN_SINK = registerReference("block.sink_open");
	public static final RegistryEntry.Reference<SoundEvent> SINK_AMBIENT = registerReference("block.sink.ambient");
	public static final RegistryEntry.Reference<SoundEvent> FLUX_CAPACITOR = registerReference("block.flux_capacitor");
	public static final RegistryEntry.Reference<SoundEvent> SHORT_CIRCUIT = registerReference("block.short_circuit");
	public static final RegistryEntry.Reference<SoundEvent> BLUE_HORIZON = registerReference("music.blue_horizon");

	public static final SoundEvent PAPER_BREAK = register("block.paper_break");
	public static final SoundEvent PAPER_STEP = register("block.paper_step");
	public static final SoundEvent PAPER_PLACE = register("block.paper_place");
	public static final SoundEvent PAPER_HIT = register("block.paper_hit");
	public static final SoundEvent PAPER_FALL = register("block.paper_fall");

	public static final SoundEvent SKIN_STEALER_ANGRY = register("entity.skin_stealer.angry");


	/**
	 * Sends a packet to a specific client for a positional sound event to be played.
	 * @param target The player that will be sent the packet.
	 * @param sound The sound event to be played.
	 * @param category Sound category.
	 * @param x X pos.
	 * @param y Y pos.
	 * @param z Z pos.
	 * @param volume Volume.
	 * @param pitch Pitch.
	 * @param seed A random seed used for variation, not important.
	 */
	public static void clientPlaySoundSingle(ServerPlayerEntity target, RegistryEntry<SoundEvent> sound, SoundCategory category, double x, double y, double z, float volume, float pitch, long seed) {
		PlaySoundS2CPacket playSoundS2CPacket = new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch, seed);
		target.networkHandler.sendPacket(playSoundS2CPacket);
	}

	/**
	 * Sends a packet to multiple clients for a positional sound event to be played.
	 * @param targets All the players that will be sent the packet.
	 * @param sound The sound event to be played.
	 * @param category Sound category.
	 * @param x X pos.
	 * @param y Y pos.
	 * @param z Z pos.
	 * @param volume Volume.
	 * @param pitch Pitch.
	 * @param seed A random seed used for variation, not important.
	 */
	public static void clientPlaySoundCollection(Collection<ServerPlayerEntity> targets, RegistryEntry<SoundEvent> sound, SoundCategory category, double x, double y, double z, float volume, float pitch, long seed) {
		PlaySoundS2CPacket playSoundS2CPacket = new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch, seed);
		for (ServerPlayerEntity serverPlayerEntity : targets) {
			serverPlayerEntity.networkHandler.sendPacket(playSoundS2CPacket);
		}
	}

	/**
	 * Sends a packet to multiple clients to stop a sound event that is being played.
	 * @param targets All the players that will be sent the packet.
	 * @param category Sound category.
	 * @param sound The identifier of the desired sound.
	 */
	public static void clientStopSoundCollection(Collection<ServerPlayerEntity> targets, @Nullable SoundCategory category, @Nullable Identifier sound) {
		StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(sound, category);
		for (ServerPlayerEntity serverPlayerEntity : targets) {
			serverPlayerEntity.networkHandler.sendPacket(stopSoundS2CPacket);
		}
	}

	public static void blockPlaySound(World world, BlockPos pos, SoundEvent sound) {
		world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
	}

}
