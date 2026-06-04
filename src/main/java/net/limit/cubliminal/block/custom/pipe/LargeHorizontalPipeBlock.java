package net.limit.cubliminal.block.custom.pipe;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.state.pipeType.LargeHorizontalPipeTypes;
import net.limit.cubliminal.block.state.pipeType.TypedPipeSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class LargeHorizontalPipeBlock extends PipeBlock implements TypedPipeSupport {
    public static MapCodec<LargeHorizontalPipeBlock> CODEC = LargeHorizontalPipeBlock.createCodec(LargeHorizontalPipeBlock::new);

    public static final BooleanProperty CONNECTOR = BooleanProperty.of("connector");
    public static final EnumProperty<LargeHorizontalPipeTypes> TYPE = TypedPipeSupport.build(LargeHorizontalPipeTypes.class);

    public LargeHorizontalPipeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(CONNECTOR, true));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    // TODO: northernlimit, add documentation here.
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        WorldView world = ctx.getWorld();
        BlockState northS = world.getBlockState(pos.offset(Direction.NORTH));
        BlockState southS = world.getBlockState(pos.offset(Direction.SOUTH));
        BlockState eastS = world.getBlockState(pos.offset(Direction.EAST));
        BlockState westS = world.getBlockState(pos.offset(Direction.WEST));
        boolean north = this.canConnect(northS, Direction.NORTH);
        boolean south = this.canConnect(southS, Direction.SOUTH);
        boolean east = this.canConnect(eastS, Direction.EAST);
        boolean west = this.canConnect(westS, Direction.WEST);

        BlockState placement = super.getPlacementState(ctx);
        Direction facing = null;
        LargeHorizontalPipeTypes type = null;
        BlockState neighbor = null;
        if (north) {
            neighbor = northS;
            if (east) {
                facing = Direction.NORTH;
            } else if (west) {
                facing = Direction.WEST;
            }
        } else if (south) {
            neighbor = southS;
            if (east) {
                facing = Direction.EAST;
            } else if (west) {
                facing = Direction.SOUTH;
            }
        } else if (east) {
            neighbor = eastS;
        } else if (west) {
            neighbor = westS;
        }

        if (facing != null) {
            type = LargeHorizontalPipeTypes.CORNER_PIPE;
        }

        if (type == null && neighbor != null) {
            type = LargeHorizontalPipeTypes.STRAIGHT_PIPE;
            facing = neighbor.get(FACING);
        }

        if (neighbor != null) {
            return placement
                    .with(CONNECTOR, !neighbor.get(CONNECTOR) && type == LargeHorizontalPipeTypes.STRAIGHT_PIPE)
                    .with(TYPE, type)
                    .with(FACING, facing);
        }

        return placement;
    }

    public boolean canConnect(BlockState state, Direction dir) {
        return state.contains(TYPE)
                && state.get(TYPE).equals(LargeHorizontalPipeTypes.STRAIGHT_PIPE)
                && !state.get(FACING).getAxis().test(dir);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CONNECTOR, TYPE);
    }
}
