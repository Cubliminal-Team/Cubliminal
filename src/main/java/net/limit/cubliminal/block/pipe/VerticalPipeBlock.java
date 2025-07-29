package net.limit.cubliminal.block.pipe;

import net.limit.cubliminal.block.state.pipeType.TypedPipeSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import static net.limit.cubliminal.block.pipe.ConnectorPipeBlock.CONNECTOR;

public class VerticalPipeBlock extends PipeBlock implements TypedPipeSupport {
    public static final VoxelShape SHAPE_WEST = Block.createCuboidShape(9.0, 0.0, 4.0, 16.0, 16.0, 12.0);
    public static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(4.0, 0.0, 9.0, 12.0, 16.0, 16.0);
    public static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0.0, 0.0, 4.0, 7.0, 16.0, 12.0);
    public static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 16.0, 7.0);

    public VerticalPipeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder
                .add(CONNECTOR);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)){
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE_NORTH;
        };
    }
}
