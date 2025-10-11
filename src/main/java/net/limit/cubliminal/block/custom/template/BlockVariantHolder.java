package net.limit.cubliminal.block.custom.template;

import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;

public interface BlockVariantHolder {

    default void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos) {
        this.changeToVariant(region, prevState, pos, Random.create(region.getSeed() + LimlibHelper.blockSeed(pos)));
    }

    void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos, Random random);
}
