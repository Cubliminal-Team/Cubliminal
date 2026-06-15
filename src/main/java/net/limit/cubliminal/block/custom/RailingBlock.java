package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.state.ConnectionMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class RailingBlock extends HorizontalFacingBlock implements Waterloggable {

    public static final MapCodec<RailingBlock> CODEC = RailingBlock.createCodec(RailingBlock::new);

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<StairShape> SHAPE = Properties.STAIR_SHAPE;
    public static final EnumProperty<ConnectionMode> LEFT = EnumProperty.of("left", ConnectionMode.class);
    public static final EnumProperty<ConnectionMode> RIGHT = EnumProperty.of("right", ConnectionMode.class);

    public RailingBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(SHAPE, StairShape.STRAIGHT)
                .with(LEFT, ConnectionMode.FLAT)
                .with(RIGHT, ConnectionMode.FLAT)
                .with(WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(blockPos);
        BlockState blockState = this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        return getShape(blockState, ctx.getWorld(), blockPos);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        StairShape shape = state.get(SHAPE);
        Direction facing = state.get(FACING);

        BlockPos left = state.get(LEFT).offset(pos).offset(shape == StairShape.INNER_LEFT ? facing.getOpposite() : facing.rotateYCounterclockwise());
        BlockState leftState = world.getBlockState(left);
        BlockPos right = state.get(RIGHT).offset(pos).offset(shape == StairShape.INNER_RIGHT ? facing.getOpposite() : facing.rotateYClockwise());
        BlockState rightState = world.getBlockState(right);

        if (isRailing(leftState)) {
            world.setBlockState(left, getShape(leftState, world, left));
        }
        if (isRailing(rightState)) {
            world.setBlockState(right, getShape(rightState, world, right));
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            StairShape shape = state.get(SHAPE);
            Direction facing = state.get(FACING);

            BlockPos left = state.get(LEFT).offset(pos).offset(shape == StairShape.INNER_LEFT ? facing.getOpposite() : facing.rotateYCounterclockwise());
            BlockState leftState = world.getBlockState(left);
            BlockPos right = state.get(RIGHT).offset(pos).offset(shape == StairShape.INNER_RIGHT ? facing.getOpposite() : facing.rotateYClockwise());
            BlockState rightState = world.getBlockState(right);

            if (isRailing(leftState)) {
                world.setBlockState(left, getShape(leftState, world, left));
            }
            if (isRailing(rightState)) {
                world.setBlockState(right, getShape(rightState, world, right));
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    private static BlockState getShape(BlockState state, BlockView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        for (ConnectionMode mode : ConnectionMode.values()) {
            BlockPos column = mode.offset(pos);

            BlockState front = world.getBlockState(column.offset(direction));
            if (isRailing(front)) {
                Direction frontFacing = front.get(FACING);
                if (frontFacing.getAxis() != state.get(FACING).getAxis() && isDifferentOrientation(state, world, pos, frontFacing.getOpposite())) {
                    if (frontFacing == direction.rotateYCounterclockwise()) {
                        return adjSlope(pos.offset(frontFacing), state, LEFT, world)
                                .with(SHAPE, StairShape.OUTER_LEFT)
                                .with(RIGHT, mode);
                    }

                    return adjSlope(pos.offset(frontFacing), state, RIGHT, world)
                            .with(SHAPE, StairShape.OUTER_RIGHT)
                            .with(LEFT, mode);
                }
            }

            BlockState back = world.getBlockState(column.offset(direction.getOpposite()));
            if (isRailing(back)) {
                Direction backFacing = back.get(FACING);
                if (backFacing.getAxis() != state.get(FACING).getAxis() && isDifferentOrientation(state, world, pos, backFacing)) {
                    if (backFacing == direction.rotateYCounterclockwise()) {
                        return adjSlope(pos.offset(backFacing.getOpposite()), state, RIGHT, world)
                                .with(SHAPE, StairShape.INNER_LEFT)
                                .with(LEFT, mode);
                    }

                    return adjSlope(pos.offset(backFacing.getOpposite()), state, LEFT, world)
                            .with(SHAPE, StairShape.INNER_RIGHT)
                            .with(RIGHT, mode);
                }
            }
        }

        BlockState leftAdjusted = adjSlope(pos.offset(direction.rotateYCounterclockwise()), state, LEFT, world);
        return adjSlope(pos.offset(direction.rotateYClockwise()), leftAdjusted, RIGHT, world).with(SHAPE, StairShape.STRAIGHT);
    }

    private static BlockState adjSlope(BlockPos column, BlockState state, EnumProperty<ConnectionMode> side, BlockView world) {
        for (ConnectionMode mode : ConnectionMode.values()) {
            BlockPos adjPos = mode.offset(column);
            BlockState adj = world.getBlockState(adjPos);
            if (isRailing(adj)) {
                return state.with(side, mode);
            }
        }

        return state.with(side, ConnectionMode.FLAT);
    }

    private static boolean isRailing(BlockState state) {
        return state.getBlock() instanceof RailingBlock;
    }

    private static boolean isDifferentOrientation(BlockState state, BlockView world, BlockPos pos, Direction dir) {
        for (ConnectionMode mode : ConnectionMode.values()) {
            BlockState blockState = world.getBlockState(mode.offset(pos).offset(dir));
            if (isRailing(blockState) && blockState.get(FACING) == state.get(FACING)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        Direction direction = state.get(FACING);
        StairShape shape = state.get(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT:
                if (direction.getAxis() == Direction.Axis.Z) {
                    return switch (shape) {
                        case INNER_LEFT ->
                                state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                        case INNER_RIGHT ->
                                state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                        case OUTER_LEFT ->
                                state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                        case OUTER_RIGHT ->
                                state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                        default -> state.rotate(BlockRotation.CLOCKWISE_180);
                    };
                }
                break;
            case FRONT_BACK:
                if (direction.getAxis() == Direction.Axis.X) {
                    return switch (shape) {
                        case INNER_LEFT -> state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                        case INNER_RIGHT ->
                                state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                        case OUTER_LEFT ->
                                state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                        case OUTER_RIGHT ->
                                state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                        case STRAIGHT -> state.rotate(BlockRotation.CLOCKWISE_180);
                    };
                }
        }

        return super.mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE, LEFT, RIGHT, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}
