package net.limit.cubliminal.world.biome.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.world.biome.noise.NoiseParameters;
import net.limit.cubliminal.world.biome.noise.RegistryNoisePreset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimplexBiomeSource extends BiomeSource implements LiminalBiomeSource {
    public static final MapCodec<SimplexBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            World.CODEC.fieldOf("dimension").forGetter(biomeSource -> biomeSource.world),
            Level.LEVEL_CODEC.fieldOf("level").forGetter(biomeSource -> biomeSource.level),
            Codec.FLOAT.optionalFieldOf("scale", 0.007f).forGetter(biomeSource -> biomeSource.scale)
    ).apply(instance, instance.stable(SimplexBiomeSource::new)));

    private final RegistryKey<World> world;
    private final RegistryNoisePreset noisePreset;
    private final Set<RegistryEntry<Biome>> levelBiomes;
    private final float scale;
    private final Level level;
    private boolean initialized;
    private SimplexNoiseSampler raritySampler;
    private final double rarityScale;
    private SimplexNoiseSampler spacingSampler;
    private final double maxSpacing;
    private SimplexNoiseSampler safetySampler;
    private final double levelSafety;

    public SimplexBiomeSource(RegistryKey<World> world, Level level, float scale) {
        this.world = world;
        this.noisePreset = RegistryNoisePreset.getPreset(world);
        this.levelBiomes = this.noisePreset.biomes().entrySet().stream()
                .filter(entry -> entry.getValue().generate())
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        this.level = level;
        this.scale = scale;
        this.rarityScale = this.noisePreset.globalSettings().rarity();
        this.maxSpacing = this.noisePreset.globalSettings().spacing();
        this.levelSafety = this.noisePreset.globalSettings().safety();
    }

    private void initSamplers() {
        if (!initialized) {
            Random random = Random.create(Cubliminal.SERVER.getSeed());
            this.raritySampler = new SimplexNoiseSampler(new ChunkRandom(random.split()));
            this.spacingSampler = new SimplexNoiseSampler(new ChunkRandom(random));
            this.safetySampler = new SimplexNoiseSampler(new ChunkRandom(random));
            initialized = true;
        }
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        return this.calcBiome(BiomeCoords.toBlock(x), BiomeCoords.toBlock(y), BiomeCoords.toBlock(z));
    }

    @Override
    public RegistryEntry<Biome> calcBiome(BlockPos pos) {
        return this.calcBiome(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public RegistryEntry<Biome> calcBiome(int blockX, int blockY, int blockZ) {
        this.initSamplers();
        int x = BiomeCoords.fromBlock(blockX);
        int z = BiomeCoords.fromBlock(blockZ);
        double dx = x * this.scale;
        double dz = z * this.scale;

        double rarityValue = this.sampleRarity(dx, dz);
        double spacingValue = this.sampleSpacing(x, z, dx, dz);
        double safetyValue = this.sampleSafety(dx, dz);

        return this.getBiomeReference(rarityValue, spacingValue, safetyValue);
    }

    private double sampleRarity(double dx, double dz) {
        double rarityValue = this.raritySampler.sample(dx * this.rarityScale, dz * this.rarityScale);
        return 8 * rarityValue;
    }

    private double sampleSpacing(double x, double z, double dx, double dz) {
        double spacingValue = (this.spacingSampler.sample(dx, dz) + 1) / 2;
        double mx = (maxLoc(x / 4) + maxLoc(z / 4)) / 2;
        return spacingValue * mx;
    }

    private double sampleSafety(double dx, double dz) {
        return this.levelSafety - Math.pow(this.safetySampler.sample(dx, dz), 3);
    }

    private double maxLoc(double d) {
        return this.maxSpacing * Math.pow(Math.cos((Math.PI / this.maxSpacing) * d), 5);
    }

    public RegistryEntry<Biome> getBiomeReference(double rarityValue, double spacingValue, double safetyValue) {
        double smallestDifference = Double.MAX_VALUE;
        RegistryEntry<Biome> chosenBiome = null;

        for (RegistryEntry<Biome> biome : this.levelBiomes) {
            NoiseParameters parameters = this.noisePreset.noiseParameters(biome);
            double distance = this.distanceTo(parameters, rarityValue, spacingValue, safetyValue);

            if (distance < smallestDifference) {
                smallestDifference = distance;
                chosenBiome = biome;
            }
        }
        return chosenBiome;
    }

    public double distanceTo(NoiseParameters parameters, double rarityValue, double spacingValue, double safetyValue) {
        double rarity = (parameters.rarity() - rarityValue) / 8;
        double spacing = (parameters.spacing() - spacingValue) / this.maxSpacing;
        double safety = (parameters.safety() - safetyValue) / this.levelSafety;
        return Math.sqrt(rarity * rarity + spacing * spacing + safety * safety);
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    protected MapCodec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    protected Stream<RegistryEntry<Biome>> biomeStream() {
        return this.levelBiomes.stream();
    }

    // I had been half blind all this time...
    @Override
    public void addDebugInfo(List<String> info, BlockPos pos, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
        this.initSamplers();
        int x = BiomeCoords.fromBlock(pos.getX());
        int z = BiomeCoords.fromBlock(pos.getZ());
        double dx = x * this.scale;
        double dz = z * this.scale;
        double r = this.sampleRarity(dx, dz);
        double sp = this.sampleSpacing(x, z, dx, dz);
        double sf = this.sampleSafety(dx, dz);
        info.add("Biome builder R: " + r + " SP: " + sp + " SF: " + sf);
    }
}
