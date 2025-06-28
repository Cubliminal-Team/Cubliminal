package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.world.structure.GridAlignedStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;

public class CubliminalStructures {
    public static StructureType<GridAlignedStructure> GRID_ALIGNED_STRUCTURES;

    public static void init() {
        GRID_ALIGNED_STRUCTURES = Registry.register(Registries.STRUCTURE_TYPE, Cubliminal.id("grid_aligned_structures"), () -> GridAlignedStructure.CODEC);
    }
}
