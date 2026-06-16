package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class MoldSproutsBlock extends PlantBlock implements Fertilizable {

    public static final MapCodec<MoldSproutsBlock> CODEC = MoldSproutsBlock.createCodec(MoldSproutsBlock::new);
    public static final int MAX_GROWTH_LIGHT_LVL = 11;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
    public static final BooleanProperty DRY = BooleanProperty.of("dry");

    public MoldSproutsBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(DRY, false));
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return !state.get(DRY);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 11.0, 14.0);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(20) == 0) {
            // If it's too bright, the sprouts will die
            if (world.getBaseLightLevel(pos, 0) > MAX_GROWTH_LIGHT_LVL) {
                world.setBlockState(pos, state.with(DRY, true), Block.NOTIFY_LISTENERS);
                return;
            }

            // If not, try to look for a dark spot to reproduce
            int i = 5;

            for (BlockPos blockPos : BlockPos.iterate(pos.add(-3, -1, -3), pos.add(3, 1, 3))) {
                if (world.getBlockState(blockPos).isOf(this) && --i <= 0) {
                    return;
                }
            }

            BlockPos blockPos2 = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

            for (int k = 0; k < 4; k++) {
                if (world.isAir(blockPos2) && state.canPlaceAt(world, blockPos2) && world.getBaseLightLevel(blockPos2, 0) <= MAX_GROWTH_LIGHT_LVL) {
                    pos = blockPos2;
                }

                blockPos2 = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            }

            if (world.isAir(blockPos2) && state.canPlaceAt(world, blockPos2) && world.getBaseLightLevel(blockPos2, 0) <= MAX_GROWTH_LIGHT_LVL) {
                world.setBlockState(blockPos2, state, Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOpaqueFullCube();
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {

    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DRY);
    }
}
