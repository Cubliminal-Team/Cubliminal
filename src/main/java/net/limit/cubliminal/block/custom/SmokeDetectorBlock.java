package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.block.custom.template.BlockVariantHolder;
import net.limit.cubliminal.init.CubliminalRegistrar;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class SmokeDetectorBlock extends Block implements BlockVariantHolder {

	protected static final VoxelShape SHAPE = Block.createCuboidShape(6, 14, 6, 10, 16, 10);

	public SmokeDetectorBlock(Settings settings) {
		super(settings);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos blockPos = pos.offset(Direction.UP);
		BlockState blockState = world.getBlockState(blockPos);
		return blockState.isSideSolidFullSquare(world, blockPos, Direction.DOWN);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d vec3d = state.getModelOffset(pos);
		return SHAPE.offset(vec3d.x, vec3d.y, vec3d.z);
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
		if (!state.canPlaceAt(world, pos)) {
			world.breakBlock(pos, false);
		}
	}

	@Override
	public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos, Random random) {
		if (random.nextFloat() > 0.1) {
			if (region.toServerWorld().getRegistryKey().equals(CubliminalRegistrar.THE_LOBBY_KEY)) {
				region.setBlockState(pos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 3), Block.FORCE_STATE);
			} else {
				region.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
			}
		}
	}
}
