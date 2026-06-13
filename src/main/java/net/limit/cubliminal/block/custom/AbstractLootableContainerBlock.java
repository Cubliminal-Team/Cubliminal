package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.block.entity.AbstractGeneric3x3LootableBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractLootableContainerBlock extends BlockWithEntity {
    protected AbstractLootableContainerBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Gets the block entity.
        BlockEntity blockEntity = world.getBlockEntity(pos);
        // Checks to see if the block entity is an instance of abstract lootable block entity.
        if (blockEntity instanceof AbstractGeneric3x3LootableBlockEntity abstractGeneric3x3LootableBlockEntity) {
            // Opens 3x3 inventory.
            player.openHandledScreen(abstractGeneric3x3LootableBlockEntity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
