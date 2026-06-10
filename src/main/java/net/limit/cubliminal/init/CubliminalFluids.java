package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.InitOrder;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.block.custom.pipe.PipeBlock;
import net.limit.cubliminal.fluid.AlmondWaterFluid;
import net.limit.cubliminal.fluid.BlackSludgeFluid;
import net.limit.cubliminal.fluid.ContaminatedWaterFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

@InitOrder(-10)
public class CubliminalFluids implements Initer {

    public static FlowableFluid ALMOND_WATER = registerStill("almond_water", new AlmondWaterFluid.Still());
    public static FlowableFluid FLOWING_ALMOND_WATER = registerFlowing("almond_water", new AlmondWaterFluid.Flowing());

    public static FlowableFluid CONTAMINATED_WATER = registerStill("contaminated_water", new ContaminatedWaterFluid.Still());
    public static FlowableFluid FLOWING_CONTAMINATED_WATER = registerFlowing("contaminated_water", new ContaminatedWaterFluid.Flowing());

    public static FlowableFluid BLACK_SLUDGE = registerStill("black_sludge", new BlackSludgeFluid.Still());
    public static FlowableFluid FLOWING_BLACK_SLUDGE = registerFlowing("black_sludge", new BlackSludgeFluid.Flowing());

    public static final TagKey<Fluid> CUSTOM_FLUIDS = of("custom_fluids");

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, Cubliminal.id(id));
    }

    private static <T extends Fluid> T registerStill(String id, T value) {
        Cubliminal.LOGGER.info("Registering Fluid: " + id);
        return Registry.register(Registries.FLUID, Cubliminal.id(id), value);
    }

    private static <T extends FlowableFluid> T registerFlowing(String name, T flowing) {
        return registerStill("flowing_" + name, flowing);
    }

    @Override
    public void init()
    {
        Cubliminal.LOGGER.info("Registering Fluids");
    }

}
