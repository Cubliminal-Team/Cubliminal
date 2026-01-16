package net.limit.cubliminal.networking.s2c;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.screen.DocScreen;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record WrittenDocScreenPayload(Hand hand) implements CustomPayload {
    private static final Codec<WrittenDocScreenPayload> RECORD_CODEC = Codec.intRange(0, Hand.values().length - 1).xmap(
            index -> new WrittenDocScreenPayload(Hand.values()[index]),
            payload -> payload.hand().ordinal()
    );

    public static final Identifier PACKED_IDENTIFIER = Cubliminal.id("written_doc_screen");

    public static final CustomPayload.Id<WrittenDocScreenPayload> ID = new Id<>(PACKED_IDENTIFIER);

    public static final PacketCodec<RegistryByteBuf, WrittenDocScreenPayload> CODEC = PacketCodecs.registryCodec(RECORD_CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void receive(WrittenDocScreenPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.player();
        if (player != null) {
            ItemStack stack = player.getStackInHand(payload.hand());
            WrittenDocContentComponent component = stack.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
            DocScreen.Contents contents = DocScreen.Contents.create(stack);
            context.client().setScreen(new DocScreen(Text.empty(), component == null ? null : component.mode(), contents));
        }
    }
}
