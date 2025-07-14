package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.fluid.AlmondWaterFluid;
import net.limit.cubliminal.fluid.BlackSludgeFluid;
import net.limit.cubliminal.fluid.ContaminatedWaterFluid;
import net.limit.cubliminal.util.Debug;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class CubliminalFluids implements Initer {
    public static FlowableFluid ALMOND_WATER;
    public static FlowableFluid FLOWING_ALMOND_WATER;

    public static FlowableFluid CONTAMINATED_WATER;
    public static FlowableFluid FLOWING_CONTAMINATED_WATER;

    public static FlowableFluid BLACK_SLUDGE;
    public static FlowableFluid FLOWING_BLACK_SLUDGE;

    private static <T extends Fluid> T register(String id, T value) {
        return Registry.register(Registries.FLUID, Cubliminal.id(id), value);
    }

    private static <T extends FlowableFluid> Map<String, FlowableFluid> registerFluidTypes(String name, T flowing, T still){
        Map<String, FlowableFluid> fluidMap = new HashMap<>();
        fluidMap.put("flowing", register("flowing_" + name, flowing));
        fluidMap.put("still", register(name, still));
        return fluidMap;
    }

    public static void initFluid(){
        Map<String, FlowableFluid> almondWater = registerFluidTypes("almond_water", new AlmondWaterFluid.Flowing(), new AlmondWaterFluid.Still());
        FLOWING_ALMOND_WATER = almondWater.get("flowing");
        ALMOND_WATER = almondWater.get("still");

        Map<String, FlowableFluid> contaminatedWater = registerFluidTypes("contaminated_water", new ContaminatedWaterFluid.Flowing(), new ContaminatedWaterFluid.Still());
        FLOWING_CONTAMINATED_WATER = contaminatedWater.get("flowing");
        CONTAMINATED_WATER = contaminatedWater.get("still");

        Map<String, FlowableFluid> blackSludge = registerFluidTypes("black_sludge", new BlackSludgeFluid.Flowing(), new BlackSludgeFluid.Still());
        FLOWING_BLACK_SLUDGE = blackSludge.get("flowing");
        BLACK_SLUDGE = blackSludge.get("still");
    }
}
