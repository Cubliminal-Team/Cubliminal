package net.limit.cubliminal.block.entity;

import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BoxBlockEntity extends AbstractGeneric3x3LootableBlockEntity {
    protected BoxBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public BoxBlockEntity(BlockPos pos, BlockState state) {
        this(CubliminalBlockEntities.BOX_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.box");
    }
}
