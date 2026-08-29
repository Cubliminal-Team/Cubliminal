package net.limit.cubliminal.client.screen.roomcreator.data;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.limit.cubliminal.client.render.SelectionRenderer;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.networking.s2c.StructureTemplateInfoS2CPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class RoomCreatorDataManager {
    public static final RoomCreatorDataManager INSTANCE = new RoomCreatorDataManager();

    private static final Function<Integer, Text> SELECTED_CORNER_TEXT = i-> Text.translatable("item.cubliminal.room_creator_tool.set_corner", i).withColor(11163050);
    private static final Vector4f BOX_COLOR = new Vector4f(1.0f, 0.85f, 0.4f, 1.0f);
    private static final Vector3f AXIS_COLOR = new Vector3f(0.2f, 0.2f, 0.2f);
    private static final Vector4f CORNER_COLOR = new Vector4f(0.25f, 1, 0.95f, 0.5f);
    private static final Vector4f PLACEMENT_CORNER_COLOR = new Vector4f(0.98f, 0.87f, 0.2f, 0.5f);

    private BlockBox selection;
    private BlockPos corner1;
    private BlockPos corner2;
    private BlockBox placement;
    private BlockPos placementPos;

    private final Map<Identifier, Vec3i> structureTemplates = new HashMap<>();


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

    public boolean hasPlacementPos() {
        return this.placementPos != null;
    }

    public BlockPos getPlacementPos() {
        return this.placementPos;
    }

    public void setPlacementPos(BlockPos pos) {
        this.placementPos = pos;
    }

    public boolean hasPlacement() {
        return this.placement != null;
    }

    public BlockBox getPlacement() {
        return this.placement;
    }

    public void setPlacement(BlockBox placement) {
        this.placement = placement;
    }

    public void processPacketUpdate(CustomPayload payload) {
        if (payload instanceof StructureTemplateInfoS2CPayload(List<Pair<Identifier, Vec3i>> templateInfoList)) {
            this.structureTemplates.clear();
            templateInfoList.forEach(pair -> this.structureTemplates.put(pair.getFirst(), pair.getSecond()));
        }
    }

    public Map<Identifier, Vec3i> getStructureTemplates() {
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

    public boolean shouldRender() {
        return this.hasAllData() && MinecraftClient.getInstance().player.isCreativeLevelTwoOp();
    }

    public void renderSelection(WorldRenderContext ctx) {
        if (this.shouldRender()) {
            SelectionRenderer.renderBox(Box.from(this.selection), ctx, BOX_COLOR, AXIS_COLOR);
            SelectionRenderer.renderBlockOutline(this.corner1, ctx, CORNER_COLOR);
            SelectionRenderer.renderBlockOutline(this.corner2, ctx, CORNER_COLOR);
        }
    }
}
