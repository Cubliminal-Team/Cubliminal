package net.limit.cubliminal.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
public interface CameraAccessor {
    BlockView getArea();
    BlockPos getBlockPos();
}
