package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.entity.BoxBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BoxBlock extends AbstractLootableContainerBlock {
    public static final MapCodec<BoxBlock> CODEC = createCodec(BoxBlock::new);

    public BoxBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            super.onUse(state, world, pos, player, hit);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BoxBlockEntity(pos, state);
    }
}
