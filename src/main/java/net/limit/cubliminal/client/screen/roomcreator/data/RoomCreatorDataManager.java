package net.limit.cubliminal.client.screen.roomcreator.data;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.render.SelectionRenderer;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.networking.s2c.StructureTemplateInfoS2CPayload;
import net.limit.cubliminal.world.room.Room;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class RoomCreatorDataManager {
    public static final RoomCreatorDataManager INSTANCE = new RoomCreatorDataManager();

    private static final Function<Integer, Text> SELECTED_CORNER_TEXT = i-> Text.translatable("item.cubliminal.room_creator_tool.set_corner", i).withColor(11163050);
    private static final Vector4f BOX_COLOR = new Vector4f(1.0f, 0.85f, 0.4f, 1.0f);
    private static final Vector3f AXIS_COLOR = new Vector3f(0.2f, 0.2f, 0.2f);
    private static final Vector4f CORNER_COLOR = new Vector4f(0.25f, 1, 0.95f, 0.5f);

    private BlockBox selection;
    private BlockPos corner1;
    private BlockPos corner2;

    private Path lastRoomSelectionWindowPath;

    private final List<Pair<Identifier, Vec3i>> structureTemplates = new ArrayList<>();


    public boolean hasSelection() {
        return this.selection != null;
    }

    public BlockBox getSelection() {
        return this.selection;
    }

    public void setSelection(BlockBox selection) {
        this.selection = selection;
    }

    public boolean hasCorner1() {
        return this.corner1 != null;
    }

    public BlockPos getCorner1() {
        return this.corner1;
    }

    public void setCorner1(BlockPos pos) {
        this.corner1 = pos;
        this.refreshSelection();
    }

    public void setCorner1(BlockPos pos, PlayerEntity player) {
        this.setCorner1(pos);
        player.sendMessage(SELECTED_CORNER_TEXT.apply(1), true);
    }

    public boolean hasCorner2() {
        return this.corner2 != null;
    }

    public BlockPos getCorner2() {
        return this.corner2;
    }

    public void setCorner2(BlockPos pos) {
        this.corner2 = pos;
        this.refreshSelection();
    }

    public void setCorner2(BlockPos pos, PlayerEntity player) {
        this.setCorner2(pos);
        player.sendMessage(SELECTED_CORNER_TEXT.apply(2), true);
    }

    public void processPacketUpdate(CustomPayload payload) {
        if (payload instanceof StructureTemplateInfoS2CPayload(List<Pair<Identifier, Vec3i>> templateInfoList)) {
            this.structureTemplates.clear();
            this.structureTemplates.addAll(templateInfoList);
            this.structureTemplates.sort(Comparator.comparing(
                    pair -> pair.getFirst().toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
            ));
        }
    }

    public List<Pair<Identifier, Vec3i>> getStructureTemplates() {
        return this.structureTemplates;
    }

    public BlockPos getStartPos() {
        return new BlockPos(this.selection.getMinX(), this.selection.getMinY(), this.selection.getMinZ());
    }

    public Vec3i getSelectionDimensions() {
        return new Vec3i(this.selection.getBlockCountX(), this.selection.getBlockCountY(), this.selection.getBlockCountZ());
    }

    public void refreshSelection() {
        if (this.hasCorner1() && this.hasCorner2()) {
            this.selection = BlockBox.create(this.corner1, this.corner2);
        }
    }

    public boolean approximateSelection(Level level) {
        int sizeX = this.selection.getBlockCountX();
        int sizeY = this.selection.getBlockCountY();
        int sizeZ = this.selection.getBlockCountZ();
        int spacingX = level.spacing_x;
        int spacingZ = level.spacing_z;

        int reconstructedX = Math.floorDiv(sizeX, spacingX) * spacingX;
        int approxSizeX = reconstructedX < sizeX ? reconstructedX + spacingX : sizeX;

        int reconstructedZ = Math.floorDiv(sizeZ, spacingZ) * spacingZ;
        int approxSizeZ = reconstructedZ < sizeZ ? reconstructedZ + spacingZ : sizeZ;

        BlockPos min = this.getStartPos();
        BlockPos max = min.add(approxSizeX - 1, sizeY - 1, approxSizeZ - 1);
        this.setCorner1(min);
        this.setCorner2(max);

        return sizeY > level.layer_height;
    }

    public boolean hasAllData() {
        return this.hasSelection() && this.hasCorner1() && this.hasCorner2();
    }

    public void clearData(boolean exceptTemplatesList) {
        this.corner1 = null;
        this.corner2 = null;
        this.selection = null;
        if (!exceptTemplatesList) {
            this.structureTemplates.clear();
        }
    }

    public Optional<String> openRoomSelectionWindow(MinecraftClient client) {
        Path path;
        if (this.lastRoomSelectionWindowPath != null) {
            path = this.lastRoomSelectionWindowPath;
        } else {
            path = client.isIntegratedServerRunning()
                    ? client.getServer().getSavePath(WorldSavePath.ROOT).resolve(Room.GENERATED_ROOMS).normalize()
                    : client.getResourcePackDir().getParent().resolve("saves");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.json"));
            filters.flip();

            Files.createDirectories(path);

            String selectedPath = TinyFileDialogs.tinyfd_openFileDialog(
                    "Select Room JSON",
                    path.toAbsolutePath() + File.separator,
                    filters,
                    "JSON Files (*.json)",
                    false
            );

            if (selectedPath != null) {
                this.lastRoomSelectionWindowPath = Path.of(selectedPath).getParent();
                return Optional.of(selectedPath);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            Cubliminal.LOGGER.error(e.getMessage());
        }

        return Optional.empty();
    }

    public boolean shouldRender() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        return this.hasAllData() && (player.isCreativeLevelTwoOp() || player.isSpectator());
    }

    public void renderSelection(WorldRenderContext ctx) {
        if (this.shouldRender()) {
            SelectionRenderer.renderBox(Box.from(this.selection), ctx, BOX_COLOR, AXIS_COLOR);
            SelectionRenderer.renderBlockOutline(this.corner1, ctx, CORNER_COLOR);
            SelectionRenderer.renderBlockOutline(this.corner2, ctx, CORNER_COLOR);
        }
    }
}
