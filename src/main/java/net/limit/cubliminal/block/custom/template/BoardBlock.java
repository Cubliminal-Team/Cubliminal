package net.limit.cubliminal.block.custom.template;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BoardBlock extends WallMountedBlock implements Waterloggable {
    public static final MapCodec<BoardBlock> CODEC = BoardBlock.createCodec(BoardBlock::new);

    protected boolean needsAttachment = false;
    protected VoxelShape CEILING_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape FLOOR_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape NORTH_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape SOUTH_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape WEST_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape EAST_SHAPE = VoxelShapes.fullCube();
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public BoardBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    public BoardBlock needsAttachment() {
        this.needsAttachment = true;
        return this;
    }

    public BoardBlock voxelShapes(double thickness) {
        this.CEILING_SHAPE = Block.createCuboidShape(0, 16 - thickness, 0, 16, 16, 16);
        this.FLOOR_SHAPE = Block.createCuboidShape(0, 0, 0, 16, thickness, 16);
        this.WEST_SHAPE = Block.createCuboidShape(16 - thickness, 0, 0, 16, 16, 16);
        this.EAST_SHAPE = Block.createCuboidShape(0, 0, 0, thickness, 16, 16);
        this.SOUTH_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16, thickness);
        this.NORTH_SHAPE = Block.createCuboidShape(0, 0, 16 - thickness, 16, 16, 16);
        return this;
    }

    @Override
    protected MapCodec<? extends WallMountedBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return !needsAttachment || super.canPlaceAt(state, world, pos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACE)) {
            case FLOOR -> FLOOR_SHAPE;
            case WALL -> switch (state.get(FACING)) {
                case EAST -> EAST_SHAPE;
                case WEST -> WEST_SHAPE;
                case SOUTH -> SOUTH_SHAPE;
                default -> NORTH_SHAPE;
            };
            default -> CEILING_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state != null) {
            FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
            return state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }

        return state;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, WATERLOGGED);
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }
}
