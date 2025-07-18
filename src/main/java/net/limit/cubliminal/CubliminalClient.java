package net.limit.cubliminal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.limit.cubliminal.access.GameRendererAccessor;
import net.limit.cubliminal.block.fluid.CustomFluidBlock;
import net.limit.cubliminal.client.hud.SanityBarHudOverlay;
import net.limit.cubliminal.client.particle.CubliminalParticleManager;
import net.limit.cubliminal.client.render.FluxCapacitorRenderer;
import net.limit.cubliminal.client.render.ManilaGatewayRenderer;
import net.limit.cubliminal.client.render.UnlimitedStructureBlockRenderer;
import net.limit.cubliminal.client.render.fog.FogSettings;
import net.limit.cubliminal.entity.client.SeatRenderer;
import net.limit.cubliminal.event.KeyInputHandler;
import net.limit.cubliminal.init.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class CubliminalClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CubliminalParticleManager.init();

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
				CubliminalBlocks.THE_LOBBY_GATEWAY_BLOCK,
				CubliminalBlocks.EMERGENCY_EXIT_DOOR_0,
				CubliminalBlocks.MOLD,
				CubliminalBlocks.JUMBLED_DOCUMENTS,
				CubliminalBlocks.LETTER_F,
				CubliminalBlocks.FLUX_CAPACITOR,
				CubliminalBlocks.WALL_LIGHT_BULB);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
				CubliminalBlocks.CHAIN_WALL);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
				CubliminalBlocks.THE_LOBBY_GATEWAY_BLOCK,
				CubliminalBlocks.EXIT_SIGN,
				CubliminalBlocks.EXIT_SIGN_2,
				CubliminalBlocks.SMOKE_DETECTOR,
				CubliminalBlocks.VENTILATION_DUCT);

		BlockEntityRendererFactories.register(CubliminalBlockEntities.THE_LOBBY_GATEWAY_BLOCK_ENTITY, ManilaGatewayRenderer::new);
		BlockEntityRendererFactories.register(CubliminalBlockEntities.FLUX_CAPACITOR_BLOCK_ENTITY, FluxCapacitorRenderer::new);
		BlockEntityRendererFactories.register(CubliminalBlockEntities.USBLOCK_BLOCK_ENTITY, UnlimitedStructureBlockRenderer::new);

		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> ((GameRendererAccessor) client.gameRenderer).setTriggered(false));

		KeyInputHandler.registerKeyInputs();
		EntityRendererRegistry.register(CubliminalEntities.SEAT_ENTITY, SeatRenderer::new);

		for (CustomFluidBlock backroomFluidBlock : CustomFluidBlock.getAll()) {
			FluidRenderHandlerRegistry.INSTANCE.register(
					backroomFluidBlock.getFluid().getStill(),
					backroomFluidBlock.getFluid().getFlowing(),
					new SimpleFluidRenderHandler(
							SimpleFluidRenderHandler.WATER_STILL,
							SimpleFluidRenderHandler.WATER_FLOWING,
							SimpleFluidRenderHandler.WATER_OVERLAY,
							backroomFluidBlock.getColor(0)
					)
			);
		}

		// Init Initers
		IniterClient.initialise();

		FogSettings.init();

		HudRenderCallback.EVENT.register(new SanityBarHudOverlay());
	}

}
