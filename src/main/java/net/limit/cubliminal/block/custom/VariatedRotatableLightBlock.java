package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.block.custom.template.BlockVariantHolder;
import net.limit.cubliminal.block.custom.template.RotatableLightBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;

public class VariatedRotatableLightBlock extends RotatableLightBlock implements BlockVariantHolder {

    private final float replacementChance;
    private final BlockState variant;

    public VariatedRotatableLightBlock(Settings settings, float replacementChance, BlockState variant) {
        this(settings, false, replacementChance, variant);
    }

    public VariatedRotatableLightBlock(Settings settings, boolean fused, float replacementChance, BlockState variant) {
        super(settings, fused);
        this.replacementChance = replacementChance;
        this.variant = variant;
    }

    @Override
    public void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos, Random random) {
        if (random.nextFloat() < replacementChance) {
            region.setBlockState(pos, variant
                            .with(FACING, prevState.get(FACING))
                            .with(WATERLOGGED, prevState.get(WATERLOGGED)),
                    Block.FORCE_STATE);
        }
    }
}
