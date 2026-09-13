package net.limit.cubliminal.networking.c2s;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.world.room.Room;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.PathUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public record SaveRoomC2SPayload(String name, Room room) implements CustomPayload {

    private static final Codec<SaveRoomC2SPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(SaveRoomC2SPayload::name),
            Room.CODEC.fieldOf("room").forGetter(SaveRoomC2SPayload::room)
    ).apply(instance, SaveRoomC2SPayload::new));

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final Identifier SAVE_ROOM = Cubliminal.id("save_room");

    public static final Id<SaveRoomC2SPayload> ID = new Id<>(SAVE_ROOM);

    public static final PacketCodec<RegistryByteBuf, SaveRoomC2SPayload> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void sendPacket(String name, Room room) {
        ClientPlayNetworking.send(new SaveRoomC2SPayload(name, room));
    }

    public static void receive(SaveRoomC2SPayload payload, ServerPlayNetworking.Context context) {
        String roomName = payload.name();
        Room room = payload.room();

        Path roomPath = Room.getRoomPath(context.server(), roomName, ".json");
        Path parentPath = roomPath.getParent();
        if (parentPath == null) {
            Cubliminal.LOGGER.error("Parent path for: {} doesn't exist!", roomPath);
            return;
        }

        try {
            Files.createDirectories(Files.exists(parentPath) ? parentPath.toRealPath() : parentPath);
            JsonElement jsonElement = Room.CODEC.encodeStart(JsonOps.INSTANCE, room).getOrThrow();
            Files.writeString(roomPath, GSON.toJson(jsonElement), StandardCharsets.UTF_8);
            context.player().sendMessage(Text.literal("Successfully saved room '" + roomName + "'"));
        } catch (IOException e) {
            Cubliminal.LOGGER.error("Failed to create parent directory: {}", parentPath);
        }
    }
}