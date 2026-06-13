package net.limit.cubliminal.block.entity;

import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WoodenCrateBlockEntity extends AbstractGeneric3x3LootableBlockEntity {
    protected WoodenCrateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public WoodenCrateBlockEntity(BlockPos pos, BlockState state) {
        this(CubliminalBlockEntities.WOODEN_CRATE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.wooden_crate");
    }
}
