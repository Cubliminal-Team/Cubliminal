package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.block.entity.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CubliminalBlockEntities implements Initer {

	public static <T extends BlockEntityType<?>> T register(String id, T blockEntityType) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, Cubliminal.id(id), blockEntityType);
	}

	public static final BlockEntityType<TheLobbyGatewayBlockEntity> THE_LOBBY_GATEWAY_BLOCK_ENTITY =
			register("the_lobby_gateway_block", FabricBlockEntityTypeBuilder.create(TheLobbyGatewayBlockEntity::new, CubliminalBlocks.THE_LOBBY_GATEWAY_BLOCK).build());

	public static final BlockEntityType<FluxCapacitorBlockEntity> FLUX_CAPACITOR_BLOCK_ENTITY =
			register("flux_capacitor", FabricBlockEntityTypeBuilder.create(FluxCapacitorBlockEntity::new, CubliminalBlocks.FLUX_CAPACITOR).build());

	public static final BlockEntityType<SinkBlockEntity> SINK_BLOCK_ENTITY =
			register("sink", FabricBlockEntityTypeBuilder.create(SinkBlockEntity::new, CubliminalBlocks.SINK).build());

	public static final BlockEntityType<ShowerBlockEntity> SHOWER_BLOCK_ENTITY =
			register("shower", FabricBlockEntityTypeBuilder.create(ShowerBlockEntity::new, CubliminalBlocks.SHOWER).build());

	public static final BlockEntityType<USBlockBlockEntity> USBLOCK_BLOCK_ENTITY =
			register("unlimited_structure_block", FabricBlockEntityTypeBuilder.create(USBlockBlockEntity::new, CubliminalBlocks.UNLIMITED_STRUCTURE_BLOCK).build());

	public static final BlockEntityType<WrittenDocumentBlockEntity> WRITTEN_DOCUMENT_BLOCK_ENTITY =
			register("written_document", FabricBlockEntityTypeBuilder.create(WrittenDocumentBlockEntity::new, CubliminalBlocks.WRITTEN_DOCUMENT).build());

	public static final BlockEntityType<WoodenCrateBlockEntity> WOODEN_CRATE_BLOCK_ENTITY =
			register("wooden_crate", FabricBlockEntityTypeBuilder.create(WoodenCrateBlockEntity::new, CubliminalBlocks.WOODEN_CRATE).build());

	public static final BlockEntityType<BoxBlockEntity> BOX_BLOCK_ENTITY =
			register("box", FabricBlockEntityTypeBuilder.create(BoxBlockEntity::new, CubliminalBlocks.BOX).build());

	public static final BlockEntityType<CorpseBlockEntity> CORPSE_BLOCK_ENTITY =
			register("corpse", FabricBlockEntityTypeBuilder.create(CorpseBlockEntity::new, CubliminalBlocks.CORPSE).build());
}
