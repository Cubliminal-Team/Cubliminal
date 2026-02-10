package net.limit.cubliminal.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.block.state.DocumentMode;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.limit.cubliminal.mixin.client.DrawContextAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.*;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DocScreen extends Screen {

    private static final Identifier DOCUMENT_TEXTURE = Cubliminal.id("textures/gui/document.png");
    private final DocumentMode mode;
    private Contents contents;
    private List<OrderedText> cachedText = null;

    public DocScreen(Text title, @Nullable DocumentMode mode, Contents contents) {
        super(title);
        this.mode = mode == null ? DocumentMode.TEXT : mode;
        this.contents = contents;
    }

    public void setPageProvider(Contents pageProvider) {
        this.contents = pageProvider;
    }

    @Override
    protected void init() {
        this.addCloseButton();
        this.initializeText();
    }

    protected void addCloseButton() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 - 100, 196, 200, 20).build());
    }

    protected void initializeText() {
        if (this.cachedText == null) {
            StringVisitable stringVisitable = this.contents.text();
            this.cachedText = this.textRenderer.wrapLines(stringVisitable, 114);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.mode == DocumentMode.IMAGE) {
            int size = 192;
            int halfSize = size / 2;
            float x = -halfSize;
            float y = -halfSize;
            this.contents.texture().ifPresent(textureId -> {
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
        } else {
            int i = (this.width - 192) / 2;
            int l = Math.min(128 / 9, this.cachedText.size());
            for (int m = 0; m < l; m++) {
                OrderedText orderedText = this.cachedText.get(m);
                context.drawText(this.textRenderer, orderedText, i + 36, 32 + m * 9, 0, false);
            }

            Style style = this.getTextStyleAt(mouseX, mouseY);
            if (style != null) {
                context.drawHoverEvent(this.textRenderer, style, mouseX, mouseY);
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
        context.drawTexture(RenderLayer::getGuiTextured, DOCUMENT_TEXTURE, (this.width - 192) / 2, 2, 0.0F, 0.0F, 192, 192, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Style style = this.getTextStyleAt(mouseX, mouseY);
            if (style != null && this.handleTextClick(style)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean handleTextClick(Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return false;
        } else {
            boolean bl = super.handleTextClick(style);
            if (bl && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.closeScreen();
            }

            return bl;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    protected void closeScreen() {
        client.setScreen(null);
    }

    @Nullable
    public Style getTextStyleAt(double x, double y) {
        if (this.cachedText.isEmpty()) {
            return null;
        } else {
            int i = MathHelper.floor(x - (this.width - 192) / 2 - 36.0);
            int j = MathHelper.floor(y - 2.0 - 30.0);
            if (i >= 0 && j >= 0) {
                int k = Math.min(128 / 9, this.cachedText.size());
                if (i <= 114 && j < 9 * k + k) {
                    int l = j / 9;
                    if (l < this.cachedText.size()) {
                        OrderedText orderedText = this.cachedText.get(l);
                        return client.textRenderer.getTextHandler().getStyleAt(orderedText, i);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public record Contents(Text text, Optional<Identifier> texture) {

        public static Contents EMPTY = new Contents(Text.empty(), Optional.empty());

        public boolean hasImage() {
            return texture.isPresent();
        }

        public static Contents create(ItemStack stack) {
            WrittenDocContentComponent component = stack.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
            if (component != null) {
                return new Contents(component.getText(MinecraftClient.getInstance().shouldFilterText()), component.texture());
            }

            return EMPTY;
        }

    }
}
