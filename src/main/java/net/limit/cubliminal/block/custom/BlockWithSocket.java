package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class BlockWithSocket extends HorizontalFacingBlock {

    public static final MapCodec<BlockWithSocket> CODEC = BlockWithSocket.createCodec(BlockWithSocket::new);
    public static final BooleanProperty SOCKET = BooleanProperty.of("socket");

    public BlockWithSocket(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(SOCKET, false));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = this.getDefaultState();
        WorldView worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Direction[] directions = ctx.getPlacementDirections();

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                blockState = blockState.with(FACING, direction.getOpposite());
                if (blockState.canPlaceAt(worldView, blockPos)) {
                    return blockState;
                }
            }
        }

        return null;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return state.get(SOCKET);
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    protected void updateNeighbors(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction);
        WireOrientation wireOrientation = OrientationHelper.getEmissionOrientation(world, direction, null);
        world.updateNeighbor(blockPos, this, wireOrientation);
        world.updateNeighbors(blockPos, this);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(SOCKET) && state.get(FACING) == direction.getOpposite() ? 15 : 0;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(CubliminalBlocks.SOCKET.asItem()) && !state.get(SOCKET) && !player.isSneaking()) {
            if (!world.isClient()) {
                player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                stack.splitUnlessCreative(1, player);
                world.setBlockState(pos, state.with(SOCKET, true).with(FACING, hit.getSide()));
                world.playSound(null, pos, SoundEvents.BLOCK_CALCITE_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!state.isOf(oldState.getBlock())) {
            this.updateNeighbors(world, pos, state);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient() && state.get(SOCKET)) {
                this.updateNeighbors(world, pos, state.with(SOCKET, false));
            }
        }
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SOCKET);
    }
}
