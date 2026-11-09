package net.limit.cubliminal.block.custom.pipe;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.custom.template.HorizontalConnectingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SmallHangingPipeBlock extends HorizontalConnectingBlock {

    public static final BooleanProperty ATTACHED = Properties.ATTACHED;
    public static final MapCodec<SmallHangingPipeBlock> CODEC = SmallHangingPipeBlock.createCodec(SmallHangingPipeBlock::new);

    public SmallHangingPipeBlock(Settings settings) {
        super(1.0f, 1.0f, 9.0f, 9.0f, 9.0f, 7.0f, settings);
        this.setDefaultState(this.getDefaultState().with(ATTACHED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalConnectingBlock> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState finalState = this.getDefaultState()
                .with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER)
                .with(ATTACHED, true);
        Map<Direction, BooleanProperty> properties = Map.of(
                Direction.EAST, EAST, Direction.WEST, WEST, Direction.NORTH, NORTH, Direction.SOUTH, SOUTH
        );
        int connections = 0;
        Direction connection = Direction.NORTH;
        for (var entry : properties.entrySet()) {
            BlockState neighbor = world.getBlockState(pos.offset(entry.getKey()));
            boolean connects = this.connectsTo(neighbor);
            finalState = finalState.with(entry.getValue(), connects);
            if (connects) {
                connections++;
                connection = entry.getKey();
                if (neighbor.get(ATTACHED)) {
                    finalState = finalState.with(ATTACHED, false);
                }
            }
        }

        if (connections == 0) {
            return finalState
                    .with(properties.get(ctx.getHorizontalPlayerFacing()), true)
                    .with(properties.get(ctx.getHorizontalPlayerFacing().getOpposite()), true);
        } else if (connections == 1) {
            return finalState.with(properties.get(connection.getOpposite()), true);
        }

        return finalState;
    }

    @Override
    protected boolean connectsTo(BlockState state) {
        return state.getBlock().equals(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ATTACHED);
    }
}
