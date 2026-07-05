package net.limit.cubliminal.networking;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.limit.cubliminal.networking.s2c.StructureTemplateListS2CPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class ServerEventPacketCallbacks {

    public static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        //Update the arrived player with the current list of structure templates
        StructureTemplateListS2CPayload.sendTo(handler.getPlayer());
    }
}
