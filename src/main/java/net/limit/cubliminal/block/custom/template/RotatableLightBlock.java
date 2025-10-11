package net.limit.cubliminal.block.custom.template;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.access.ServerWorldAccessor;
import net.limit.cubliminal.event.backrooms.BlackoutManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class RotatableLightBlock extends RotatableBlock implements BlackoutListener {
    public static final MapCodec<RotatableLightBlock> CODEC = RotatableLightBlock.createCodec(RotatableLightBlock::new);

    public static final BooleanProperty LIT = Properties.LIT;
    protected final boolean fused;

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    public RotatableLightBlock(Settings settings) {
        this(settings, false);
    }

    public RotatableLightBlock(Settings settings, boolean fused) {
        super(settings);
        this.fused = fused;
        this.setDefaultState(this.getDefaultState().with(LIT, !fused));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state != null && ctx.getWorld() instanceof ServerWorldAccessor accessor) {
            BlackoutManager blackoutManager = accessor.blackoutManager();
            if (blackoutManager != null) {
                return state.with(LIT, !this.fused && !blackoutManager.lightsOffIn(ctx.getBlockPos()));
            }
        }
        return state;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState newState;
        BlackoutManager blackoutManager = ((ServerWorldAccessor) world).blackoutManager();
        if (blackoutManager != null) {
            newState = state.with(LIT, !(this.fused || blackoutManager.lightsOffIn(pos)));
        } else {
            newState = state.with(LIT, !this.fused);
        }
        world.setBlockState(pos, newState);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlackoutManager blackoutManager = ((ServerWorldAccessor) world).blackoutManager();
        if ((blackoutManager == null || !blackoutManager.lightsOffIn(pos)) && random.nextInt(this.fused ? 2 : 3) == 0) {
            world.setBlockState(pos, state.with(LIT, !this.fused));
            world.scheduleBlockTick(pos, state.getBlock(), 2);
        }
    }

    @Override
    public void blackoutUpdate(BlockState state, ServerWorld world, BlockPos pos, boolean lightsOff, Random random) {
        if (!this.fused) {
            world.setBlockState(pos, state.with(LIT, !lightsOff));
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIT);
    }
}
