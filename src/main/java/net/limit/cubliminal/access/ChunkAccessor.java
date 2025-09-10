package net.limit.cubliminal.access;

import net.minecraft.world.biome.source.BiomeSupplier;

public interface ChunkAccessor {

    void cubliminal$chunkPopulateBiomes(BiomeSupplier biomeSupplier);

    void cubliminal$sectionPopulateBiomes(BiomeSupplier biomeSupplier);

}