package net.limit.cubliminal.world.structure;

import com.google.common.collect.Lists;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.world.chunk.BackroomsLevel;
import net.limit.cubliminal.world.room.Room;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Vec2i;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;
import java.util.Optional;

public class GridAlignedStructureGenerator {
    public static Optional<Structure.StructurePosition> generate(
            Structure.Context context,
            RegistryEntry<StructurePool> structurePool,
            int size,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Type> projectStartToHeightmap,
            int maxDistanceFromCenter,
            StructurePoolAliasLookup aliasLookup,
            DimensionPadding dimensionPadding,
            StructureLiquidSettings liquidSettings,
            BackroomsLevel level,
            Optional<Room> optionalRoom
    ) {
        DynamicRegistryManager dynamicRegistryManager = context.dynamicRegistryManager();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
        HeightLimitView heightLimitView = context.world();
        ChunkRandom chunkRandom = context.random();
        Registry<StructurePool> registry = dynamicRegistryManager.getOrThrow(RegistryKeys.TEMPLATE_POOL);
        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        StructurePool structurePool2 = structurePool.getKey()
                .flatMap(key -> registry.getOptionalValue(aliasLookup.lookup(key)))
                .orElse(structurePool.value());
        StructurePoolElement structurePoolElement = structurePool2.getRandomElement(chunkRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        } else {
            // This is where the structure is aligned
            Vec2i rotationOffset = rotationOffset(level.getLevel(), blockRotation);
            BlockPos blockPos = pos.subtract(new Vec3i(rotationOffset.getX(), 0, rotationOffset.getY()));
            // Create a room and invoke callback method
            optionalRoom.ifPresent(room -> level.saveRoom(pos, room.newInstance(Manipulation.of(blockRotation), false)));

            Vec3i vec3i = blockPos.subtract(pos);
            BlockPos blockPos2 = pos.subtract(vec3i);
            PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
                    structureTemplateManager,
                    structurePoolElement,
                    blockPos2,
                    structurePoolElement.getGroundLevelDelta(),
                    blockRotation,
                    structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, blockRotation),
                    liquidSettings
            );
            BlockBox blockBox = poolStructurePiece.getBoundingBox();
            int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
            int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
            int k;
            k = projectStartToHeightmap.map(type -> pos.getY() + chunkGenerator.getHeightOnGround(i, j, type, heightLimitView, context.noiseConfig())).orElseGet(blockPos2::getY);

            int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
            poolStructurePiece.translate(0, k - l, 0);
            int m = k + vec3i.getY();
            return Optional.of(
                    new Structure.StructurePosition(
                            new BlockPos(i, m, j),
                            collector -> {
                                List<PoolStructurePiece> list = Lists.newArrayList();
                                list.add(poolStructurePiece);
                                if (size > 0) {
                                    Box box = new Box(
                                            i - maxDistanceFromCenter,
                                            Math.max(m - maxDistanceFromCenter, heightLimitView.getBottomY() + dimensionPadding.bottom()),
                                            j - maxDistanceFromCenter,
                                            i + maxDistanceFromCenter + 1,
                                            Math.min(m + maxDistanceFromCenter + 1, heightLimitView.getTopYInclusive() + 1 - dimensionPadding.top()),
                                            j + maxDistanceFromCenter + 1
                                    );
                                    VoxelShape voxelShape = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST);
                                    StructurePoolBasedGenerator.generate(
                                            context.noiseConfig(),
                                            size,
                                            useExpansionHack,
                                            chunkGenerator,
                                            structureTemplateManager,
                                            heightLimitView,
                                            chunkRandom,
                                            registry,
                                            poolStructurePiece,
                                            list,
                                            voxelShape,
                                            aliasLookup,
                                            liquidSettings
                                    );
                                    list.forEach(collector::addPiece);
                                }
                            }
                    )
            );
        }
    }

    private static Vec2i rotationOffset(Level level, BlockRotation blockRotation) {
        return switch (blockRotation) {
            case NONE -> new Vec2i(0, 0);
            case CLOCKWISE_90 -> new Vec2i(level.spacing_x - 1, 0);
            case CLOCKWISE_180 -> new Vec2i(level.spacing_x - 1, level.spacing_z - 1);
            case COUNTERCLOCKWISE_90 -> new Vec2i(0, level.spacing_z - 1);
        };
    }
}
