package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class LargeBeamBlock extends WallMountedBlock {

    public static final MapCodec<LargeBeamBlock> CODEC = LargeBeamBlock.createCodec(LargeBeamBlock::new);

    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0, 0, 2, 16, 16, 14);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(2, 0, 0, 14, 16, 16);

    public LargeBeamBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }
}
