package net.limit.cubliminal.world.connection;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;

import java.util.Optional;

public abstract class ConnectionPlacement {
    public static final Codec<ConnectionPlacement> TYPE_CODEC = ConnectionPlacementType.REGISTRY
            .getCodec()
            .dispatch("type", ConnectionPlacement::getType, ConnectionPlacementType::codec);

    private final Vec3i locateOffset;
    private final StructurePlacement.FrequencyReductionMethod frequencyReductionMethod;
    private final float frequency;
    private final int salt;
    private final Optional<ConnectionPlacement.ExclusionZone> exclusionZone;

    protected static <C extends ConnectionPlacement> Products.P5<RecordCodecBuilder.Mu<C>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<ConnectionPlacement.ExclusionZone>> buildCodec(RecordCodecBuilder.Instance<C> instance) {
        return instance.group(
                Vec3i.createOffsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(ConnectionPlacement::getLocateOffset),
                StructurePlacement.FrequencyReductionMethod.CODEC
                        .optionalFieldOf("frequency_reduction_method", StructurePlacement.FrequencyReductionMethod.DEFAULT)
                        .forGetter(ConnectionPlacement::getFrequencyReductionMethod),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(ConnectionPlacement::getFrequency),
                Codecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(ConnectionPlacement::getSalt),
                ConnectionPlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(ConnectionPlacement::getExclusionZone)
        );
    }

    protected ConnectionPlacement(Vec3i locateOffset, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod,
                                  float frequency, int salt, Optional<ConnectionPlacement.ExclusionZone> exclusionZone) {
        this.locateOffset = locateOffset;
        this.frequencyReductionMethod = frequencyReductionMethod;
        this.frequency = frequency;
        this.salt = salt;
        this.exclusionZone = exclusionZone;
    }

    protected Vec3i getLocateOffset() {
        return this.locateOffset;
    }

    protected StructurePlacement.FrequencyReductionMethod getFrequencyReductionMethod() {
        return this.frequencyReductionMethod;
    }

    protected float getFrequency() {
        return this.frequency;
    }

    protected int getSalt() {
        return this.salt;
    }

    protected Optional<ConnectionPlacement.ExclusionZone> getExclusionZone() {
        return this.exclusionZone;
    }

    public boolean shouldGenerate(long seed, int cellX, int cellZ) {
        return this.isStartCell(seed, cellX, cellZ)
                && this.applyFrequencyReduction(cellX, cellZ, seed)
                && this.applyExclusionZone(seed, cellX, cellZ);
    }

    public boolean applyFrequencyReduction(int cellX, int cellZ, long seed) {
        return !(this.frequency < 1.0F) || this.frequencyReductionMethod.shouldGenerate(seed, this.salt, cellX, cellZ, this.frequency);
    }

    public boolean applyExclusionZone(long seed, int centerCellX, int centerCellZ) {
        return this.exclusionZone.isEmpty() || !this.exclusionZone.get().shouldExclude(seed, centerCellX, centerCellZ);
    }

    public abstract boolean isStartCell(long seed, int cellX, int cellZ);

    public BlockPos getLocatePos(MazeComponent.Vec2i cellPos, int cellWidth, int cellHeight) {
        return new BlockPos(cellPos.x() * cellWidth, 0, cellPos.y() * cellHeight).add(this.getLocateOffset());
    }

    public abstract ConnectionPlacementType<?> getType();

    public record ExclusionZone(Identifier otherConnection, int cellCount) {
        public static final Codec<ConnectionPlacement.ExclusionZone> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Identifier.CODEC.fieldOf("other_connection").forGetter(ConnectionPlacement.ExclusionZone::otherConnection),
                                Codec.intRange(1, 16).fieldOf("cell_count").forGetter(ConnectionPlacement.ExclusionZone::cellCount)
                        ).apply(instance, ConnectionPlacement.ExclusionZone::new)
        );

        boolean shouldExclude(long seed, int centerCellX, int centerCellZ) {
            return this.canGenerate(seed, centerCellX, centerCellZ);
        }

        public boolean canGenerate(long seed, int centerCellX, int centerCellZ) {
            if (!ConnectionRegistry.containsId(otherConnection)) {
                throw new IllegalArgumentException("Can't fetch excluded connection: " + otherConnection);
            } else {
                ConnectionPlacement placement = ConnectionRegistry.getMapping(otherConnection).placement();
                for (int i = centerCellX - cellCount; i <= centerCellX + cellCount; i++) {
                    for (int j = centerCellZ - cellCount; j <= centerCellZ + cellCount; j++) {
                        if (placement.shouldGenerate(seed, i, j)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }
    }
}
