package net.limit.cubliminal.client.screen.roomcreator.widget;

import net.limit.cubliminal.client.screen.roomcreator.data.ComponentBuilder;
import net.limit.cubliminal.client.screen.roomcreator.data.RoomCreatorDataManager;
import net.limit.cubliminal.level.Level;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TemplateSuggestorWidget extends ClickableWidget {

    private final TextFieldWidget templateNameWidget;
    private final List<Identifier> templatesWithSameSize = new ArrayList<>();
    private final List<Identifier> suggestedTemplates = new ArrayList<>();

    private int oldSizeX = 0;
    private int oldSizeZ = 0;

    private int inWindowIndex;
    private int selection;

    public TemplateSuggestorWidget(TextFieldWidget templateNameWidget) {
        super(
                templateNameWidget.getX() - (templateNameWidget.drawsBackground() ? 0 : 1),
                templateNameWidget.getY() + templateNameWidget.getHeight() + (templateNameWidget.drawsBackground() ? 1 : 0),
                templateNameWidget.getWidth(),
                0,
                Text.empty()
        );
        this.templateNameWidget = templateNameWidget;
    }

    private void updateWindow() {
        int visibleCount = Math.min(this.suggestedTemplates.size(), 7);
        this.height = visibleCount * 12;
        this.setX(this.templateNameWidget.getX() - (this.templateNameWidget.drawsBackground() ? 0 : 1));
        this.setY(this.templateNameWidget.getY() + this.templateNameWidget.getHeight() + (this.templateNameWidget.drawsBackground() ? 1 : 0));
        this.width = this.templateNameWidget.getWidth();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.suggestedTemplates.isEmpty()) return;

        this.updateWindow();
        int visibleCount = Math.min(this.suggestedTemplates.size(), 7);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int defaultColor = 0xF0101010;

        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 300.0f);

        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), defaultColor);

        context.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());

        boolean isMouseOverAnyItem = false;

        for (int l = 0; l < visibleCount; l++) {
            int itemIndex = l + this.inWindowIndex;
            if (itemIndex >= this.suggestedTemplates.size()) break;

            Identifier suggestion = this.suggestedTemplates.get(itemIndex);
            int rowY = this.getY() + 12 * l;

            boolean hovered = mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() &&
                    mouseY >= rowY && mouseY < rowY + 12;

            if (hovered) {
                this.select(itemIndex);
                isMouseOverAnyItem = true;
                context.fill(this.getX(), rowY, this.getX() + this.getWidth(), rowY + 12, 0x80FFFFFF);
            }

            String fullText = suggestion.toString();
            int maxWidth = this.getWidth() - 4;

            String displayText = textRenderer.getWidth(fullText) > maxWidth
                    ? textRenderer.trimToWidth(fullText, maxWidth - textRenderer.getWidth("...")) + "..."
                    : fullText;

            context.drawTextWithShadow(
                    textRenderer,
                    displayText,
                    this.getX() + 2,
                    rowY + 2,
                    itemIndex == this.selection ? Colors.YELLOW : Colors.WHITE
            );
        }

        context.disableScissor();

        context.getMatrices().pop();

        if (isMouseOverAnyItem && this.selection >= 0 && this.selection < this.suggestedTemplates.size()) {
            context.drawTooltip(textRenderer, Text.literal(this.suggestedTemplates.get(this.selection).toString()), mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.suggestedTemplates.isEmpty()) return false;

        if (keyCode == 265) { // Up
            this.scroll(-1);
            return true;
        } else if (keyCode == 264) { // Down
            this.scroll(1);
            return true;
        } else if (keyCode == 258) { // Tab
            this.complete();
            return true;
        } else if (keyCode == 256) { // Escape
            this.clearAndClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && this.active && button == 0 && !this.suggestedTemplates.isEmpty() && this.isMouseOver(mouseX, mouseY)) {
            int clickedRow = (int) ((mouseY - this.getY()) / 12) + this.inWindowIndex;
            if (clickedRow >= 0 && clickedRow < this.suggestedTemplates.size()) {
                this.select(clickedRow);
                this.complete();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.suggestedTemplates.isEmpty() && this.isMouseOver(mouseX, mouseY)) {
            int maxScroll = Math.max(0, this.suggestedTemplates.size() - 7);
            this.inWindowIndex = MathHelper.clamp((int) (this.inWindowIndex - verticalAmount), 0, maxScroll);
            return true;
        }
        return false;
    }

    private void scroll(int offset) {
        this.select(this.selection + offset);
        int maxVisible = 7;
        if (this.selection < this.inWindowIndex) {
            this.inWindowIndex = Math.max(0, this.selection);
        } else if (this.selection >= this.inWindowIndex + maxVisible) {
            this.inWindowIndex = Math.max(0, this.selection - maxVisible + 1);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    private void select(int index) {
        if (this.suggestedTemplates.isEmpty()) {
            this.templateNameWidget.setSuggestion(null);
            return;
        }

        this.selection = MathHelper.clamp(index, 0, this.suggestedTemplates.size() - 1);

        this.addNameSuggestion();
    }

    private void complete() {
        if (this.suggestedTemplates.isEmpty()) return;

        Identifier suggestion = this.suggestedTemplates.get(this.selection);
        String fullString = suggestion.toString();

        this.templateNameWidget.setText(fullString);
        this.templateNameWidget.setSelectionStart(fullString.length());
        this.templateNameWidget.setSelectionEnd(fullString.length());

        this.clearAndClose();
    }

    public void recalculateTemplateSize(Level level, ComponentBuilder component) {
        this.setSize(level.spacing_x * component.getSizeX(), level.spacing_z * component.getSizeZ());
    }

    private void setSize(int newSizeX, int newSizeZ) {
        if (this.oldSizeX == newSizeX && this.oldSizeZ == newSizeZ) return;

        this.oldSizeX = newSizeX;
        this.oldSizeZ = newSizeZ;

        this.templatesWithSameSize.clear();
        RoomCreatorDataManager.INSTANCE.getStructureTemplates().forEach((id, size) -> {
            if (size.getX() == newSizeX && size.getZ() == newSizeZ) {
                this.templatesWithSameSize.add(id);
            }
        });
        this.setTemplateName(this.templateNameWidget.getText());
    }

    public void setTemplateName(String newName) {
        this.suggestedTemplates.clear();
        this.inWindowIndex = 0;

        if (newName.isEmpty()) {
            this.suggestedTemplates.addAll(this.templatesWithSameSize);
            this.templateNameWidget.setSuggestion(null);
        } else {
            this.templatesWithSameSize.forEach(id -> {
                if (id.getPath().startsWith(newName) || id.toString().startsWith(newName)) {
                    this.suggestedTemplates.add(id);
                }
            });

            this.select(0);
        }

        if (!this.suggestedTemplates.isEmpty()) {
            this.suggestedTemplates.sort(Comparator.comparing(
                    id -> id.toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
            ));
        }
    }

    private void addNameSuggestion() {
        String currentText = this.templateNameWidget.getText();
        String suggestedText = this.suggestedTemplates.get(this.selection).toString();

        String suffix = getSuggestionSuffix(currentText, suggestedText);
        if (suffix != null) {
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            int currentWidth = font.getWidth(currentText);

            int availableWidth = (this.templateNameWidget.getWidth() - 8) - currentWidth;

            if (availableWidth <= 0) {
                this.templateNameWidget.setSuggestion(null);
                return;
            }

            if (font.getWidth(suffix) > availableWidth) {
                int ellipsisWidth = font.getWidth("...");
                if (availableWidth > ellipsisWidth) {
                    suffix = font.trimToWidth(suffix, availableWidth - ellipsisWidth) + "...";
                } else {
                    suffix = null;
                }
            }
        }

        this.templateNameWidget.setSuggestion(suffix);
    }

    private void clearAndClose() {
        this.suggestedTemplates.clear();
        this.visible = false;
        this.templateNameWidget.setSuggestion(null);
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) return;
        if (!visible || this.suggestedTemplates.isEmpty()) {
            this.visible = false;
            this.templateNameWidget.setSuggestion(null);
        } else {
            this.visible = true;
            this.select(this.selection);
        }
    }

    @Nullable
    static String getSuggestionSuffix(String original, String suggestion) {
        return suggestion.startsWith(original) ? suggestion.substring(original.length()) : null;
    }
}