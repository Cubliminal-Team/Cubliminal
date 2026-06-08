package net.limit.cubliminal.mixin;

import net.limit.cubliminal.access.ChunkAccessor;
import net.limit.cubliminal.access.ChunkSectionAccessor;
import net.limit.cubliminal.world.biome.source.LiminalBiomeSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements ChunkAccessor {

    @Shadow
    public abstract ChunkPos getPos();

    @Shadow
    public abstract HeightLimitView getHeightLimitView();

    @Shadow
    public abstract ChunkSection getSection(int yIndex);

    @Override
    public void cubliminal$chunkPopulateBiomes(BiomeSupplier biomeSupplier) {
        if (biomeSupplier instanceof LiminalBiomeSource biomeSource) {
            ChunkPos chunkPos = this.getPos();
            HeightLimitView heightLimitView = this.getHeightLimitView();
            int k = heightLimitView.getBottomSectionCoord();

            RegistryEntry<Biome> biome = biomeSource.calcBiome(
                    chunkPos.getStartX(),
                    ChunkSectionPos.getBlockCoord(k),
                    chunkPos.getStartZ()
            );

            for (; k <= heightLimitView.getTopSectionCoord(); ++k) {
                ChunkSection chunkSection = this.getSection(heightLimitView.sectionCoordToIndex(k));
                ((ChunkSectionAccessor) chunkSection).cubliminal$populateBiomes(biome);
            }
        }
    }

    @Override
    public void cubliminal$sectionPopulateBiomes(BiomeSupplier biomeSupplier) {
        if (biomeSupplier instanceof LiminalBiomeSource biomeSource) {
            ChunkPos chunkPos = this.getPos();
            int startX = chunkPos.getStartX();
            int startZ = chunkPos.getStartZ();
            HeightLimitView heightLimitView = this.getHeightLimitView();

            for (int k = heightLimitView.getBottomSectionCoord(); k <= heightLimitView.getTopSectionCoord(); k++) {
                ChunkSection chunkSection = this.getSection(heightLimitView.sectionCoordToIndex(k));
                RegistryEntry<Biome> biome = biomeSource.calcBiome(startX, ChunkSectionPos.getBlockCoord(k), startZ);
                ((ChunkSectionAccessor) chunkSection).cubliminal$populateBiomes(biome);
            }
        }
    }
}
