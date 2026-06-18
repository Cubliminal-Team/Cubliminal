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
import net.limit.cubliminal.client.block.*;
import net.limit.cubliminal.client.hud.SanityBarHudOverlay;
import net.limit.cubliminal.client.particle.CubliminalParticleManager;
import net.limit.cubliminal.client.fog.FogSettings;
import net.limit.cubliminal.client.entity.SeatRenderer;
import net.limit.cubliminal.client.screen.DocBlockEditScreen;
import net.limit.cubliminal.client.screen.DocBlockScreen;
import net.limit.cubliminal.event.KeyInputHandler;
import net.limit.cubliminal.init.*;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
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
				CubliminalBlocks.MOLD_SPROUTS,
				CubliminalBlocks.JUMBLED_DOCUMENTS,
				CubliminalBlocks.WRITTEN_DOCUMENT,
				CubliminalBlocks.LETTER_F,
				CubliminalBlocks.FLUX_CAPACITOR,
				CubliminalBlocks.WALL_LIGHT_BULB,
				CubliminalBlocks.FUSED_WALL_LIGHT_BULB,
				CubliminalBlocks.SMOKE_DETECTOR,
				CubliminalBlocks.VENTILATION_DUCT,
				CubliminalBlocks.CHAIN_WALL,
				CubliminalBlocks.CHAIN_BLOCK,
				CubliminalBlocks.CHAIN_SLAB,
				CubliminalBlocks.CHAIN_STAIRS,
				CubliminalBlocks.RED_CHAIN_WALL,
				CubliminalBlocks.WOODEN_PLANK
		);
		//BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped());
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
				CubliminalBlocks.THE_LOBBY_GATEWAY_BLOCK,
				CubliminalBlocks.EXIT_SIGN,
				CubliminalBlocks.EXIT_SIGN_2,
				CubliminalBlocks.CONTROL_BOX
		);

		BlockEntityRendererFactories.register(CubliminalBlockEntities.THE_LOBBY_GATEWAY_BLOCK_ENTITY, ManilaGatewayRenderer::new);
		BlockEntityRendererFactories.register(CubliminalBlockEntities.FLUX_CAPACITOR_BLOCK_ENTITY, FluxCapacitorRenderer::new);
		BlockEntityRendererFactories.register(CubliminalBlockEntities.USBLOCK_BLOCK_ENTITY, UnlimitedStructureBlockRenderer::new);
		BlockEntityRendererFactories.register(CubliminalBlockEntities.WRITTEN_DOCUMENT_BLOCK_ENTITY, WrittenDocumentRenderer::new);
		BlockEntityRendererFactories.register(CubliminalBlockEntities.CORPSE_BLOCK_ENTITY, CorpseBlockEntityRenderer::new);

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
		HandledScreens.register(CubliminalScreenHandlers.DOC_SCREEN_HANDLER, DocBlockScreen::make);
		HandledScreens.register(CubliminalScreenHandlers.DOC_EDIT_SCREEN_HANDLER, DocBlockEditScreen::make);
	}

}
