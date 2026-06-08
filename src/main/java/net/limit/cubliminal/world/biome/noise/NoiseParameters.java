package net.limit.cubliminal.world.biome.noise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A record that holds three noise settings and the {@code decayFactor} of a location within the world.
 * Note that the average noise value is determined by the dimension's global noise settings.
 * @param rarity Range [-8,8]; How rare is the location. Its value is given by a 2D noise that slightly fluctuates between noise value points
 * @param spacing Range (-∞,∞); Average number of noise value points separating two zones. It increases notably in grids located within the world
 * @param safety Range [0,5]; 5 - Survival difficulty. Smooth 2D noise to avoid inconsistencies. Greatly affected by the dimension's global noise settings
 * @param decayFactor Range [0,1]; Chance of players' mental fatigue to increase each tick
 * @param generate Whether the biome is able to generate
 */
public record NoiseParameters(double rarity, double spacing, double safety, double decayFactor, boolean generate) {
    public static final Codec<NoiseParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.doubleRange(-8, 8).fieldOf("rarity").forGetter(NoiseParameters::rarity),
            Codec.DOUBLE.fieldOf("spacing").forGetter(NoiseParameters::spacing),
            Codec.doubleRange(0, 5).fieldOf("safety").forGetter(NoiseParameters::safety),
            Codec.doubleRange(0, 1).fieldOf("decay_factor").forGetter(NoiseParameters::decayFactor),
            Codec.BOOL.optionalFieldOf("generate", true).forGetter(NoiseParameters::generate)
    ).apply(instance, instance.stable(NoiseParameters::new)));

    public static final NoiseParameters DEFAULT = new NoiseParameters(0, 0, 0, 0.3, false);
}
