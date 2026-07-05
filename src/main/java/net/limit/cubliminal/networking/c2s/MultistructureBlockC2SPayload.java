package net.limit.cubliminal.networking.c2s;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.entity.MultiStructureBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import java.nio.charset.StandardCharsets;

public class MultistructureBlockC2SPayload implements CustomPayload {
    public static final Codec<MultistructureBlockC2SPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StructureBlockContext.CODEC.fieldOf("structure_block_context").forGetter(MultistructureBlockC2SPayload::context)
    ).apply(instance, MultistructureBlockC2SPayload::new));

    private final StructureBlockContext context;

    public static Identifier USBLOCK_UPDATE = Cubliminal.id("usblock_update");

    public static final CustomPayload.Id<MultistructureBlockC2SPayload> ID = new CustomPayload.Id<>(USBLOCK_UPDATE);

    public static final PacketCodec<RegistryByteBuf, MultistructureBlockC2SPayload> PAYLOAD_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);

    public MultistructureBlockC2SPayload(BlockPos pos, MultiStructureBlockEntity.Action action, StructureBlockMode mode, String templateName, BlockPos offset, Vec3i size, BlockMirror mirror, BlockRotation rotation, String metadata, boolean ignoreEntities, boolean showAir, boolean showBoundingBox, float integrity, long seed) {
        int i = 0;
        if (ignoreEntities) {
            i |= 1;
        }

        if (showAir) {
            i |= 2;
        }

        if (showBoundingBox) {
            i |= 4;
        }
        this.context = new StructureBlockContext(pos, action, mode, templateName, offset, size, mirror, rotation, metadata, i, integrity, seed);
    }

    public MultistructureBlockC2SPayload(StructureBlockContext context) {
        this.context = context;
    }

    public StructureBlockContext context() {
        return this.context;
    }

    public BlockPos getPos() {
        return context.pos();
    }

    public MultiStructureBlockEntity.Action getAction() {
        return context.action();
    }

    public StructureBlockMode getMode() {
        return context.mode();
    }

    public String getTemplateName() {
        return context.templateName();
    }

    public BlockPos getOffset() {
        return context.offset();
    }

    public Vec3i getSize() {
        return context.size();
    }

    public BlockMirror getMirror() {
        return context.mirror();
    }

    public BlockRotation getRotation() {
        return context.rotation();
    }

    public String getMetadata() {
        return context.metadata();
    }

    public boolean shouldIgnoreEntities() {
        return context.shouldIgnoreEntities();
    }

    public boolean shouldShowAir() {
        return context.shouldShowAir();
    }

    public boolean shouldShowBoundingBox() {
        return context.shouldShowBoundingBox();
    }

    public float getIntegrity() {
        return context.integrity();
    }

    public long getSeed() {
        return context.seed();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(MultistructureBlockC2SPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().isCreativeLevelTwoOp()) {
            BlockPos blockPos = payload.getPos();
            BlockState blockState = context.player().getWorld().getBlockState(blockPos);
            BlockEntity blockEntity = context.player().getWorld().getBlockEntity(blockPos);
            if (blockEntity instanceof MultiStructureBlockEntity multiStructureBlockEntity) {
                multiStructureBlockEntity.setMode(payload.getMode());
                multiStructureBlockEntity.setTemplateName(payload.getTemplateName());
                multiStructureBlockEntity.setOffset(payload.getOffset());
                multiStructureBlockEntity.setSize(payload.getSize());
                multiStructureBlockEntity.setMirror(payload.getMirror());
                multiStructureBlockEntity.setRotation(payload.getRotation());
                multiStructureBlockEntity.setMetadata(payload.getMetadata());
                multiStructureBlockEntity.setIgnoreEntities(payload.shouldIgnoreEntities());
                multiStructureBlockEntity.setShowAir(payload.shouldShowAir());
                multiStructureBlockEntity.setShowBoundingBox(payload.shouldShowBoundingBox());
                multiStructureBlockEntity.setIntegrity(payload.getIntegrity());
                multiStructureBlockEntity.setSeed(payload.getSeed());
                if (multiStructureBlockEntity.hasStructureName()) {
                    String string = multiStructureBlockEntity.getTemplateName();
                    if (payload.getAction() == MultiStructureBlockEntity.Action.SAVE_AREA) {
                        if (multiStructureBlockEntity.saveStructure()) {
                            context.player().sendMessage(Text.translatable("structure_block.save_success", string), false);
                        } else {
                            context.player().sendMessage(Text.translatable("structure_block.save_failure", string), false);
                        }
                    } else if (payload.getAction() == MultiStructureBlockEntity.Action.LOAD_AREA) {
                        if (!multiStructureBlockEntity.isStructureAvailable()) {
                            context.player().sendMessage(Text.translatable("structure_block.load_not_found", string), false);
                        } else if (multiStructureBlockEntity.loadAndTryPlaceStructure(context.player().getServerWorld())) {
                            context.player().sendMessage(Text.translatable("structure_block.load_success", string), false);
                        } else {
                            context.player().sendMessage(Text.translatable("structure_block.load_prepare", string), false);
                        }
                    } else if (payload.getAction() == MultiStructureBlockEntity.Action.SCAN_AREA) {
                        if (multiStructureBlockEntity.detectStructureSize()) {
                            context.player().sendMessage(Text.translatable("structure_block.size_success", string), false);
                        } else {
                            context.player().sendMessage(Text.translatable("structure_block.size_failure"), false);
                        }
                    }
                } else {
                    context.player().sendMessage(Text.translatable("structure_block.invalid_structure_name", payload.getTemplateName()), false);
                }

                multiStructureBlockEntity.markDirty();
                context.player().getWorld().updateListeners(blockPos, blockState, blockState, 3);
            }
        }
    }

    public record StructureBlockContext(BlockPos pos, MultiStructureBlockEntity.Action action, StructureBlockMode mode, String templateName, BlockPos offset, Vec3i size, BlockMirror mirror, BlockRotation rotation, String metadata, int booleans, float integrity, long seed) {
        public static final Codec<StructureBlockContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(StructureBlockContext::pos),
                StringIdentifiable.BasicCodec.STRING.fieldOf("action").xmap(MultiStructureBlockEntity.Action::valueOf, MultiStructureBlockEntity.Action::toString).forGetter(StructureBlockContext::action),
                StringIdentifiable.BasicCodec.STRING.fieldOf("mode").xmap(StructureBlockMode::valueOf, StructureBlockMode::toString).forGetter(StructureBlockContext::mode),
                Codec.STRING.fieldOf("templateName").forGetter(StructureBlockContext::templateName),
                BlockPos.CODEC.fieldOf("offset").forGetter(StructureBlockContext::offset),
                Vec3i.CODEC.fieldOf("size").forGetter(StructureBlockContext::size),
                BlockMirror.CODEC.fieldOf("mirror").forGetter(StructureBlockContext::mirror),
                BlockRotation.CODEC.fieldOf("rotation").forGetter(StructureBlockContext::rotation),
                Codec.STRING.fieldOf("metadata").forGetter(StructureBlockContext::metadata),
                Codec.INT.fieldOf("booleans").forGetter(StructureBlockContext::booleans),
                Codec.FLOAT.fieldOf("integrity").forGetter(StructureBlockContext::integrity),
                Codec.LONG.fieldOf("seed").forGetter(StructureBlockContext::seed)
        ).apply(instance, StructureBlockContext::new));

        public StructureBlockContext(BlockPos pos, MultiStructureBlockEntity.Action action, StructureBlockMode mode, String templateName, BlockPos offset, Vec3i size, BlockMirror mirror, BlockRotation rotation, String metadata, int booleans, float integrity, long seed) {
            this.pos = pos;
            this.action = action;
            this.mode = mode;
            this.templateName = templateName;
            this.offset = new BlockPos(
                    MathHelper.clamp(offset.getX(), -MultiStructureBlockEntity.structureSizeLimit(), MultiStructureBlockEntity.structureSizeLimit()),
                    MathHelper.clamp(offset.getY(), -MultiStructureBlockEntity.structureSizeLimit(), MultiStructureBlockEntity.structureSizeLimit()),
                    MathHelper.clamp(offset.getZ(), -MultiStructureBlockEntity.structureSizeLimit(), MultiStructureBlockEntity.structureSizeLimit()));
            this.size = new Vec3i(
                    MathHelper.clamp(size.getX(), 0, MultiStructureBlockEntity.structureSizeLimit()),
                    MathHelper.clamp(size.getY(), 0, MultiStructureBlockEntity.structureSizeLimit()),
                    MathHelper.clamp(size.getZ(), 0, MultiStructureBlockEntity.structureSizeLimit()));
            this.mirror = mirror;
            this.rotation = rotation;
            this.metadata = decode(metadata, 128);
            this.booleans = booleans;
            this.integrity = MathHelper.clamp(integrity, 0.0F, 1.0F);
            this.seed = seed;
        }

        public static String decode(String packet, int maxLength) {
            int i = ByteBufUtil.utf8MaxBytes(maxLength);
            int j = packet.getBytes(StandardCharsets.UTF_8).length;
            if (j > i) {
                throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i + ")");
            } else {
                if (packet.length() > maxLength) {
                    int var10002 = packet.length();
                    throw new DecoderException("The received string length is longer than maximum allowed (" + var10002 + " > " + maxLength + ")");
                } else {
                    return packet;
                }
            }
        }

        public boolean shouldIgnoreEntities() {
            return (booleans & 1) != 0;
        }

        public boolean shouldShowAir() {
            return (booleans & 2) != 0;
        }

        public boolean shouldShowBoundingBox() {
            return (booleans & 4) != 0;
        }
    }
}
