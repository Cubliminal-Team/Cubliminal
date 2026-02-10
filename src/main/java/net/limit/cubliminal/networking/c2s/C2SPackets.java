package net.limit.cubliminal.networking.c2s;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.IniterClient;
import net.limit.cubliminal.networking.s2c.NoClipSyncPayload;
import net.limit.cubliminal.networking.s2c.SanitySyncPayload;
import net.limit.cubliminal.networking.s2c.WrittenDocScreenPayload;

@Environment(EnvType.CLIENT)
public class C2SPackets implements IniterClient {

    @Override
    public void init() {
        ClientPlayNetworking.registerGlobalReceiver(NoClipSyncPayload.ID, NoClipSyncPayload::receive);
        ClientPlayNetworking.registerGlobalReceiver(SanitySyncPayload.ID, SanitySyncPayload::receive);
        ClientPlayNetworking.registerGlobalReceiver(WrittenDocScreenPayload.ID, WrittenDocScreenPayload::receive);
    }
}
