package net.limit.cubliminal.block.custom.pipe;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.state.pipeType.TypedPipeSupport;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class VerticalPipeBlock extends AbstractPipeBlock implements TypedPipeSupport {
    public static MapCodec<VerticalPipeBlock> CODEC = VerticalPipeBlock.createCodec(VerticalPipeBlock::new);

    public static final BooleanProperty CONNECTOR = BooleanProperty.of("connector");
    // Gets the voxel shape of the vertical pipe since the vertical pipe is smaller.
    public static final VoxelShape SHAPE_WEST = Block.createCuboidShape(9.0, 0.0, 4.0, 16.0, 16.0, 12.0);
    public static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(4.0, 0.0, 9.0, 12.0, 16.0, 16.0);
    public static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0.0, 0.0, 4.0, 7.0, 16.0, 12.0);
    public static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 16.0, 7.0);

    public VerticalPipeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(CONNECTOR, true));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        BlockState down = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        if (down.isOf(CubliminalBlocks.VERTICAL_PIPE)) {
            return state
                    .with(FACING, down.get(FACING))
                    .with(CONNECTOR, !down.get(CONNECTOR));
        }
        BlockState up = ctx.getWorld().getBlockState(ctx.getBlockPos().up());
        if (up.isOf(CubliminalBlocks.VERTICAL_PIPE)) {
            return state
                    .with(FACING, up.get(FACING))
                    .with(CONNECTOR, !up.get(CONNECTOR));
        }
        return state;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Sets up the outline shape based on facing.
        return switch (state.get(FACING)){
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CONNECTOR);
    }
}
