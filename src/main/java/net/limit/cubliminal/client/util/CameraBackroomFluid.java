package net.limit.cubliminal.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.access.CameraAccessor;
import net.limit.cubliminal.init.CubliminalFluids;
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
public class CameraBackroomFluid {
    private static FluidState FLUID_STATE;

    public static boolean isSubmergedInBackroomFluid(Camera camera){
        if (camera instanceof CameraAccessor accessor){
            BlockView view = accessor.getArea();
            BlockPos pos = accessor.getBlockPos();
            FluidState fluidState = view.getFluidState(pos);
            if (fluidState.isIn(CubliminalFluids.CUSTOM_FLUIDS)){
                setFluidState(fluidState);
                double fluidHeight = pos.getY() + fluidState.getHeight(view, pos);
                return camera.getPos().y < fluidHeight;
            }
        }
        return false;
    }

    public static FluidState getFluidState() {
        return FLUID_STATE;
    }

    private static void setFluidState(FluidState fluidState) {
        CameraBackroomFluid.FLUID_STATE = fluidState;
    }
}
