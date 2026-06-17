package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.entity.CorpseBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CorpseBlock extends BlockWithEntity {
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

    public static final MapCodec<CorpseBlock> CODEC = createCodec(CorpseBlock::new);

    public CorpseBlock(Settings settings) {
        super(settings);
        // Sets default facing state to NORTH.
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        // Creates the block entity.
        return new CorpseBlockEntity(pos, state);
    }

    // Builds and appends the properties.
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Places block based on direction of player.
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    // Renders the block.
    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Check if the world is not a client
        if (!world.isClient) {
            // Gets the block entity at the position.
            BlockEntity blockEntity = world.getBlockEntity(pos);
            // Checks to see if the block entity is an instance of a corpse block entity.
            if (blockEntity instanceof CorpseBlockEntity corpseBlockEntity) {
                // Only allow players to open corpse inventories if it isn't empty.
                // Also only allows players in creative mode to open up the inventory.
                if (!corpseBlockEntity.getHeldStacks().stream().allMatch(ItemStack::isEmpty) || player.isCreative()) {
                    // Opens handled screen
                    player.openHandledScreen(corpseBlockEntity);
                } else {
                    return super.onUse(state, world, pos, player, hit);
                }
            }
        }
        return ActionResult.SUCCESS;
    }
}
