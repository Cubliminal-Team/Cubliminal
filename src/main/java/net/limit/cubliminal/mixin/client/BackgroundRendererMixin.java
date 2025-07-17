package net.limit.cubliminal.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.fluid.CustomFluidBlock;
import net.limit.cubliminal.client.render.fog.FogManager;
import net.limit.cubliminal.client.render.fog.FogSettings;
import net.limit.cubliminal.client.util.CameraBackroomFluid;
import net.limit.cubliminal.config.CubliminalConfig;
import net.limit.cubliminal.fluid.BackroomsFlowableFluid;
import net.limit.cubliminal.util.ColorManager;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    // TODO: Improve BackgroundRendererMixin and optimize it to become more flexible.
    @Shadow
    @Nullable
    private static BackgroundRenderer.StatusEffectFogModifier getFogModifier(Entity entity, float tickDelta) {
        return null;
    }

    @Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;", shift = At.Shift.AFTER), cancellable = true)
    private static void cubliminal$fogDistance(Camera camera, BackgroundRenderer.FogType fogType,
                                               Vector4f color, float viewDistance, boolean thickenFog,
                                               float tickDelta, CallbackInfoReturnable<Fog> cir) {
        // Tick the smooth fading in cubliminal's foggy dimensions
        if (CubliminalConfig.get().enableSuperDenseFog) {
            if (thickenFog && camera.getSubmersionType() == CameraSubmersionType.NONE && getFogModifier(camera.getFocusedEntity(), tickDelta) == null) {
                ClientWorld world = MinecraftClient.getInstance().world;

                if (world != null && world.getRegistryKey().getValue().getNamespace().equals(Cubliminal.MOD_ID)) {

                    RegistryKey<Biome> biome = world.getBiome(camera.getBlockPos()).getKey().get();
                    FogSettings settings = FogSettings.FOG_PRESETS.getOrDefault(biome, new FogSettings(
                            viewDistance * 0.05f, Math.min(viewDistance, 192.0f) * 0.5f, 100f));

                    Fog newFog = FogManager.INSTANCE.tick(settings, color, tickDelta);
                    cir.setReturnValue(newFog);
                    cir.cancel();
                }
            }
        }
    }


    // TODO: Integrate this properly with Cubliminal.
    @Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;", shift = At.Shift.AFTER), cancellable = true)
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (CameraBackroomFluid.isSubmergedInBackroomFluid(camera)){
            FluidState fluidState = CameraBackroomFluid.getFluidState();
            if (fluidState.getFluid() instanceof BackroomsFlowableFluid backroomFluid){
                CustomFluidBlock.Settings settings = backroomFluid.getFluidBlock().getFluidSettings();
                ColorManager fluidColor = settings.getColor();
                Fog fog = new Fog(
                        settings.getFogStart(),
                        settings.getFogEnd(),
                        FogShape.SPHERE,
                        fluidColor.getRedDecimal(),
                        fluidColor.getGreenDecimal(),
                        fluidColor.getBlueDecimal(),
                        0.5f
                );
                RenderSystem.setShaderFog(fog);
                cir.setReturnValue(fog);
                cir.cancel();
            }
        }
    }
}
