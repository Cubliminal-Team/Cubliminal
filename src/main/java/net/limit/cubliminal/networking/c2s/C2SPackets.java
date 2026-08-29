package net.limit.cubliminal.networking.c2s;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.networking.s2c.NoClipSyncPayload;
import net.limit.cubliminal.networking.s2c.SanitySyncPayload;
import net.limit.cubliminal.networking.s2c.StructureTemplateInfoS2CPayload;
import net.limit.cubliminal.networking.s2c.WrittenDocScreenPayload;

/**
 * This class registers server side actions performed when a client-to-server packet is received. It's also used
 * for payload registration, because it's done in common.
 */
public class C2SPackets implements Initer {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(NoClipC2SPayload.ID, NoClipC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NoClipSyncPayload.ID, NoClipSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SanitySyncPayload.ID, SanitySyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MultistructureBlockC2SPayload.ID, MultistructureBlockC2SPayload.PAYLOAD_CODEC);
        PayloadTypeRegistry.playS2C().register(WrittenDocScreenPayload.ID, WrittenDocScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DocUpdateC2SPayload.ID, DocUpdateC2SPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SaveSelectionC2SPayload.ID, SaveSelectionC2SPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(StructureTemplateInfoS2CPayload.ID, StructureTemplateInfoS2CPayload.PACKET_CODEC);
    }

    public static void registerGlobalReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(NoClipC2SPayload.ID, NoClipC2SPayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(MultistructureBlockC2SPayload.ID, MultistructureBlockC2SPayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(DocUpdateC2SPayload.ID, DocUpdateC2SPayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(SaveSelectionC2SPayload.ID, SaveSelectionC2SPayload::receive);
    }

    @Override
    public void init() {
        registerPayloads();
        registerGlobalReceivers();
    }
}