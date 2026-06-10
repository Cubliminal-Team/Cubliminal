package net.limit.cubliminal.networking.s2c;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.IniterClient;

/**
 * This class registers client side actions performed when a server-to-client packet is received.
 */

@Environment(EnvType.CLIENT)
public class S2CPackets implements IniterClient {

    @Override
    public void init() {
        ClientPlayNetworking.registerGlobalReceiver(NoClipSyncPayload.ID, NoClipSyncPayload::receive);
        ClientPlayNetworking.registerGlobalReceiver(SanitySyncPayload.ID, SanitySyncPayload::receive);
        ClientPlayNetworking.registerGlobalReceiver(WrittenDocScreenPayload.ID, WrittenDocScreenPayload::receive);
    }
}
