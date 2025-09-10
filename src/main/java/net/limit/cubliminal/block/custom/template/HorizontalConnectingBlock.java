package net.limit.cubliminal.block.custom.template;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
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

public abstract class HorizontalConnectingBlock extends Block implements Waterloggable {

    public static final BooleanProperty NORTH;
    public static final BooleanProperty EAST;
    public static final BooleanProperty SOUTH;
    public static final BooleanProperty WEST;
    public static final BooleanProperty WATERLOGGED;
    protected static final Map<Direction, BooleanProperty> FACING_PROPERTIES;
    protected final VoxelShape[] collisionShapes;
    protected final VoxelShape[] boundingShapes;
    private final Object2IntMap<BlockState> SHAPE_INDEX_CACHE = new Object2IntOpenHashMap<>();

    public HorizontalConnectingBlock(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, float bottomOffset, Settings settings) {
        super(settings);
        this.collisionShapes = this.createShapes(radius1, radius2, collisionHeight, bottomOffset, collisionHeight);
        this.boundingShapes = this.createShapes(radius1, radius2, boundingHeight1, bottomOffset, boundingHeight2);

        for (BlockState blockState : this.stateManager.getStates()) {
            this.getShapeIndex(blockState);
        }

    }

    protected abstract MapCodec<? extends HorizontalConnectingBlock> getCodec();

    protected abstract boolean connectsTo(BlockState state);

    protected boolean connectsTo(BlockView world, BlockPos pos, Direction offset) {
        return this.connectsTo(world.getBlockState(pos.offset(offset)));
    }

    protected VoxelShape[] createShapes(float radius1, float radius2, float height1, float offset1, float height2) {
        float f = 8.0f - radius1;
        float g = 8.0f + radius1;
        float h = 8.0f - radius2;
        float i = 8.0f + radius2;
        VoxelShape voxelShape = Block.createCuboidShape(f, offset1, f, g, height1, g);
        VoxelShape voxelShape2 = Block.createCuboidShape(h, offset1, 0.0, i, height2, i);
        VoxelShape voxelShape3 = Block.createCuboidShape(h, offset1, h, i, height2, 16.0);
        VoxelShape voxelShape4 = Block.createCuboidShape(0.0, offset1, h, i, height2, i);
        VoxelShape voxelShape5 = Block.createCuboidShape(h, offset1, h, 16.0, height2, i);
        VoxelShape voxelShape6 = VoxelShapes.union(voxelShape2, voxelShape5);
        VoxelShape voxelShape7 = VoxelShapes.union(voxelShape3, voxelShape4);
        VoxelShape[] voxelShapes = new VoxelShape[]{VoxelShapes.empty(), voxelShape3, voxelShape4, voxelShape7, voxelShape2, VoxelShapes.union(voxelShape3, voxelShape2), VoxelShapes.union(voxelShape4, voxelShape2), VoxelShapes.union(voxelShape7, voxelShape2), voxelShape5, VoxelShapes.union(voxelShape3, voxelShape5), VoxelShapes.union(voxelShape4, voxelShape5), VoxelShapes.union(voxelShape7, voxelShape5), voxelShape6, VoxelShapes.union(voxelShape3, voxelShape6), VoxelShapes.union(voxelShape4, voxelShape6), VoxelShapes.union(voxelShape7, voxelShape6)};

        for(int j = 0; j < 16; ++j) {
            voxelShapes[j] = VoxelShapes.union(voxelShape, voxelShapes[j]);
        }

        return voxelShapes;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return this.getDefaultState()
                .with(NORTH, this.connectsTo(world, blockPos, Direction.NORTH))
                .with(SOUTH, this.connectsTo(world, blockPos, Direction.SOUTH))
                .with(WEST, this.connectsTo(world, blockPos, Direction.WEST))
                .with(EAST, this.connectsTo(world, blockPos, Direction.EAST))
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.boundingShapes[this.getShapeIndex(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collisionShapes[this.getShapeIndex(state)];
    }

    private static int getDirectionMask(Direction dir) {
        return 1 << dir.getHorizontal();
    }

    protected int getShapeIndex(BlockState state) {
        return this.SHAPE_INDEX_CACHE.computeIfAbsent(state, statex -> {
            int i = 0;
            if (((BlockState) statex).get(NORTH)) {
                i |= getDirectionMask(Direction.NORTH);
            }

            if (((BlockState) statex).get(EAST)) {
                i |= getDirectionMask(Direction.EAST);
            }

            if (((BlockState) statex).get(SOUTH)) {
                i |= getDirectionMask(Direction.SOUTH);
            }

            if (((BlockState) statex).get(WEST)) {
                i |= getDirectionMask(Direction.WEST);
            }

            return i;
        });
    }

    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
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
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction.getAxis().isHorizontal()
                ? state.with(FACING_PROPERTIES.get(direction), this.connectsTo(neighborState))
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
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
