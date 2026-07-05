package net.limit.cubliminal.networking.c2s;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StructureTemplateInfoC2SPayload(Identifier template) implements CustomPayload {

    private static final Codec<StructureTemplateInfoC2SPayload> CODEC = Identifier.CODEC
            .fieldOf("template")
            .codec()
            .xmap(StructureTemplateInfoC2SPayload::new, StructureTemplateInfoC2SPayload::template);

    public static final Identifier STRUCTURE_TEMPLATE_INFO = Cubliminal.id("structure_template_info");

    public static final Id<StructureTemplateInfoC2SPayload> ID = new Id<>(STRUCTURE_TEMPLATE_INFO);

    public static final PacketCodec<RegistryByteBuf, StructureTemplateInfoC2SPayload> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(StructureTemplateInfoC2SPayload payload, ServerPlayNetworking.Context context) {

    }
}
