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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
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
    public static final BooleanProperty UP;
    public static final BooleanProperty DOWN;
    protected static final Map<Direction, BooleanProperty> FACING_PROPERTIES;

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
                .with(UP, this.connectsTo(world, blockPos, Direction.UP))
                .with(DOWN, this.connectsTo(world, blockPos, Direction.DOWN))
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
        return state.with(FACING_PROPERTIES.get(direction), this.connectsTo(neighborState));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN, FAN);
    }

    static {
        NORTH = ConnectingBlock.NORTH;
        EAST = ConnectingBlock.EAST;
        SOUTH = ConnectingBlock.SOUTH;
        WEST = ConnectingBlock.WEST;
        UP = ConnectingBlock.UP;
        DOWN = ConnectingBlock.DOWN;
        FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;
    }
}
