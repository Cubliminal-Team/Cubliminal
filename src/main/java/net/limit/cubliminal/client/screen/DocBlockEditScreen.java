package net.limit.cubliminal.client.screen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.state.DocumentMode;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.limit.cubliminal.mixin.client.DrawContextAccessor;
import net.limit.cubliminal.networking.c2s.DocUpdateC2SPayload;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Vec2i;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DocBlockEditScreen extends Screen implements ScreenHandlerProvider<DocEditScreenHandler> {

    private static final Identifier DOCUMENT_TEXTURE = Cubliminal.id("textures/gui/document.png");
    private static final Text FLIP = Text.translatable("gui.cubliminal.flip");
    private static final Text USE = Text.translatable("gui.cubliminal.use");
    private final DocEditScreenHandler handler;
    private final ItemStack document;
    private DocumentMode mode;
    private final FinalContent content;
    private int tickCounter;
    private boolean dirty;
    private final SelectionManager selectionManager;
    private WritableContent writableContent = WritableContent.EMPTY;
    private ButtonWidget useButton;
    private TextFieldWidget imagePathField;
    private long lastClickTime;
    private int lastClickIndex = -1;

    private DocBlockEditScreen(Text title, DocEditScreenHandler handler, @Nullable DocumentMode mode, FinalContent content) {
        super(title);
        this.handler = handler;
        this.document = handler.getDocItem();
        this.mode = mode == null ? DocumentMode.TEXT : mode;
        this.content = content;
        this.selectionManager = new SelectionManager(
                this::getTextContent,
                this::setTextContent,
                this::getClipboard,
                this::setClipboard,
                string -> string.length() < 1024 && this.textRenderer.getWrappedLinesHeight(string, 114) <= 128
        );
    }

    public static DocBlockEditScreen make(DocEditScreenHandler handler, PlayerInventory inventory, Text title) {
        ItemStack stack = handler.getDocItem();
        WrittenDocContentComponent component = stack.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
        return new DocBlockEditScreen(title, handler, component == null ? null : component.mode(), FinalContent.create(stack));
    }

    private void setClipboard(String clipboard) {
        if (this.client != null) {
            SelectionManager.setClipboard(this.client, clipboard);
        }
    }

    private String getClipboard() {
        return this.client != null ? SelectionManager.getClipboard(this.client) : "";
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCounter++;
    }

    @Override
    protected void init() {
        this.invalidateTextContent();
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.close();
            this.finalizeDocument();
        }).dimensions(this.width / 2 - 100, 206, 98, 20).build());
        this.addDrawableChild(ButtonWidget.builder(FLIP, button -> {
            int next = this.mode.ordinal() + 1;
            this.setMode(DocumentMode.values()[next >= DocumentMode.values().length ? 0 : next]);
        }).dimensions(this.width / 2 + 2, 206, 98, 20).build());
        this.imagePathField = new TextFieldWidget(this.textRenderer, this.width / 2 - 99, 182, 198, 20, Text.empty()) {
            @Override
            public boolean charTyped(char chr, int modifiers) {
                return DocBlockEditScreen.this.isValidCharacterForName(this.getText(), chr, this.getCursor()) && super.charTyped(chr, modifiers);
            }
        };
        this.imagePathField.setMaxLength(128);
        this.imagePathField.setEditableColor(5636095);
        this.imagePathField.setText(this.content.getTexture().orElse(Identifier.of("")).toString());
        this.addSelectableChild(this.imagePathField);
        this.useButton = this.addDrawableChild(ButtonWidget.builder(USE, button -> {
            this.updateImagePath(this.imagePathField.getText());
        }).dimensions(this.width / 2 + 102, 181, 40, 22).build());
        this.updateButtons();
    }

    private void updateButtons() {
        if (this.textMode()) {
            this.imagePathField.setVisible(false);
            this.useButton.visible = false;
        } else {
            this.imagePathField.setVisible(true);
            this.useButton.visible = true;
        }
    }

    private void updateImagePath(String newPath) {
        if (newPath != null) {
            Identifier raw = Identifier.of(newPath);
            Identifier texture = Identifier.of(raw.getNamespace(), "textures/" + raw.getPath() + ".png");
            if (this.client.getResourceManager().getResource(texture).isPresent()) {
                this.content.setTexture(raw);
                this.imagePathField.setEditableColor(5636095);
                this.dirty = true;
            } else {
                this.imagePathField.setEditableColor(Colors.LIGHT_RED);
            }
        }
    }

    private void finalizeDocument() {
        if (this.dirty) {
            ClientPlayNetworking.send(new DocUpdateC2SPayload(this.handler.getBlockEntityPos(), this.writeNbtData()));
        }
    }

    private WrittenDocContentComponent writeNbtData() {
        WrittenDocContentComponent component = new WrittenDocContentComponent(
                this.mode,
                Optional.of(RawFilteredPair.of(Text.of(this.getTextContent()))),
                this.content.getTexture(),
                false
        );
        this.document.set(CubliminalDataComponents.WRITTEN_DOC_COMPONENT, component);

        return component;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.textMode()) {
            if (this.keyPressedEditMode(keyCode)) {
                this.invalidateTextContent();
                return true;
            }
        }
        return false;
    }

    private boolean keyPressedEditMode(int keyCode) {
        if (Screen.isSelectAll(keyCode)) {
            this.selectionManager.selectAll();
            return true;
        } else if (Screen.isCopy(keyCode)) {
            this.selectionManager.copy();
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.selectionManager.paste();
            return true;
        } else if (Screen.isCut(keyCode)) {
            this.selectionManager.cut();
            return true;
        } else {
            SelectionManager.SelectionType selectionType = Screen.hasControlDown() ? SelectionManager.SelectionType.WORD : SelectionManager.SelectionType.CHARACTER;
            return switch (keyCode) {
                case 257, 335 -> {
                    this.selectionManager.insert("\n");
                    yield true;
                }
                case 259 -> {
                    this.selectionManager.delete(-1, selectionType);
                    yield true;
                }
                case 261 -> {
                    this.selectionManager.delete(1, selectionType);
                    yield true;
                }
                case 262 -> {
                    this.selectionManager.moveCursor(1, Screen.hasShiftDown(), selectionType);
                    yield true;
                }
                case 263 -> {
                    this.selectionManager.moveCursor(-1, Screen.hasShiftDown(), selectionType);
                    yield true;
                }
                case 264 -> {
                    this.moveDownLine();
                    yield true;
                }
                case 265 -> {
                    this.moveUpLine();
                    yield true;
                }
                case 268 -> {
                    this.moveToLineStart();
                    yield true;
                }
                case 269 -> {
                    this.moveToLineEnd();
                    yield true;
                }
                default -> false;
            };
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers)) {
            return true;
        } else if (StringHelper.isValidChar(chr) && this.textMode()) {
            this.selectionManager.insert(Character.toString(chr));
            this.invalidateTextContent();
            return true;
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.imageMode()) {
            int size = 192;
            int halfSize = size / 2;
            float x = -halfSize;
            float y = -halfSize;
            this.content.getTexture().ifPresent(textureId -> {
                context.getMatrices().push();
                Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
                matrix4f.translate((float) (this.width / 2) - 4, halfSize - 4, 0);
                matrix4f.scale(0.7f, 0.7f, 1.0f);
                RenderLayer layer = RenderLayer.getGuiTextured(Identifier.of(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png"));
                VertexConsumer vertexConsumer = ((DrawContextAccessor) context).getConsumers().getBuffer(layer);
                vertexConsumer.vertex(matrix4f, x, y, 0.0f).texture(0, 0).color(Colors.WHITE);
                vertexConsumer.vertex(matrix4f, x, y + size, 0.0f).texture(0, 1).color(Colors.WHITE);
                vertexConsumer.vertex(matrix4f, x + size, y + size, 0.0f).texture(1, 1).color(Colors.WHITE);
                vertexConsumer.vertex(matrix4f, x + size, y, 0.0f).texture(1, 0).color(Colors.WHITE);
                context.getMatrices().pop();
            });
            this.imagePathField.render(context, mouseX, mouseY, delta);
        } else {
            WritableContent pageContent = this.getWritableContent();
            for (Line line : pageContent.lines) {
                context.drawText(this.textRenderer, line.text, line.x, line.y, Colors.BLACK, false);
            }
            this.drawSelection(context, pageContent.selectionRectangles);
            this.drawCursor(context, pageContent.position, pageContent.atEnd);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
        context.drawTexture(RenderLayer::getGuiTextured, DOCUMENT_TEXTURE, (this.width - 192) / 2, 2, 0.0F, 0.0F, 192, 192, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button) && this.textMode()) {
            if (button == 0) {
                long l = Util.getMeasuringTimeMs();
                WritableContent pageContent = this.getWritableContent();
                int i = pageContent.getCursorPosition(this.textRenderer, this.screenPositionToAbsolutePosition(new Vec2i((int) mouseX, (int) mouseY)));
                if (i >= 0) {
                    if (i != this.lastClickIndex || l - this.lastClickTime >= 250L) {
                        this.selectionManager.moveCursorTo(i, Screen.hasShiftDown());
                    } else if (!this.selectionManager.isSelecting()) {
                        this.selectCurrentWord(i);
                    } else {
                        this.selectionManager.selectAll();
                    }

                    this.invalidateTextContent();
                }

                this.lastClickIndex = i;
                this.lastClickTime = l;
            }

        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) && this.textMode()) {
            if (button == 0) {
                WritableContent pageContent = this.getWritableContent();
                int i = pageContent.getCursorPosition(this.textRenderer, this.screenPositionToAbsolutePosition(new Vec2i((int) mouseX, (int) mouseY)));
                this.selectionManager.moveCursorTo(i, true);
                this.invalidateTextContent();
            }

        }
        return true;
    }

    private void selectCurrentWord(int cursor) {
        String string = this.getTextContent();
        this.selectionManager.setSelection(
                TextHandler.moveCursorByWords(string, -1, cursor, false),
                TextHandler.moveCursorByWords(string, 1, cursor, false)
        );
    }

    private void drawCursor(DrawContext context, Vec2i position, boolean atEnd) {
        if (this.tickCounter / 6 % 2 == 0) {
            position = this.absolutePositionToScreenPosition(position);
            if (!atEnd) {
                context.fill(position.x(), position.y() - 1, position.x() + 1, position.y() + 9, Colors.BLACK);
            } else {
                context.drawText(this.textRenderer, "_", position.x(), position.y(), 0, false);
            }
        }
    }

    private void drawSelection(DrawContext context, Rect2i[] selectionRectangles) {
        for (Rect2i rect2i : selectionRectangles) {
            int i = rect2i.getX();
            int j = rect2i.getY();
            int k = i + rect2i.getWidth();
            int l = j + rect2i.getHeight();
            context.fill(RenderLayer.getGuiTextHighlight(), i, j, k, l, Colors.BLUE);
        }
    }

    private void moveUpLine() {
        this.moveVertically(-1);
    }

    private void moveDownLine() {
        this.moveVertically(1);
    }

    private void moveVertically(int lines) {
        int i = this.selectionManager.getSelectionStart();
        int j = this.getWritableContent().getVerticalOffset(i, lines);
        this.selectionManager.moveCursorTo(j, Screen.hasShiftDown());
    }

    private void moveToLineStart() {
        if (Screen.hasControlDown()) {
            this.selectionManager.moveCursorToStart(Screen.hasShiftDown());
        } else {
            int i = this.selectionManager.getSelectionStart();
            int j = this.getWritableContent().getLineStart(i);
            this.selectionManager.moveCursorTo(j, Screen.hasShiftDown());
        }
    }

    private void moveToLineEnd() {
        if (Screen.hasControlDown()) {
            this.selectionManager.moveCursorToEnd(Screen.hasShiftDown());
        } else {
            WritableContent pageContent = this.getWritableContent();
            int i = this.selectionManager.getSelectionStart();
            int j = pageContent.getLineEnd(i);
            this.selectionManager.moveCursorTo(j, Screen.hasShiftDown());
        }
    }

    private WritableContent createWritableContent() {
        String string = this.content.getText();
        if (string.isEmpty()) {
            return WritableContent.EMPTY;
        } else {
            int i = this.selectionManager.getSelectionStart();
            int j = this.selectionManager.getSelectionEnd();
            IntList intList = new IntArrayList();
            List<Line> list = Lists.newArrayList();
            MutableInt mutableInt = new MutableInt();
            MutableBoolean mutableBoolean = new MutableBoolean();
            TextHandler textHandler = this.textRenderer.getTextHandler();
            textHandler.wrapLines(string, 114, Style.EMPTY, true, (style, start, end) -> {
                int ix = mutableInt.getAndIncrement();
                String stringx = string.substring(start, end);
                mutableBoolean.setValue(stringx.endsWith("\n"));
                String string2x = StringUtils.stripEnd(stringx, " \n");
                int jx = ix * 9;
                Vec2i positionx = this.absolutePositionToScreenPosition(new Vec2i(0, jx));
                intList.add(start);
                list.add(Line.create(style, string2x, positionx.x(), positionx.y()));
            });
            int[] is = intList.toIntArray();
            boolean bl = i == string.length();
            Vec2i position;
            if (bl && mutableBoolean.isTrue()) {
                position = new Vec2i(0, list.size() * 9);
            } else {
                int k = getLineFromOffset(is, i);
                int l = this.textRenderer.getWidth(string.substring(is[k], i));
                position = new Vec2i(l, k * 9);
            }

            List<Rect2i> list2 = Lists.<Rect2i>newArrayList();
            if (i != j) {
                int l = Math.min(i, j);
                int m = Math.max(i, j);
                int n = getLineFromOffset(is, l);
                int o = getLineFromOffset(is, m);
                if (n == o) {
                    int p = n * 9;
                    int q = is[n];
                    list2.add(this.getLineSelectionRectangle(string, textHandler, l, m, p, q));
                } else {
                    int p = n + 1 > is.length ? string.length() : is[n + 1];
                    list2.add(this.getLineSelectionRectangle(string, textHandler, l, p, n * 9, is[n]));

                    for (int q = n + 1; q < o; q++) {
                        int r = q * 9;
                        String string2 = string.substring(is[q], is[q + 1]);
                        int s = (int)textHandler.getWidth(string2);
                        list2.add(this.getRectFromCorners(new Vec2i(0, r), new Vec2i(s, r + 9)));
                    }

                    list2.add(this.getLineSelectionRectangle(string, textHandler, is[o], m, o * 9, is[o]));
                }
            }

            return new WritableContent(
                    string, position, bl, is, list.toArray(new Line[0]), list2.toArray(new Rect2i[0])
            );
        }
    }

    private void invalidateTextContent() {
        this.writableContent = null;
    }

    private String getTextContent() {
        return this.content.getText();
    }

    private WritableContent getWritableContent() {
        if (this.writableContent == null) {
            this.writableContent = this.createWritableContent();
        }
        return this.writableContent;
    }

    private void setTextContent(String newContent) {
        this.content.setText(newContent);
        this.dirty = true;
        this.invalidateTextContent();
    }

    private boolean textMode() {
        return this.mode == DocumentMode.TEXT;
    }

    private boolean imageMode() {
        return this.mode == DocumentMode.IMAGE;
    }

    private void setMode(DocumentMode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            this.updateButtons();
            this.dirty = true;
        }
    }

    private Vec2i screenPositionToAbsolutePosition(Vec2i position) {
        return new Vec2i(position.x() - (this.width - 192) / 2 - 36, position.y() - 32);
    }

    private Vec2i absolutePositionToScreenPosition(Vec2i position) {
        return new Vec2i(position.x() + (this.width - 192) / 2 + 36, position.y() + 32);
    }

    private Rect2i getLineSelectionRectangle(String string, TextHandler handler, int selectionStart, int selectionEnd, int lineY, int lineStart) {
        String string2 = string.substring(lineStart, selectionStart);
        String string3 = string.substring(lineStart, selectionEnd);
        Vec2i position = new Vec2i((int)handler.getWidth(string2), lineY);
        Vec2i position2 = new Vec2i((int)handler.getWidth(string3), lineY + 9);
        return this.getRectFromCorners(position, position2);
    }

    private Rect2i getRectFromCorners(Vec2i start, Vec2i end) {
        Vec2i position = this.absolutePositionToScreenPosition(start);
        Vec2i position2 = this.absolutePositionToScreenPosition(end);
        int i = Math.min(position.x(), position2.x());
        int j = Math.max(position.x(), position2.x());
        int k = Math.min(position.y(), position2.y());
        int l = Math.max(position.y(), position2.y());
        return new Rect2i(i, k, j - i, l - k);
    }

    static int getLineFromOffset(int[] lineStarts, int position) {
        int i = Arrays.binarySearch(lineStarts, position);
        return i < 0 ? -(i + 2) : i;
    }

    @Override
    public DocEditScreenHandler getScreenHandler() {
        return this.handler;
    }

    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    static class FinalContent {

        private String text;
        private Optional<Identifier> texture;

        public FinalContent(String text, Optional<Identifier> texture) {
            this.text = text;
            this.texture = texture;
        }

        public static FinalContent empty() {
            return new FinalContent("", Optional.empty());
        }

        public String getText() {
            return this.text;
        }

        public FinalContent setText(String string) {
            this.text = string != null && !string.isEmpty() ? string : "";
            return this;
        }

        public Optional<Identifier> getTexture() {
            return this.texture;
        }

        public FinalContent setTexture(@Nullable Identifier id) {
            this.texture = Optional.ofNullable(id);
            return this;
        }

        public static FinalContent create(ItemStack stack) {
            WrittenDocContentComponent component = stack.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
            if (component != null) {
                return new FinalContent(
                        component.getText(MinecraftClient.getInstance().shouldFilterText()).getString(),
                        component.texture()
                );
            }

            return empty();
        }
    }

    @Environment(EnvType.CLIENT)
    record Line(Style style, String content, Text text, int x, int y) {
        static Line create(Style style, String content, int x, int y) {
            return new Line(style, content, Text.literal(content).setStyle(style), x, y);
        }
    }

    @Environment(EnvType.CLIENT)
    static class WritableContent {
        static final WritableContent EMPTY = new WritableContent(
                "", Vec2i.ZERO, true, new int[]{0}, new Line[]{Line.create(Style.EMPTY, "", 0, 0)}, new Rect2i[0]
        );

        private final String content;
        final Vec2i position;
        final boolean atEnd;
        private final int[] lineStarts;
        final Line[] lines;
        final Rect2i[] selectionRectangles;

        public WritableContent(String content, Vec2i position, boolean atEnd,
                               int[] lineStarts, Line[] lines, Rect2i[] rectangles) {
            this.content = content;
            this.position = position;
            this.atEnd = atEnd;
            this.lineStarts = lineStarts;
            this.lines = lines;
            this.selectionRectangles = rectangles;
        }

        public int getCursorPosition(TextRenderer renderer, Vec2i position) {
            int i = position.y() / 9;
            if (i < 0) {
                return 0;
            } else if (i >= this.lines.length) {
                return this.content.length();
            }

            Line line = this.lines[i];
            return this.lineStarts[i] + renderer.getTextHandler().getTrimmedLength(line.content, position.x(), line.style);
        }

        public int getVerticalOffset(int position, int lines) {
            int i = getLineFromOffset(this.lineStarts, position);
            int j = i + lines;
            int m;
            if (0 <= j && j < this.lineStarts.length) {
                int k = position - this.lineStarts[i];
                int l = this.lines[j].content.length();
                m = this.lineStarts[j] + Math.min(k, l);
            } else {
                m = position;
            }

            return m;
        }

        public int getLineStart(int position) {
            int i = getLineFromOffset(this.lineStarts, position);
            return this.lineStarts[i];
        }

        public int getLineEnd(int position) {
            int i = getLineFromOffset(this.lineStarts, position);
            return this.lineStarts[i] + this.lines[i].content.length();
        }
    }
}
