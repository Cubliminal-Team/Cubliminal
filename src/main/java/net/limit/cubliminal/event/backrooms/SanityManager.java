package net.limit.cubliminal.event.backrooms;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.init.CubliminalEffects;
import net.limit.cubliminal.networking.s2c.SanitySyncPayload;
import net.limit.cubliminal.world.biome.noise.RegistryNoisePreset;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class SanityManager {

	private int sanity = 100;
	private int mentalFatigue = 0;

	public void update(ServerPlayerEntity player) {
		Random random = player.getRandom();
		int bound = player.isSprinting() ? 20 : 16;

		// days without sleeping and light level
		World world = player.getWorld();
		int daysSinceRest = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)) / 24000;
		if (world.getLightLevel(player.getBlockPos()) < 3 || daysSinceRest > 2) {
			bound -= 3;
		}

		// difficulty
		Difficulty difficulty = world.getDifficulty();
		if (difficulty.equals(Difficulty.HARD)) {
			bound -= 3;
		} else if (difficulty.equals(Difficulty.EASY)) {
			bound += 3;
		}
		bound *= 2;

		// biomes that drop sanity even faster
		RegistryEntry<Biome> biome = world.getBiome(player.getBlockPos());
		RegistryNoisePreset noisePreset = RegistryNoisePreset.getPreset(world.getRegistryKey());
		double globalDecayFactor = noisePreset.globalSettings().decayFactor();
		double decayFactor = noisePreset.noiseParameters(biome).decayFactor();

		// decrease sanity if needed
		double result = random.nextInt(bound) * (1 - decayFactor);
		if (result * result < bound * globalDecayFactor) {
			this.decrease(player, random);
		}

		ServerPlayNetworking.send(player, new SanitySyncPayload(this.sanity));
	}

	public void decrease(ServerPlayerEntity player, Random random) {
		++this.mentalFatigue;
		if (this.mentalFatigue > 10) {
			this.mentalFatigue = 0;
			this.sanity = Math.max(this.sanity - 1, 0);
			if (this.sanity < 20) {
				if (this.sanity < 5) {
					player.addStatusEffect(new StatusEffectInstance(CubliminalEffects.PARANOIA,
							400, 0, true, false, true));
				}
				switch (random.nextInt(5 + this.sanity)) {
					case 1:
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60,
								1, false, false, false));
					case 2:
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200,
								0, false, false, false));
					case 3:
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200,
								0, false, false, false));
					default:
						break;
				}
			}
		}
	}

	public void readNbt(NbtCompound nbt) {
		if (nbt.contains("Sanity", NbtElement.NUMBER_TYPE)) {
			this.sanity = nbt.getInt("Sanity");
			this.mentalFatigue = nbt.getInt("MentalFatigue");
		}
	}

	public void writeNbt(NbtCompound nbt) {
		nbt.putInt("Sanity", this.sanity);
		nbt.putInt("MentalFatigue", this.mentalFatigue);
	}

	public int getSanity() {
		return this.sanity;
	}

	public void setSanity(int sanity) {
		this.sanity = sanity;
	}

	public void resetTimer() {
		this.sanity = 100;
		this.mentalFatigue = 0;
	}
}
