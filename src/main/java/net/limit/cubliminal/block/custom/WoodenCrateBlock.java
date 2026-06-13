package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.custom.template.BlackoutListener;
import net.limit.cubliminal.block.entity.WoodenCrateBlockEntity;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WoodenCrateBlock extends AbstractLootableContainerBlock implements BlackoutListener {
    public static final MapCodec<WoodenCrateBlock> CODEC = createCodec(WoodenCrateBlock::new);
    public static final BooleanProperty OPENED = BooleanProperty.of("opened");

    public WoodenCrateBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(OPENED, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        for (Direction direction : Direction.values()) {
            if (world.getLightLevel(pos.offset(direction)) > 11) {
                return;
            }
        }
        world.breakBlock(pos, false);
        world.setBlockState(pos, CubliminalBlocks.CRATE_AIR.getDefaultState());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            // Gets OPENED property state.
            boolean opened = state.get(OPENED);
            // Checks to see if it's opened or if player is in creative.
            // Otherwise, player will need to use specific tools to open the crate.
            if (opened || player.isCreative()) {
                // Opens up the GUI for the inventory slots.
                super.onUse(state, world, pos, player, hit);
            } else {
                // Gets the player's inventory.
                PlayerInventory playerInventory = player.getInventory();
                // Gets the current selected slot.
                ItemStack selected = playerInventory.main.get(playerInventory.selectedSlot);
                // Checks to see if the current selected item is an Axe or Pickaxe.
                if (selected.getItem() instanceof AxeItem || selected.getItem() instanceof PickaxeItem){
                    world.playSound(null, pos, SoundEvents.BLOCK_BAMBOO_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    // Sets the blockstate to opened.
                    world.setBlockState(pos, state.with(OPENED, true));
                    // Damage the pickaxe/axe upon use.
                    selected.damage(10, player);
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void blackoutUpdate(BlockState state, ServerWorld world, BlockPos pos, boolean lightsOff, Random random) {
        if (lightsOff && random.nextFloat() < 0.5) {
            world.scheduleBlockTick(pos, state.getBlock(), 1);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WoodenCrateBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPENED);
    }
}
