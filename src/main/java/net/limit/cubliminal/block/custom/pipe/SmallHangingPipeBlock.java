package net.limit.cubliminal.block.custom.pipe;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.custom.template.AbstractHorizontalConnectingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class SmallHangingPipeBlock extends AbstractHorizontalConnectingBlock {
    public static final BooleanProperty ATTACHED = Properties.ATTACHED;
    public static final MapCodec<SmallHangingPipeBlock> CODEC = SmallHangingPipeBlock.createCodec(SmallHangingPipeBlock::new);
    public SmallHangingPipeBlock(Settings settings) {
        super(1.0f, 1.0f, 9.0f, 9.0f, 9.0f, 7.0f, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(ATTACHED, false));
    }

    @Override
    protected MapCodec<? extends AbstractHorizontalConnectingBlock> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockState blockState1 = blockView.getBlockState(blockPos.north());
        BlockState blockState2 = blockView.getBlockState(blockPos.south());
        BlockState blockState3 = blockView.getBlockState(blockPos.west());
        BlockState blockState4 = blockView.getBlockState(blockPos.east());
        BlockState[] blockStates = new BlockState[]{blockState1, blockState2, blockState3, blockState4};
        Boolean[] booleans = new Boolean[]{false, false, false, false, false};
        for (int i = 0; i < 4; ++i) {
            BlockState state = blockStates[i];
            if (this.connectsTo(state)) {
                booleans[i + 1] = true;
                booleans[0] = !state.get(ATTACHED);
            }
        }
        return this.getDefaultState()
                .with(ATTACHED, booleans[0])
                .with(NORTH, booleans[1])
                .with(SOUTH, booleans[2])
                .with(WEST, booleans[3])
                .with(EAST, booleans[4])
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction.getAxis().isHorizontal() ? state.with(FACING_PROPERTIES.get(direction), this.connectsTo(neighborState)) : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getOutlineShape(state, world, pos, context);
    }

    public final boolean connectsTo(BlockState state) {
        return state.getBlock() instanceof SmallHangingPipeBlock;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ATTACHED, NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}
