package net.limit.cubliminal.block.custom.template;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class TransparentBoardBlock extends BoardBlock {

    public TransparentBoardBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(this)) {
            BlockFace face = state.get(FACE);
            BlockFace faceFrom = stateFrom.get(FACE);
            Direction facing = state.get(FACING);
            Direction facingFrom = stateFrom.get(FACING);
            if (face != BlockFace.WALL) {
                return direction.getAxis() != Direction.Axis.Y && face == faceFrom;
            }
            return direction.getAxis() != facing.getAxis() && face == faceFrom && facing == facingFrom;
        }

        return false;
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
