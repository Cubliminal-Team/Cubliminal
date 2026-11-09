package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.block.custom.template.BlockVariantHolder;
import net.limit.cubliminal.block.custom.template.RotatableLightBlock;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class LargeHangingLampBlock extends RotatableLightBlock implements BlockVariantHolder {

    public static final BooleanProperty CONNECTED = BooleanProperty.of("connected");
    private static final VoxelShape VOXEL_SHAPE = Block.createCuboidShape(0, 12,0, 16, 16, 16);

    public LargeHangingLampBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(CONNECTED, false));
    }

    public LargeHangingLampBlock(Settings settings, boolean fused) {
        super(settings, fused);
        this.setDefaultState(this.getDefaultState().with(CONNECTED, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VOXEL_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VOXEL_SHAPE;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state != null) {
            Direction facing = state.get(FACING);
            BlockPos pos = ctx.getBlockPos();
            World world = ctx.getWorld();
            BlockState neighbor = world.getBlockState(pos.offset(facing));

            boolean useInitialFacing = this.connectsTo(facing, neighbor);
            if (!useInitialFacing) {
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    if (this.connectsTo(dir, world.getBlockState(pos.offset(dir)))) {
                        return state.with(FACING, dir).with(CONNECTED, true);
                    }
                }
            }

            return state.with(CONNECTED, useInitialFacing);
        }

        return null;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        Direction facing = state.get(FACING);
        boolean findAdjacentLamp = false;
        if (!state.get(CONNECTED)) {
            if (this.connectsTo(direction, neighborState)) {
                return state.with(FACING, direction).with(CONNECTED, true);
            } else {
                findAdjacentLamp = true;
            }
        } else if (!this.connectsTo(facing, world.getBlockState(pos.offset(facing)))) {
            findAdjacentLamp = true;
        }

        if (findAdjacentLamp) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                if (this.connectsTo(dir, world.getBlockState(pos.offset(dir)))) {
                    return state.with(FACING, dir).with(CONNECTED, true);
                }
            }
            return state.with(CONNECTED, false);
        } else {
            return state;
        }
    }

    private boolean connectsTo(Direction facing, BlockState state) {
        return state.getBlock() instanceof LargeHangingLampBlock && facing.getAxis().isHorizontal() && (!state.get(CONNECTED) || state.get(FACING).getOpposite() == facing);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CONNECTED);
    }

    @Override
    public void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos, Random random) {
        if (random.nextFloat() > 0.8) {
            region.setBlockState(pos, CubliminalBlocks.FUSED_LARGE_HANGING_LAMP
                    .getDefaultState()
                    .with(CONNECTED, prevState.get(CONNECTED))
                    .with(FACING, prevState.get(FACING))
                    .with(WATERLOGGED, prevState.get(WATERLOGGED)),
                    Block.FORCE_STATE);
        }
    }
}
