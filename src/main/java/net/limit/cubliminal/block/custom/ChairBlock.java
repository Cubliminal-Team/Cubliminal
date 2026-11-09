package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.custom.template.SeatBlock;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class ChairBlock extends SeatBlock implements Waterloggable {
    public static final MapCodec<ChairBlock> CODEC = ChairBlock.createCodec(ChairBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    protected static final VoxelShape EAST_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 11, 0, 2, 27, 16),
            Block.createCuboidShape(0, 0, 14, 2, 11, 16),
            Block.createCuboidShape(0, 0, 0, 2, 11, 2),
            Block.createCuboidShape(14, 0, 0, 16, 9, 2),
            Block.createCuboidShape(14, 0, 14, 16, 9, 16),
            Block.createCuboidShape(2, 9, 0, 16, 11, 16));
    protected static final VoxelShape WEST_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(14, 11, 0, 16, 27, 16),
            Block.createCuboidShape(14, 0, 0, 16, 11, 2),
            Block.createCuboidShape(14, 0, 14, 16, 11, 16),
            Block.createCuboidShape(0, 0, 14, 2, 9, 16),
            Block.createCuboidShape(0, 0, 0, 2, 9, 2),
            Block.createCuboidShape(0, 9, 0, 14, 11, 16));
    protected static final VoxelShape SOUTH_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 11, 0, 16, 27, 2),
            Block.createCuboidShape(0, 0, 0, 2, 11, 2),
            Block.createCuboidShape(14, 0, 0, 16, 11, 2),
            Block.createCuboidShape(14, 0, 14, 16, 9, 16),
            Block.createCuboidShape(0, 0, 14, 2, 9, 16),
            Block.createCuboidShape(0, 9, 2, 16, 11, 16));
    protected static final VoxelShape NORTH_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 11, 14, 16, 27, 16),
            Block.createCuboidShape(14, 0, 14, 16, 11, 16),
            Block.createCuboidShape(0, 0, 14, 2, 11, 16),
            Block.createCuboidShape(0, 0, 0, 2, 9, 2),
            Block.createCuboidShape(14, 0, 0, 16, 9, 2),
            Block.createCuboidShape(0, 9, 0, 16, 11, 14));

    public ChairBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public float getPassengerYaw(BlockState state, Entity entity) {
        return state.get(FACING).asRotation();
    }

    @Override
    public float seatHeight(BlockState state) {
        return 0.7f;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }
}
