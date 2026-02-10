package net.limit.cubliminal.networking.c2s;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.custom.WrittenDocumentBlock;
import net.limit.cubliminal.block.entity.WrittenDocumentBlockEntity;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record DocUpdateC2SPayload(BlockPos blockEntityPos, WrittenDocContentComponent component) implements CustomPayload {
    public static final Codec<DocUpdateC2SPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(DocUpdateC2SPayload::blockEntityPos),
            WrittenDocContentComponent.CODEC.fieldOf("component").forGetter(DocUpdateC2SPayload::component)
    ).apply(instance, DocUpdateC2SPayload::new));

    public static final Identifier DOC_UPDATE = Cubliminal.id("doc_update");

    public static final Id<DocUpdateC2SPayload> ID = new Id<>(DOC_UPDATE);

    public static final PacketCodec<RegistryByteBuf, DocUpdateC2SPayload> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(DocUpdateC2SPayload payload, ServerPlayNetworking.Context context) {
        BlockPos pos = payload.blockEntityPos();
        World world = context.player().getWorld();
        BlockState state = world.getBlockState(pos);
        if (state.contains(WrittenDocumentBlock.MODE)) {
            BlockState newState = state.with(WrittenDocumentBlock.MODE, payload.component().mode());
            world.setBlockState(pos, newState);
            if (world.getBlockEntity(pos) instanceof WrittenDocumentBlockEntity entity) {
                ItemStack stack = entity.getDocument();
                stack.set(CubliminalDataComponents.WRITTEN_DOC_COMPONENT, payload.component());
                entity.markDirty();
                world.updateListeners(pos, state, newState, Block.NOTIFY_ALL);
            }
        }
    }
}
