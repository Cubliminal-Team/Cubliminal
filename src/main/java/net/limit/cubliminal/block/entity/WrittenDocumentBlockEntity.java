package net.limit.cubliminal.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.limit.cubliminal.block.state.DocumentMode;
import net.limit.cubliminal.block.custom.WrittenDocumentBlock;
import net.limit.cubliminal.client.screen.documents.DocEditScreenHandler;
import net.limit.cubliminal.client.screen.documents.DocScreenHandler;
import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.WrittenDocumentItem;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WrittenDocumentBlockEntity extends BlockEntity implements Clearable, ExtendedScreenHandlerFactory<BlockPos> {

    private ItemStack doc = ItemStack.EMPTY;
    private boolean openEditScreen = false;
    private Optional<Identifier> texturePath = Optional.empty();
    private Optional<RawFilteredPair<Text>> text = Optional.empty();
    private boolean editing = false;
    private final Inventory inventory = new Inventory() {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return !WrittenDocumentBlockEntity.this.hasDocument();
        }

        @Override
        public ItemStack getStack(int slot) {
            return slot == 0 ? WrittenDocumentBlockEntity.this.doc : ItemStack.EMPTY;
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return this.removeStack(slot);
        }

        @Override
        public ItemStack removeStack(int slot) {
            if (slot == 0) {
                ItemStack stack = WrittenDocumentBlockEntity.this.getDocument();
                WrittenDocumentBlockEntity.this.doc = ItemStack.EMPTY;
                WrittenDocumentBlockEntity.this.updateDocData();
                return stack;
            }

            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {

        }

        @Override
        public void markDirty() {
            WrittenDocumentBlockEntity.this.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return Inventory.canPlayerUse(WrittenDocumentBlockEntity.this, player);
        }

        @Override
        public void clear() {
        }
    };

    public WrittenDocumentBlockEntity(BlockPos pos, BlockState state) {
        super(CubliminalBlockEntities.WRITTEN_DOCUMENT_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (!this.doc.isEmpty()) {
            nbt.put("Doc", this.doc.toNbt(registries));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("Doc", NbtElement.COMPOUND_TYPE)) {
            this.doc = this.resolveDocument(ItemStack.fromNbt(registries, nbt.getCompound("Doc")).orElse(ItemStack.EMPTY), null);
        } else {
            this.doc = ItemStack.EMPTY;
        }
        this.updateDocData();
    }

    public void updateDocData() {
        if (!this.doc.isEmpty()) {
            WrittenDocContentComponent component = this.doc.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
            if (component != null) {
                this.texturePath = component.texture();
                this.text = component.text();
            }
        } else {
            this.texturePath = Optional.empty();
            this.text = Optional.empty();
        }
    }

    private ItemStack resolveDocument(ItemStack doc, @Nullable PlayerEntity player) {
        if (this.world instanceof ServerWorld serverWorld) {
            WrittenDocumentItem.resolve(doc, this.getCommandSource(player, serverWorld), player);
        }

        return doc;
    }

    private ServerCommandSource getCommandSource(@Nullable PlayerEntity player, ServerWorld world) {
        String name;
        Text displayName;
        if (player == null) {
            name = "Written Document";
            displayName = Text.literal(name);
        } else {
            name = player.getName().getString();
            displayName = player.getDisplayName();
        }
        return new ServerCommandSource(CommandOutput.DUMMY, this.pos.toCenterPos(), Vec2f.ZERO, world, 2, name, displayName, world.getServer(), player);
    }

    public void setDocData(ItemStack doc, @Nullable PlayerEntity player) {
        this.doc = this.resolveDocument(doc, player);
        this.updateDocData();
        this.markDirty();
    }

    public boolean hasDocument() {
        return !this.doc.isEmpty();
    }

    public ItemStack getDocument() {
        return this.doc;
    }

    public boolean isInImageMode() {
        return getCachedState().get(WrittenDocumentBlock.MODE) == DocumentMode.IMAGE;
    }

    public float getRotationDeg() {
        BlockState state = getCachedState();
        return Direction.getHorizontalDegrees(state.get(WrittenDocumentBlock.FACING)) - state.get(WrittenDocumentBlock.ROT).rotation();
    }

    public boolean hasTexture() {
        return this.texturePath.isPresent();
    }

    public Identifier getTexture() {
        return this.texturePath.orElseThrow();
    }

    public boolean hasText() {
        return this.text.isPresent();
    }

    public RawFilteredPair<Text> getText() {
        return this.text.orElseThrow();
    }

    @Override
    public void clear() {
        this.setDocData(ItemStack.EMPTY, null);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createNbt(registries);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return this.pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.empty();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setOpenEditScreen(boolean edit) {
        this.openEditScreen = edit;
    }

    public void setEditing(boolean edit) {
        this.editing = edit;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (this.openEditScreen && !this.editing) {
            this.setEditing(true);
            return new DocEditScreenHandler(syncId, getInventory(), this.getPos());
        }

        this.editing = false;
        return new DocScreenHandler(syncId, getInventory());
    }
}
