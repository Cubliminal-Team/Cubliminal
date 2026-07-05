package net.limit.cubliminal.networking.c2s;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.block.Blocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record SaveSelectionC2SPayload(Identifier selectionName, BlockPos startPos, Vec3i size, boolean includeEntities) implements CustomPayload {

    private static final Codec<SaveSelectionC2SPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("selectionName").forGetter(SaveSelectionC2SPayload::selectionName),
            BlockPos.CODEC.fieldOf("startPos").forGetter(SaveSelectionC2SPayload::startPos),
            Vec3i.CODEC.fieldOf("size").forGetter(SaveSelectionC2SPayload::size),
            Codec.BOOL.fieldOf("includeEntities").forGetter(SaveSelectionC2SPayload::includeEntities)
    ).apply(instance, SaveSelectionC2SPayload::new));

    public static final Identifier SAVE_SELECTION = Cubliminal.id("save_selection");

    public static final Id<SaveSelectionC2SPayload> ID = new Id<>(SAVE_SELECTION);

    public static final PacketCodec<RegistryByteBuf, SaveSelectionC2SPayload> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(SaveSelectionC2SPayload payload, ServerPlayNetworking.Context context) {
        ServerWorld world = context.player().getServerWorld();
        StructureTemplateManager templateManager = world.getStructureTemplateManager();
        Identifier name = payload.selectionName();
        try {
            StructureTemplate structureTemplate = templateManager.getTemplateOrBlank(name);
            structureTemplate.saveFromWorld(world, payload.startPos(), payload.size(), payload.includeEntities(), Blocks.STRUCTURE_VOID);
            structureTemplate.setAuthor(context.player().getNameForScoreboard());
            try {
                templateManager.saveTemplate(name);
                context.player().sendMessage(Text.literal("Successfully saved structure in 'generated/" + name.getNamespace() + "/" + name.getPath() + "'"));
            } catch (InvalidIdentifierException var7) {
                Cubliminal.LOGGER.error("Couldn't save structure template {}", name);
            }
        } catch (InvalidIdentifierException var8) {
            Cubliminal.LOGGER.error("Couldn't create structure template {}", name);
        }
    }
}
