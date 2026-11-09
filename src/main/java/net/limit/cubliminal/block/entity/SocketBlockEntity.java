package net.limit.cubliminal.block.entity;

import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SocketBlockEntity extends BlockEntity {

    private BlockState support = null;

    public SocketBlockEntity(BlockPos pos, BlockState state) {
        super(CubliminalBlockEntities.SOCKET_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public BlockState getSupport() {
        return this.support;
    }

    public void setSupport(BlockState state) {
        this.support = state;
        this.markDirty();
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        if (this.support == null && nbt.contains("Support")) {
            this.support = BlockState.CODEC.decode(NbtOps.INSTANCE, nbt.get("Support")).getOrThrow().getFirst();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        if (this.support != null && !nbt.contains("Support")) {
            nbt.put("Support", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, this.support).getOrThrow());
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createNbt(registryLookup);
    }
}
