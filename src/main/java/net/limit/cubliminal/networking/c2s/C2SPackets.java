package net.limit.cubliminal.networking.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.networking.s2c.NoClipSyncPayload;
import net.limit.cubliminal.networking.s2c.SanitySyncPayload;

public class C2SPackets {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(NoClipSyncPayload.ID, NoClipSyncPayload::receive);
        ClientPlayNetworking.registerGlobalReceiver(SanitySyncPayload.ID, SanitySyncPayload::receive);
    }
}
