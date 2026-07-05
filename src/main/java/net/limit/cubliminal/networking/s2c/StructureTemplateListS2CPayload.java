package net.limit.cubliminal.networking.s2c;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.screen.roomcreator.RoomCreatorDataManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;

import java.util.List;

public record StructureTemplateListS2CPayload(List<Identifier> structureTemplates) implements CustomPayload {

    private static final Codec<StructureTemplateListS2CPayload> CODEC = Identifier.CODEC.listOf()
            .fieldOf("structureTemplates")
            .codec()
            .xmap(StructureTemplateListS2CPayload::new, StructureTemplateListS2CPayload::structureTemplates);

    public static final Identifier STRUCTURE_TEMPLATE_LIST = Cubliminal.id("structure_template_list");

    public static final Id<StructureTemplateListS2CPayload> ID = new Id<>(STRUCTURE_TEMPLATE_LIST);

    public static final PacketCodec<RegistryByteBuf, StructureTemplateListS2CPayload> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void sendTo(ServerPlayerEntity player) {
        StructureTemplateManager structureTemplateManager = player.getServerWorld().getStructureTemplateManager();
        List<Identifier> structureTemplates = structureTemplateManager.streamTemplates().toList();
        ServerPlayNetworking.send(player, new StructureTemplateListS2CPayload(structureTemplates));
    }

    public static void receive(StructureTemplateListS2CPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.player();
        if (player != null) {
            RoomCreatorDataManager.INSTANCE.processPacketUpdate(payload);
        }
    }
}
