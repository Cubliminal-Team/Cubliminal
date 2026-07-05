package net.limit.cubliminal.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class TransparentSlabBlock extends SlabBlock {

    public TransparentSlabBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(this)) {
            SlabType t1 = state.get(TYPE);
            SlabType t2 = state.get(TYPE);
            return switch (direction) {
                case DOWN -> t1 != SlabType.BOTTOM && t2 != SlabType.TOP;
                case UP -> t1 != SlabType.TOP && t2 != SlabType.BOTTOM;
                default -> t1 == t2 || t2 == SlabType.DOUBLE;
            };
        }

        return super.isSideInvisible(state, stateFrom, direction);
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }
}
