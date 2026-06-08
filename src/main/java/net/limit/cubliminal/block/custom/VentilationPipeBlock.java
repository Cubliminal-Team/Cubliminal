package net.limit.cubliminal.block.custom;

import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VentilationPipeBlock extends Block implements Waterloggable {

    public static final BooleanProperty FAN = BooleanProperty.of("fan");
    public static final BooleanProperty NORTH;
    public static final BooleanProperty EAST;
    public static final BooleanProperty SOUTH;
    public static final BooleanProperty WEST;
    public static final BooleanProperty WATERLOGGED;
    protected static final Map<Direction, BooleanProperty> FACING_PROPERTIES;

    private static final VoxelShape EMPTY = VoxelShapes.empty();
    private static final VoxelShape BASE_SHAPE = VoxelShapes.union(Block.createCuboidShape(0, 0, 0, 16, 2, 16), Block.createCuboidShape(0, 14, 0, 16, 16, 16));
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0, 0, 14, 16, 16, 16);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(14, 0, 0, 16, 16, 16);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0, 0, 0, 2, 16, 16);

    public VentilationPipeBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        Random random = Random.create(LimlibHelper.blockSeed(ctx.getBlockPos()));
        return this.getDefaultState()
                .with(NORTH, this.connectsTo(world, blockPos, Direction.NORTH))
                .with(SOUTH, this.connectsTo(world, blockPos, Direction.SOUTH))
                .with(WEST, this.connectsTo(world, blockPos, Direction.WEST))
                .with(EAST, this.connectsTo(world, blockPos, Direction.EAST))
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
                .with(FAN, random.nextDouble() < 0.1f);
    }

    protected boolean connectsTo(BlockState state) {
        return state.getBlock() instanceof VentilationPipeBlock;
    }

    protected boolean connectsTo(BlockView world, BlockPos pos, Direction offset) {
        return this.connectsTo(world.getBlockState(pos.offset(offset)));
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        boolean north = state.get(NORTH);
        boolean south = state.get(SOUTH);
        boolean east = state.get(EAST);
        boolean west = state.get(WEST);
        return VoxelShapes.union(
                BASE_SHAPE,
                north || (!east && !west) ? EMPTY : NORTH_SHAPE,
                south || (!east && !west) ? EMPTY : SOUTH_SHAPE,
                east || (west && !north && !south) ? EMPTY : EAST_SHAPE,
                west || (east && !north && !south) ? EMPTY : WEST_SHAPE
        );
    }

    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180 -> {
                return state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
            }
            case COUNTERCLOCKWISE_90 -> {
                return state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
            }
            case CLOCKWISE_90 -> {
                return state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
            }
            default -> {
                return state;
            }
        }
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT -> {
                return state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
            }
            case FRONT_BACK -> {
                return state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
            }
            default -> {
                return super.mirror(state, mirror);
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction.getAxis().isHorizontal()
                ? state.with(FACING_PROPERTIES.get(direction), this.connectsTo(neighborState))
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED, FAN);
    }

    static {
        NORTH = ConnectingBlock.NORTH;
        EAST = ConnectingBlock.EAST;
        SOUTH = ConnectingBlock.SOUTH;
        WEST = ConnectingBlock.WEST;
        WATERLOGGED = Properties.WATERLOGGED;
        FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter((entry) -> entry.getKey().getAxis().isHorizontal()).collect(Util.toMap());
    }
}
