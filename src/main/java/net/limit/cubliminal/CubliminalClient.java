package net.limit.cubliminal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.limit.cubliminal.client.render.RenderLayers;
import net.limit.cubliminal.client.hud.NoclipHudOverlay;
import net.limit.cubliminal.client.hud.SanityBarHudOverlay;
import net.limit.cubliminal.client.render.FluxCapacitorRenderer;
import net.limit.cubliminal.client.render.ManilaGatewayRenderer;
import net.limit.cubliminal.client.render.UnlimitedStructureBlockRenderer;
import net.limit.cubliminal.client.render.fog.FogSettings;
import net.limit.cubliminal.entity.client.SeatRenderer;
import net.limit.cubliminal.event.KeyInputHandler;
import net.limit.cubliminal.init.*;
import net.limit.cubliminal.networking.c2s.C2SPackets;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class CubliminalClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
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

		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> NoclipHudOverlay.INSTANCE.setAux_renderOverlay(false));

		KeyInputHandler.registerKeyInputs();
		EntityRendererRegistry.register(CubliminalEntities.SEAT_ENTITY, SeatRenderer::new);
		RenderLayers.init();
		FogSettings.init();
		C2SPackets.init();

		//HudRenderCallback.EVENT.register(NoclipHudOverlay.INSTANCE);
		HudRenderCallback.EVENT.register(new SanityBarHudOverlay());
	}

}
