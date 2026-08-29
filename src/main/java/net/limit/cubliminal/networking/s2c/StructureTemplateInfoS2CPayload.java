package net.limit.cubliminal.networking.s2c;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.screen.roomcreator.data.RoomCreatorDataManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public record StructureTemplateInfoS2CPayload(List<Pair<Identifier, Vec3i>> templateInfoList) implements CustomPayload {

    private static final Codec<StructureTemplateInfoS2CPayload> CODEC = Codec
            .pair(Identifier.CODEC.fieldOf("i").codec(), Vec3i.CODEC.fieldOf("s").codec())
            .listOf()
            .xmap(StructureTemplateInfoS2CPayload::new, StructureTemplateInfoS2CPayload::templateInfoList);

    public static final Identifier STRUCTURE_TEMPLATE_INFO = Cubliminal.id("structure_template_info");

    public static final Id<StructureTemplateInfoS2CPayload> ID = new Id<>(STRUCTURE_TEMPLATE_INFO);

    public static final PacketCodec<RegistryByteBuf, StructureTemplateInfoS2CPayload> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static List<Pair<Identifier, Vec3i>> collectTemplateInfo(StructureTemplateManager templateManager) {
        List<Pair<Identifier, Vec3i>> templateInfo = new ArrayList<>();
        Stream<Identifier> templateNames = templateManager.streamTemplates();
        templateNames.forEach(id -> templateManager.getTemplate(id).ifPresent(template -> templateInfo.add(Pair.of(id, template.getSize()))));
        return templateInfo;
    }

    public static void sendTo(ServerPlayerEntity player) {
        StructureTemplateManager structureTemplateManager = player.getServerWorld().getStructureTemplateManager();
        ServerPlayNetworking.send(player, new StructureTemplateInfoS2CPayload(collectTemplateInfo(structureTemplateManager)));
    }

    public static void sendToAll(Collection<ServerPlayerEntity> players) {
        if (!players.isEmpty()) {
            StructureTemplateManager structureTemplateManager = players.stream().toList().getFirst().getServerWorld().getStructureTemplateManager();
            StructureTemplateInfoS2CPayload payload = new StructureTemplateInfoS2CPayload(collectTemplateInfo(structureTemplateManager));
            players.forEach(player -> ServerPlayNetworking.send(player, payload));
        }
    }

    public static void receive(StructureTemplateInfoS2CPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.player();
        if (player != null) {
            RoomCreatorDataManager.INSTANCE.processPacketUpdate(payload);
        }
    }
}
