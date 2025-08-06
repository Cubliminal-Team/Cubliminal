package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.access.ServerWorldAccessor;
import net.limit.cubliminal.block.custom.template.RotatableLightBlock;
import net.limit.cubliminal.block.state.CustomProperties;
import net.limit.cubliminal.event.backrooms.BlackoutManager;
import net.limit.cubliminal.init.CubliminalBiomes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class FluorescentLightBlock extends RotatableLightBlock {

    public static final BooleanProperty RED = CustomProperties.RED;
    private static final VoxelShape VOXEL_SHAPE = Block.createCuboidShape(0, 15,0, 16, 16, 16);
    private final boolean fused;
    private final boolean flicker;

    public FluorescentLightBlock(Settings settings, boolean fused, boolean flicker) {
        super(settings);
        this.fused = fused;
        this.flicker = flicker;
        this.setDefaultState(this.getDefaultState()
                .with(LIT, !fused)
                .with(RED, false));
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
            return state
                    .with(LIT, state.get(LIT) && !this.fused)
                    .with(RED, ctx.getWorld().getBiome(ctx.getBlockPos())
                            .getKey().orElseThrow().equals(CubliminalBiomes.REDROOMS_BIOME));
        }
        return state;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState newState = state.with(LIT, !this.fused);
        if (world instanceof ServerWorldAccessor accessor) {
            BlackoutManager blackoutManager = accessor.blackoutManager();
            if (blackoutManager != null) {
                newState = state.with(LIT, !(this.fused || blackoutManager.lightsOffIn(pos)));
            }
        }
        world.setBlockState(pos, newState);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world instanceof ServerWorldAccessor accessor && this.flicker) {
            BlackoutManager blackoutManager = accessor.blackoutManager();
            if ((blackoutManager == null || !blackoutManager.lightsOffIn(pos)) && random.nextInt(this.fused ? 2 : 3) == 0) {
                world.setBlockState(pos, state.with(LIT, !this.fused));
                world.scheduleBlockTick(pos, state.getBlock(), 2);
            }
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
        builder.add(RED);
    }
}
