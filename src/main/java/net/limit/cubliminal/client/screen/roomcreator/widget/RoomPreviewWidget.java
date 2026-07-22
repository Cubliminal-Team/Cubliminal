package net.limit.cubliminal.client.screen.roomcreator.widget;

import net.limit.cubliminal.client.screen.roomcreator.data.ComponentData;
import net.limit.cubliminal.client.screen.roomcreator.data.FloorData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class RoomPreviewWidget extends ClickableWidget {

    private static final int BOUNDS_FILL = 0x30E8E85A;
    private static final int BOUNDS_BORDER = 0xFFE8E85A;
    private static final int ORIGIN_COLOR = 0xFF4CAF50;
    private static final int COMPONENT_FILL = 0x8000A2FF;
    private static final int COMPONENT_SELECTED_FILL = 0xC0FFA500;
    private static final int COMPONENT_BORDER = 0xFFFFFFFF;
    private static final int RELATIVE_MARKER_COLOR = 0xFF9B30FF;
    private static final int LABEL_COLOR = 0xFFDDDDDD;

    private static final int LABEL_MARGIN = 12;

    private FloorData floor;
    private int boundsSizeX = 16;
    private int boundsSizeZ = 16;
    private float scale = 8.0f;

    private int originScreenX;
    private int originScreenZ;

    private ComponentData selected;
    private ComponentData dragging;
    private int dragOffsetX;
    private int dragOffsetZ;

    private Consumer<ComponentData> selectionListener = c -> {};
    private Runnable dragListener = () -> {};

    public RoomPreviewWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
    }

    public void setFloor(FloorData floor) {
        this.floor = floor;
        this.selected = null;
        this.dragging = null;
    }

    public void setBounds(int sizeX, int sizeZ) {
        this.boundsSizeX = Math.max(1, sizeX);
        this.boundsSizeZ = Math.max(1, sizeZ);
        this.recalculateScale();
    }

    public void setSelectionListener(Consumer<ComponentData> listener) {
        this.selectionListener = listener;
    }

    public void setDragListener(Runnable listener) {
        this.dragListener = listener;
    }

    public void select(ComponentData component) {
        this.selected = component;
    }

    private void recalculateScale() {
        float scaleX = (float) (this.width - LABEL_MARGIN * 2) / this.boundsSizeX;
        float scaleZ = (float) (this.height - LABEL_MARGIN * 2) / this.boundsSizeZ;
        this.scale = Math.max(0.5f, Math.min(scaleX, scaleZ));

        // Anchor to the bottom-left of the widget's drawable area (not centered).
        this.originScreenX = this.getX() + LABEL_MARGIN;
        this.originScreenZ = this.getY() + this.height - LABEL_MARGIN;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.recalculateScale();

        int boxLeft = this.originScreenX;
        int boxRight = this.originScreenX + Math.round(this.boundsSizeX * this.scale);
        int boxBottom = this.originScreenZ;
        int boxTop = this.originScreenZ - Math.round(this.boundsSizeZ * this.scale);

        context.fill(boxLeft, boxTop, boxRight, boxBottom, BOUNDS_FILL);
        context.drawBorder(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, BOUNDS_BORDER);

        this.renderCompassLabels(context, boxLeft, boxTop, boxRight, boxBottom);

        int originMarkerSize = Math.max(4, (int) (this.scale / 2));
        context.fill(boxLeft - originMarkerSize / 2, boxBottom - originMarkerSize / 2,
                boxLeft + originMarkerSize / 2, boxBottom + originMarkerSize / 2, ORIGIN_COLOR);

        if (this.floor != null) {
            for (ComponentData component : this.floor.getComponents()) {
                this.renderComponent(context, component);
            }
        }
    }

    private void renderCompassLabels(DrawContext context, int boxLeft, int boxTop, int boxRight, int boxBottom) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int midX = (boxLeft + boxRight) / 2;
        int midZ = (boxTop + boxBottom) / 2;

        context.drawText(textRenderer, "N", midX - textRenderer.getWidth("N") / 2, boxTop - LABEL_MARGIN + 1, LABEL_COLOR, false);
        context.drawText(textRenderer, "S", midX - textRenderer.getWidth("S") / 2, boxBottom + 2, LABEL_COLOR, false);
        context.drawText(textRenderer, "W", boxLeft - LABEL_MARGIN + 1, midZ - textRenderer.fontHeight / 2, LABEL_COLOR, false);
        context.drawText(textRenderer, "E", boxRight + 2, midZ - textRenderer.fontHeight / 2, LABEL_COLOR, false);
    }

    private void renderComponent(DrawContext context, ComponentData component) {
        int x0 = this.originScreenX + Math.round(component.getRelX() * this.scale);
        int x1 = x0 + Math.max(1, Math.round(component.getWidth() * this.scale));
        int zBottom = this.originScreenZ - Math.round(component.getRelZ() * this.scale);
        int zTop = zBottom - Math.max(1, Math.round(component.getLength() * this.scale));

        int fillColor = component == this.selected ? COMPONENT_SELECTED_FILL : COMPONENT_FILL;
        context.fill(x0, zTop, x1, zBottom, fillColor);
        context.drawBorder(x0, zTop, x1 - x0, zBottom - zTop, COMPONENT_BORDER);

        int markerSize = Math.max(3, (int) (this.scale / 3));
        context.fill(x0 - markerSize / 2, zBottom - markerSize / 2,
                x0 + markerSize / 2, zBottom + markerSize / 2, RELATIVE_MARKER_COLOR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || this.floor == null || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        ComponentData hit = this.componentAt(mouseX, mouseY);
        this.selected = hit;
        this.selectionListener.accept(hit);

        if (hit != null) {
            this.dragging = hit;
            this.dragOffsetX = this.screenToBlockX(mouseX) - hit.getRelX();
            this.dragOffsetZ = this.screenToBlockZ(mouseY) - hit.getRelZ();
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragging == null || button != 0) {
            return false;
        }

        int newX = clamp(this.screenToBlockX(mouseX) - this.dragOffsetX, 0, this.boundsSizeX - this.dragging.getWidth());
        int newZ = clamp(this.screenToBlockZ(mouseY) - this.dragOffsetZ, 0, this.boundsSizeZ - this.dragging.getLength());
        this.dragging.setRelX(newX);
        this.dragging.setRelZ(newZ);
        this.dragListener.run();
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // fuh naw dawg
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.dragging = null;
        super.onRelease(mouseX, mouseY);
    }

    private ComponentData componentAt(double mouseX, double mouseY) {
        if (this.floor == null) return null;
        int blockX = this.screenToBlockX(mouseX);
        int blockZ = this.screenToBlockZ(mouseY);

        List<ComponentData> components = this.floor.getComponents();
        for (int i = components.size() - 1; i >= 0; i--) {
            ComponentData c = components.get(i);
            if (blockX >= c.getRelX() && blockX < c.getRelX() + c.getWidth()
                    && blockZ >= c.getRelZ() && blockZ < c.getRelZ() + c.getLength()) {
                return c;
            }
        }
        return null;
    }

    private int screenToBlockX(double mouseX) {
        return (int) ((mouseX - this.originScreenX) / this.scale);
    }

    private int screenToBlockZ(double mouseY) {
        return (int) ((this.originScreenZ - mouseY) / this.scale);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}