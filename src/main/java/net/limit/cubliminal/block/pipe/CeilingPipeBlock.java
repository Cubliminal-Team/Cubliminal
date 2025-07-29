package net.limit.cubliminal.block.pipe;

import net.limit.cubliminal.block.state.pipeType.CeilingPipeTypes;
import net.limit.cubliminal.block.state.pipeType.TypedPipeSupport;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Objects;

public class CeilingPipeBlock extends PipeBlock implements TypedPipeSupport {
    public static final EnumProperty<CeilingPipeTypes> TYPE = TypedPipeSupport.build(CeilingPipeTypes.class);
    public CeilingPipeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TYPE);
    }

    /*@Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        BlockState up = world.getBlockState(pos.up());
        BlockState down = world.getBlockState(pos.down());
        if (down.isOf(BackroomBlocks.VERTICAL_PIPE)) {
            return state.with(CeilingPipeBlock.CONNECTION, down.get(VerticalPipeBlock.FACING));
        }
        if (up.isOf(BackroomBlocks.VERTICAL_PIPE)) {
            return state.with(CeilingPipeBlock.CONNECTION, up.get(VerticalPipeBlock.FACING));
        }
        return state.with(CeilingPipeBlock.CONNECTION, Direction.UP);
    }*/

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (neighborState.isOf(CubliminalBlocks.VERTICAL_PIPE)){
            if (Objects.equals(neighborPos, pos.up()) || Objects.equals(neighborPos, pos.down())){
                Direction neighborDirection = neighborState.get(VerticalPipeBlock.FACING);
                return state.with(CeilingPipeBlock.TYPE, CeilingPipeTypes.ONE_LAYER_VERTICAL_PIPE_CONNECTION).with(CeilingPipeBlock.FACING, neighborDirection.getOpposite());
            }
        } else if (neighborState.isOf(Blocks.AIR)){
            if (!world.getBlockState(pos.up()).isOf(CubliminalBlocks.VERTICAL_PIPE) && !world.getBlockState(pos.down()).isOf(CubliminalBlocks.VERTICAL_PIPE)){
                return state.with(CeilingPipeBlock.TYPE, CeilingPipeTypes.ONE_LAYER_STRAIGHT);
            }
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }
}
