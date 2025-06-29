package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;

import net.limit.cubliminal.Initer;
import net.limit.cubliminal.world.structure.GridAlignedStructure;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;

public class CubliminalStructures implements Initer {
    public static StructureType<GridAlignedStructure> GRID_ALIGNED_STRUCTURES;

    @Override
    public void init() {
        GRID_ALIGNED_STRUCTURES = Registry.register(Registries.STRUCTURE_TYPE, Cubliminal.id("grid_aligned_structures"), () -> GridAlignedStructure.CODEC);
    }
}
