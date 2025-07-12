package net.limit.cubliminal.world.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.placement.*;

import java.util.Optional;

public class RandomSpreadConnectionPlacement extends ConnectionPlacement {
    public static final MapCodec<RandomSpreadConnectionPlacement> CODEC = RecordCodecBuilder.<RandomSpreadConnectionPlacement>mapCodec(
                    instance -> buildCodec(instance).and(
                                    instance.group(
                                            Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadConnectionPlacement::getSpacing),
                                            Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadConnectionPlacement::getSeparation),
                                            SpreadType.CODEC.optionalFieldOf("spread_type", SpreadType.LINEAR).forGetter(RandomSpreadConnectionPlacement::getSpreadType)
                                    )).apply(instance, RandomSpreadConnectionPlacement::new))
            .validate(RandomSpreadConnectionPlacement::validate);
    private final int spacing;
    private final int separation;
    private final SpreadType spreadType;

    private static DataResult<RandomSpreadConnectionPlacement> validate(RandomSpreadConnectionPlacement structurePlacement) {
        return structurePlacement.spacing <= structurePlacement.separation
                ? DataResult.error(() -> "Spacing has to be larger than separation")
                : DataResult.success(structurePlacement);
    }

    public RandomSpreadConnectionPlacement(
            Vec3i locateOffset,
            StructurePlacement.FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ConnectionPlacement.ExclusionZone> exclusionZone,
            int spacing,
            int separation,
            SpreadType spreadType
    ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.spacing = spacing;
        this.separation = separation;
        this.spreadType = spreadType;
    }

    public RandomSpreadConnectionPlacement(int spacing, int separation, SpreadType spreadType, int salt) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, salt, Optional.empty(), spacing, separation, spreadType);
    }

    public int getSpacing() {
        return this.spacing;
    }

    public int getSeparation() {
        return this.separation;
    }

    public SpreadType getSpreadType() {
        return this.spreadType;
    }

    public MazeComponent.Vec2i getStartPos(long seed, int cellX, int cellZ) {
        int i = Math.floorDiv(cellX, this.spacing);
        int j = Math.floorDiv(cellZ, this.spacing);
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
        chunkRandom.setRegionSeed(seed, i, j, this.getSalt());
        int k = this.spacing - this.separation;
        int l = this.spreadType.get(chunkRandom, k);
        int m = this.spreadType.get(chunkRandom, k);
        return new MazeComponent.Vec2i(i * this.spacing + l, j * this.spacing + m);
    }

    @Override
    public boolean isStartCell(long seed, int cellX, int cellZ) {
        MazeComponent.Vec2i cellPos = this.getStartPos(seed, cellX, cellZ);
        return cellPos.x() == cellX && cellPos.y() == cellZ;
    }

    @Override
    public ConnectionPlacementType<?> getType() {
        return ConnectionPlacementType.RANDOM_SPREAD;
    }
}
