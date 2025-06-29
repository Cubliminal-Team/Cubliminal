package net.limit.cubliminal.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.init.CubliminalStructures;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.world.chunk.BackroomsLevel;
import net.limit.cubliminal.world.room.Room;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.*;

public class GridAlignedStructure extends Structure {

    public static final MapCodec<GridAlignedStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GridAlignedStructure.configCodecBuilder(instance),
            StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding),
            StructureLiquidSettings.codec.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
            TagKey.codec(RegistryKeys.BLOCK).optionalFieldOf("block_filter").forGetter(structure -> structure.blockFilter),
            Room.CODEC.optionalFieldOf("room").forGetter(structure -> structure.room)
    ).apply(instance, GridAlignedStructure::new));

    private final RegistryEntry<StructurePool> startPool;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Type> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final DimensionPadding dimensionPadding;
    private final StructureLiquidSettings liquidSettings;
    private final Optional<TagKey<Block>> blockFilter;
    private final Optional<Room> room;

    public GridAlignedStructure(Config config, RegistryEntry<StructurePool> startPool, int size, HeightProvider startHeight,
                                Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter,
                                DimensionPadding dimensionPadding, StructureLiquidSettings liquidSettings,
                                Optional<TagKey<Block>> blockFilter, Optional<Room> room) {
        super(config);
        this.startPool = startPool;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
        this.blockFilter = blockFilter;
        this.room = room;
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        if (context.chunkGenerator() instanceof BackroomsLevel generator) {
            return GridAlignedStructureGenerator.generate(
                    context,
                    this.startPool,
                    this.size,
                    getBlockPos(context, this.dimensionPadding, generator.getLevel()),
                    false,
                    this.projectStartToHeightmap,
                    this.maxDistanceFromCenter,
                    StructurePoolAliasLookup.EMPTY,
                    this.dimensionPadding,
                    this.liquidSettings,
                    generator,
                    this.room
            );
        }
        return Optional.empty();
    }

    private static BlockPos getBlockPos(Context context, DimensionPadding dimensionPadding, Level level) {
        BlockPos startPos = context.chunkPos().getStartPos();
        List<BlockPos> possibilities = new ArrayList<>();
        for (int x = 0; x < 16; ++x) {
            for (int y = dimensionPadding.bottom(); y + startPos.getY() < Math.min(level.world_height - dimensionPadding.top(), level.layer_height * level.layer_count + dimensionPadding.bottom()); y += level.layer_height) {
                for (int z = 0; z < 16; ++z) {
                    BlockPos inPos = startPos.add(x, y, z);
                    if (Math.floorMod(inPos.getX(), level.spacing_x) == 0 && Math.floorMod(inPos.getZ(), level.spacing_z) == 0)
                        if (!possibilities.contains(inPos)) possibilities.add(inPos);
                }
            }
        }
        return possibilities.get(context.random().nextInt(possibilities.size()));
    }

    @Override
    public StructureType<?> getType() {
        return CubliminalStructures.GRID_ALIGNED_STRUCTURES;
    }
}
