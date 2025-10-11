package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.block.custom.template.BlockVariantHolder;
import net.limit.cubliminal.block.custom.template.RotatableLightBlock;
import net.limit.cubliminal.block.state.CustomProperties;
import net.limit.cubliminal.init.CubliminalBiomes;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class FluorescentLightBlock extends RotatableLightBlock implements BlockVariantHolder {

    public static final BooleanProperty RED = CustomProperties.RED;
    private static final VoxelShape VOXEL_SHAPE = Block.createCuboidShape(0, 15,0, 16, 16, 16);

    public FluorescentLightBlock(Settings settings, boolean fused) {
        super(settings, fused);
        this.setDefaultState(this.getDefaultState().with(RED, false));
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
        if (!this.needsAttachment) {
            return true;
        } else {
            BlockPos blockPos = pos.offset(Direction.DOWN.getOpposite());
            BlockState blockState = world.getBlockState(blockPos);
            return blockState.isSideSolidFullSquare(world, blockPos, Direction.DOWN);
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state != null) {
            return state.with(RED, ctx.getWorld().getBiome(ctx.getBlockPos())
                    .getKey().orElseThrow().equals(CubliminalBiomes.REDROOMS_BIOME));
        }
        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(RED);
    }

    @Override
    public void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos) {
        if (prevState.isOf(CubliminalBlocks.FLUORESCENT_LIGHT)) {
            BlockVariantHolder.super.changeToVariant(region, prevState, pos);
        }
    }

    @Override
    public void changeToVariant(ChunkRegion region, BlockState state, BlockPos pos, Random random) {
        if (random.nextFloat() > 0.9 || region.getStatesInBox(new Box(pos).expand(1))
                .anyMatch(blockState -> blockState.isOf(CubliminalBlocks.FUSED_FLUORESCENT_LIGHT))) {
            region.setBlockState(pos, CubliminalBlocks.FUSED_FLUORESCENT_LIGHT.getDefaultState()
                    .with(HorizontalFacingBlock.FACING, state.get(HorizontalFacingBlock.FACING))
                    .with(CustomProperties.RED, state.get(CustomProperties.RED)), Block.FORCE_STATE);
        } else if (random.nextFloat() < 0.1) {
            region.setBlockState(pos, CubliminalBlocks.FLICKERING_FLUORESCENT_LIGHT.getDefaultState()
                    .with(HorizontalFacingBlock.FACING, state.get(HorizontalFacingBlock.FACING))
                    .with(CustomProperties.RED, state.get(CustomProperties.RED)), Block.FORCE_STATE);
        }
    }
}
