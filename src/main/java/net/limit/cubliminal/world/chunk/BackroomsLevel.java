package net.limit.cubliminal.world.chunk;

import net.limit.cubliminal.level.Level;
import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;

public interface BackroomsLevel {

    Level getLevel();

    default Random blockSeed(ChunkRegion region, BlockPos pos) {
        return Random.create(region.getSeed() + LimlibHelper.blockSeed(pos));
    }
}
