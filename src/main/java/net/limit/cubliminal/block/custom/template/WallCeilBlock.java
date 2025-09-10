package net.limit.cubliminal.block.custom.template;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class WallCeilBlock extends WallMountedBlock {
    public static final MapCodec<WallCeilBlock> CODEC = WallCeilBlock.createCodec(WallCeilBlock::new);

    protected boolean needsAttachment = false;
    protected VoxelShape CEILING_X_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape CEILING_Z_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape FLOOR_X_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape FLOOR_Z_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape NORTH_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape SOUTH_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape WEST_SHAPE = VoxelShapes.fullCube();
    protected VoxelShape EAST_SHAPE = VoxelShapes.fullCube();

    public WallCeilBlock(Settings settings) {
        super(settings);
    }

    public WallCeilBlock needsAttachment() {
        this.needsAttachment = true;
        return this;
    }

    public WallCeilBlock voxelShapes(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.CEILING_X_SHAPE = Block.createCuboidShape(minY, 16 - maxZ, 16 - maxX, maxY, 16 - minZ, 16 - minX);
        this.CEILING_Z_SHAPE = Block.createCuboidShape(minX, 16 - maxZ, minY, maxX, 16 - minZ, maxY);
        this.FLOOR_X_SHAPE = Block.createCuboidShape(16 - maxY, minZ, 16 - maxX, 16 - minY, maxZ, 16 - minX);
        this.FLOOR_Z_SHAPE = Block.createCuboidShape(minX, minZ, 16 - maxY, maxX, maxZ, 16 - minY);
        this.WEST_SHAPE = Block.createCuboidShape(16 - maxZ, minY, minX, 16 - minZ, maxY, maxX);
        this.EAST_SHAPE = Block.createCuboidShape(minZ, minY, 16 - maxX, maxZ, maxY, 16 - minX);
        this.SOUTH_SHAPE = Block.createCuboidShape(minX, minY, minZ, maxX, maxY, maxZ);
        this.NORTH_SHAPE = Block.createCuboidShape(16 - maxX, minY, 16 - maxZ, 16 - minX, maxY, 16 - minZ);
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
        Direction direction = state.get(FACING);
        return switch (state.get(FACE)) {
            case FLOOR -> direction.getAxis() == Direction.Axis.X ? FLOOR_X_SHAPE : FLOOR_Z_SHAPE;
            case WALL -> switch (direction) {
                    case EAST -> EAST_SHAPE;
                    case WEST -> WEST_SHAPE;
                    case SOUTH -> SOUTH_SHAPE;
                    default -> NORTH_SHAPE;
                };
            case CEILING -> direction.getAxis() == Direction.Axis.X ? CEILING_X_SHAPE : CEILING_Z_SHAPE;
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }
}
