package net.limit.cubliminal.client.util;

import net.limit.cubliminal.init.CubliminalFluidTags;
import net.limit.cubliminal.util.Debug;
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class CameraBackroomFluid {
    private static FluidState fluidState;

    public static boolean isSubmergedInBackroomFluid(Camera camera){
        if (camera instanceof CameraAccessor accessor){
            BlockView view = accessor.getArea();
            BlockPos pos = accessor.getBlockPos();
            FluidState fluidState = view.getFluidState(pos);
            if (fluidState.isIn(CubliminalFluidTags.CUSTOM_FLUIDS)){
                setFluidState(fluidState);
                double fluidHeight = pos.getY() + fluidState.getHeight(view, pos);
                return camera.getPos().y < fluidHeight;
            }
        }
        return false;
    }

    public static FluidState getFluidState() {
        return fluidState;
    }

    private static void setFluidState(FluidState fluidState) {
        CameraBackroomFluid.fluidState = fluidState;
    }
}
