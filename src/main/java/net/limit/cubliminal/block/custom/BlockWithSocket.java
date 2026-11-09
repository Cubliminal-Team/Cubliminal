package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.custom.template.VariableBlastResistance;
import net.limit.cubliminal.block.entity.SocketBlockEntity;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlockWithSocket extends BlockWithEntity implements BlockEntityProvider, VariableBlastResistance {

    public static final MapCodec<BlockWithSocket> CODEC = BlockWithSocket.createCodec(BlockWithSocket::new);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public BlockWithSocket(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
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
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (world.getBlockEntity(pos) != null || world.getBlockState(pos).getBlock() instanceof DoorBlock) return false;
        Direction facing = state.get(FACING);
        return WallTorchBlock.canPlaceAt(world, pos.offset(facing), facing);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
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
        return state.get(FACING) == direction.getOpposite() ? 15 : 0;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!state.isOf(oldState.getBlock())) {
            this.updateNeighbors(world, pos, state);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!world.isClient() && !state.isOf(newState.getBlock())) {
            this.updateNeighbors(world, pos, state);
        }
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (!world.isClient() && blockEntity instanceof SocketBlockEntity socketBlockEntity && socketBlockEntity.getSupport() != null) {
            world.setBlockState(pos, socketBlockEntity.getSupport(), Block.NOTIFY_ALL_AND_REDRAW);
            Direction lookDirection = player.getFacing().getOpposite();
            ItemEntity itemEntity = new ItemEntity(
                    world,
                    pos.getX() + 0.5 + lookDirection.getOffsetX() * 0.65,
                    pos.getY() + 0.1,
                    pos.getZ() + 0.5 + lookDirection.getOffsetZ() * 0.65,
                    new ItemStack(CubliminalBlocks.BLOCK_WITH_SOCKET)
            );
            itemEntity.setVelocity(
                    0.05 * lookDirection.getOffsetX() + world.random.nextDouble() * 0.02, 0.05, 0.05 * lookDirection.getOffsetZ() + world.random.nextDouble() * 0.02
            );
            world.spawnEntity(itemEntity);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockState support = this.getSupportNullable(pos, world);
        return support != null
                ? support.getOutlineShape(world, pos, context)
                : VoxelShapes.empty();
    }

    @Override
    protected VoxelShape getCullingShape(BlockState state) {
        return VoxelShapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockState support = this.getSupportNullable(pos, world);
        return support != null
                ? support.getCollisionShape(world, pos, context)
                : VoxelShapes.empty();
    }

    @Nullable
    private BlockState getSupportNullable(BlockPos pos, BlockView world) {
        return world.getBlockEntity(pos) instanceof SocketBlockEntity entity ? entity.getSupport() : null;
    }

    @Override
    public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (blockState.isAir() && fluidState.isEmpty()) {
            return Optional.empty();
        }

        float blockResistance = world.getBlockEntity(pos) instanceof SocketBlockEntity entity && entity.getSupport() != null
                ? entity.getSupport().getBlock().getBlastResistance()
                : blockState.getBlock().getBlastResistance();

        return Optional.of(Math.max(blockResistance, fluidState.getBlastResistance()));
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        if (
                builder.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof SocketBlockEntity socketBlockEntity
                && socketBlockEntity.getSupport() != null
                && !(builder.getOptional(LootContextParameters.THIS_ENTITY) instanceof LivingEntity)
        ) {
            List<ItemStack> drops = super.getDroppedStacks(state, builder);
            drops.addAll(socketBlockEntity.getSupport().getDroppedStacks(builder));
            return drops;
        }

        return Collections.emptyList();
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof SocketBlockEntity entity && entity.getSupport() != null
                ? entity.getSupport().getAmbientOcclusionLightLevel(world, pos)
                : super.getAmbientOcclusionLightLevel(state, world, pos);
    }

    @Override
    protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        super.spawnBreakParticles(world, player, pos, state);
        if (player.isCreative() && world.getBlockEntity(pos) instanceof SocketBlockEntity entity) {
            super.spawnBreakParticles(world, player, pos, entity.getSupport());
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SocketBlockEntity(pos, state);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
