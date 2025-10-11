package net.limit.cubliminal.block.custom.pipe;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.state.pipeType.CeilingPipeTypes;
import net.limit.cubliminal.block.state.pipeType.TypedPipeSupport;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class CeilingPipeBlock extends PipeBlock implements TypedPipeSupport {
    public static MapCodec<CeilingPipeBlock> CODEC = CeilingPipeBlock.createCodec(CeilingPipeBlock::new);

    public static final VoxelShape ONE_LAYER = Block.createCuboidShape(0, 0, 0, 16, 5, 16);
    public static final VoxelShape TWO_LAYER = Block.createCuboidShape(0, 0, 0, 16, 10, 16);
    public static final EnumProperty<CeilingPipeTypes> TYPE = TypedPipeSupport.build(CeilingPipeTypes.class);

    public CeilingPipeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TYPE);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        BlockState down = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        if (down.isOf(CubliminalBlocks.VERTICAL_PIPE)) {
            return state
                    .with(TYPE, CeilingPipeTypes.ONE_LAYER_VERTICAL_PIPE_CONNECTION)
                    .with(FACING, down.get(FACING).getOpposite());
        }
        BlockState up = ctx.getWorld().getBlockState(ctx.getBlockPos().up());
        if (up.isOf(CubliminalBlocks.VERTICAL_PIPE)) {
            return state
                    .with(TYPE, CeilingPipeTypes.ONE_LAYER_VERTICAL_PIPE_CONNECTION)
                    .with(FACING, up.get(FACING).getOpposite());
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        BlockState down = world.getBlockState(pos.down());
        if (down.isOf(CubliminalBlocks.VERTICAL_PIPE)) {
            return state
                    .with(TYPE, CeilingPipeTypes.ONE_LAYER_VERTICAL_PIPE_CONNECTION)
                    .with(FACING, down.get(FACING).getOpposite());
        }
        BlockState up = world.getBlockState(pos.up());
        if (up.isOf(CubliminalBlocks.VERTICAL_PIPE)) {
            return state
                    .with(TYPE, CeilingPipeTypes.ONE_LAYER_VERTICAL_PIPE_CONNECTION)
                    .with(FACING, up.get(FACING).getOpposite());
        }

        BlockState def = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        if (state.get(TYPE).equals(CeilingPipeTypes.ONE_LAYER_STRAIGHT)) {
            def = def.with(TYPE, CeilingPipeTypes.ONE_LAYER_STRAIGHT);
        }
        return def;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(TYPE)) {
            case ONE_LAYER_STRAIGHT -> ONE_LAYER;
            case ONE_LAYER_VERTICAL_PIPE_CONNECTION, TWO_LAYER_UP -> VoxelShapes.fullCube();
            case TWO_LAYER_STRAIGHT, TWO_LAYER_CORNER -> TWO_LAYER;
        };
    }
}
