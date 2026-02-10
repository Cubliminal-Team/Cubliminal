package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.entity.WrittenDocumentBlockEntity;
import net.limit.cubliminal.block.state.DocumentMode;
import net.limit.cubliminal.block.state.RandRot;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class WrittenDocumentBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final MapCodec<WrittenDocumentBlock> CODEC = WrittenDocumentBlock.createCodec(WrittenDocumentBlock::new);
    protected static final VoxelShape VOXEL_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<DocumentMode> MODE = EnumProperty.of("mode", DocumentMode.class);
    public static final EnumProperty<RandRot> ROT = EnumProperty.of("rot", RandRot.class);

    public WrittenDocumentBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(MODE, DocumentMode.TEXT)
                .with(ROT, RandRot.NO_ROT));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VOXEL_SHAPE;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return hasTopRim(world, pos.down());
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.getBlockEntity(pos) instanceof WrittenDocumentBlockEntity entity) {
            entity.setDocData(itemStack.copyWithCount(1), placer instanceof PlayerEntity player ? player : null);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world.getBlockEntity(pos) instanceof WrittenDocumentBlockEntity entity && entity.hasDocument()) {
            ItemStack stack = entity.getDocument();
            if (newState.contains(MODE)) {
                WrittenDocContentComponent component = stack.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
                if (component != null) {
                    stack.set(CubliminalDataComponents.WRITTEN_DOC_COMPONENT, new WrittenDocContentComponent(
                            newState.get(MODE), component.text(), component.texture(), component.resolved()
                    ));
                }
            } else {
                Block.dropStack(world, pos, stack);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState = this.getDefaultState().with(ROT, RandRot.random(blockPos));
        WrittenDocContentComponent component = ctx.getStack().get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
        if (component != null) {
            blockState = blockState.with(MODE, component.mode());
        }
        WorldView worldView = ctx.getWorld();
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

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WrittenDocumentBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof WrittenDocumentBlockEntity blockEntity) {
            if (!world.isClient()) {
                blockEntity.setOpenEditScreen(stack.isOf(Items.FEATHER));
                player.openHandledScreen(blockEntity);
                blockEntity.setOpenEditScreen(false);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE, ROT);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
