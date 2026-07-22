package net.limit.cubliminal.networking.c2s;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public record SaveRoomC2SPayload(Identifier roomName, String json) implements CustomPayload {

    private static final Codec<SaveRoomC2SPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("roomName").forGetter(SaveRoomC2SPayload::roomName),
            Codec.STRING.fieldOf("json").forGetter(SaveRoomC2SPayload::json)
    ).apply(instance, SaveRoomC2SPayload::new));

    public static final Identifier SAVE_ROOM = Cubliminal.id("save_room");

    public static final Id<SaveRoomC2SPayload> ID = new Id<>(SAVE_ROOM);

    public static final PacketCodec<RegistryByteBuf, SaveRoomC2SPayload> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(SaveRoomC2SPayload payload, ServerPlayNetworking.Context context) {
        Identifier name = payload.roomName();
        Path generatedRoot = context.server().getSavePath(WorldSavePath.GENERATED);
        Path target = generatedRoot.resolve(name.getNamespace()).resolve(name.getPath() + ".json");

        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, payload.json(), StandardCharsets.UTF_8);
            context.player().sendMessage(Text.literal("Successfully saved room '" + name.getNamespace() + ":" + name.getPath() + "'"));
        } catch (IOException e) {
            Cubliminal.LOGGER.error("Couldn't save room {}", name, e);
        }
    }
}